package greenart.trade.chat.controller;

import greenart.trade.chat.dto.ChatRoomDTO;
import greenart.trade.chat.dto.MessageDTO;
import greenart.trade.chat.entity.ChatRoom;
import greenart.trade.chat.service.ChatConverter;
import greenart.trade.chat.service.ChatRoomService;
import greenart.trade.chat.service.MessageService;
import greenart.trade.mebmer.dto.AuthDTO;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.service.MemberService;
import greenart.trade.product.dto.ProductDTO;
import greenart.trade.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProductService productService;
    private final ChatConverter chatConverter;
    private final MemberService memberService;

    // 메시지 전송 처리
    @MessageMapping("/message/send/{chatRoomId}")
    public void sendMessage(@PathVariable Long chatRoomId, @Payload MessageDTO messageDTO) {
        try {
            // WebSocket을 통한 메시지 전송
            messagingTemplate.convertAndSend("/sub/chat/" + chatRoomId, messageDTO);

            // 메시지 저장
            messageService.saveMessage(messageDTO);
        } catch (Exception e) {
            log.error("Error processing message for chatRoomId: {}", chatRoomId, e);
        }
    }

    @PostMapping("/chat/sendMessage")
    public ResponseEntity<String> sendMessage(@Validated @RequestBody MessageDTO messageDTO, @AuthenticationPrincipal AuthDTO authDTO) {
        log.debug("Received MessageDTO: {}", messageDTO);
        try {
            // 필수 값 검증
            if (messageDTO.getChatRoomId() == null || messageDTO.getContent() == null) {
                throw new IllegalArgumentException("Required fields are missing in the request.");
            }

            // 채팅방 정보 가져오기
            ChatRoom chatRoom = chatRoomService.findById(messageDTO.getChatRoomId());
            // 임시 변수 temp를 사용하여 senderId와 receiverId를 바꿔야 할 경우 처리
            Long temp;
            String tempName;

            // senderId와 receiverId에 해당하는 Member 조회
            Member sender = chatRoom.getSender();  // sender는 이미 Member 객체로 매핑됨
            Member receiver = chatRoom.getReceiver();  // receiver는 이미 Member 객체로 매핑됨


            String senderName = sender.getName();
            String receiverName = receiver.getName();

            // senderId와 receiverId를 authDTO와 비교하여 설정
            if (authDTO.getMemberId().equals(chatRoom.getSender().getMemberId())) {
                // 현재 사용자가 senderId인 경우
                messageDTO.setSenderId(chatRoom.getSender().getMemberId());
                messageDTO.setReceiverId(chatRoom.getReceiver().getMemberId());
                messageDTO.setSenderName(senderName); // senderName 설정
                messageDTO.setReceiverName(receiverName); // receiverName 설정
            } else if (authDTO.getMemberId().equals(chatRoom.getReceiver().getMemberId())) {
                // 현재 사용자가 receiverId인 경우, senderId와 receiverId를 바꿉니다.
                temp = messageDTO.getSenderId();
                messageDTO.setSenderId(messageDTO.getReceiverId());
                messageDTO.setReceiverId(temp);
                tempName = messageDTO.getSenderName();
                messageDTO.setSenderName(messageDTO.getReceiverName());
                messageDTO.setReceiverName(tempName);
            } else {
                return ResponseEntity.badRequest().body("Unauthorized access to the chat room.");
            }

            // WebSocket 메시지 전송
            Long chatRoomId = messageDTO.getChatRoomId();
            messagingTemplate.convertAndSend("/sub/chat/" + chatRoomId, messageDTO);

            // 메시지 저장
            messageService.saveMessage(messageDTO);

            return ResponseEntity.ok("메시지 전송 성공");
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.status(500).body("메시지 전송 실패: " + e.getMessage());
        }
    }


    @GetMapping("/chat/chatRoom/{productId}")
    @ResponseBody
    public Map<String, Object> createOrGetChatRoom(@AuthenticationPrincipal AuthDTO authDTO,
                                                   @PathVariable Long productId) {
        Long senderId = authDTO.getMemberId();

        // 상품 정보 가져오기
        ProductDTO product = productService.getProductById(productId);
        Long receiverId = product.getMemberId();  // 해당 상품의 판매자 ID

        // 채팅방 가져오기 또는 생성
        ChatRoom chatRoom = chatRoomService.getOrCreateRoom(senderId, receiverId);

        // 채팅방 ID가 없으면 새로 생성
        if (chatRoom == null) {
            chatRoom = chatRoomService.getOrCreateRoom(senderId, receiverId);
        }

        // 채팅방 ID 반환
        Map<String, Object> response = new HashMap<>();
        response.put("chatRoomId", chatRoom.getChatRoomId());
        return response;
    }


    @GetMapping("/chat/chatRoomListData")
    @ResponseBody
    public ResponseEntity<List<ChatRoomDTO>> chatRoomList(@AuthenticationPrincipal AuthDTO authDTO) {
        if (authDTO == null) {
            throw new RuntimeException("로그인된 사용자가 없습니다.");
        }
        Long memberId = authDTO.getMemberId();
        List<ChatRoomDTO> chatRoomDTOList = chatRoomService.getChatRoomsByMemberId(memberId)
                .stream()
                .map(chatRoom -> chatConverter.toChatRoomDTO(chatRoom))
                .collect(Collectors.toList());// memberId에 맞는 채팅방만 반환

        return ResponseEntity.ok(chatRoomDTOList);
    }

    @GetMapping("/chat/chatRoomList")
    public String readChatRoomList(@AuthenticationPrincipal AuthDTO authDTO, Model model) {
        if (authDTO == null) {
            throw new RuntimeException("로그인된 사용자가 없습니다.");
        }

        model.addAttribute("memberId", authDTO.getMemberId());
        model.addAttribute("memberName", authDTO.getMemberName());
        model.addAttribute("authDTO", authDTO);
        return "chat/chatRoomList";
    }
}
