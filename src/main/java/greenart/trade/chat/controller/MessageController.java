package greenart.trade.chat.controller;

import greenart.trade.chat.dto.MessageDTO;
import greenart.trade.chat.service.ChatRoomService;
import greenart.trade.chat.service.MessageService;
import greenart.trade.mebmer.dto.AuthDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;

    @MessageMapping("/chat/send/chatRoom")
    public void handleSendMessage(@Payload MessageDTO messageDTO) {
        log.info("Received message in room {}", messageDTO);
        log.info("Saving message: {}", messageDTO);

        // 메시지 저장
        messageService.saveMessage(messageDTO);

        // 클라이언트에게 메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/" + messageDTO.getChatRoomId(), messageDTO);  // 특정 채팅방으로 메시지 전달
    }

    @GetMapping("/chat/messages/{chatRoomId}")
    @ResponseBody
    public Map<String, Object> getMessagesByRoomId(@PathVariable Long chatRoomId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<MessageDTO> messages = messageService.getMessagesByChatRoomId(chatRoomId);
            response.put("messages", messages);
            response.put("error", null);
        } catch (Exception e) {
            response.put("messages", null);
            response.put("error", "Failed to fetch messages: " + e.getMessage());
        }
        return response;
    }

    // 채팅방에 입장 시 메시지 읽음 처리 및 메시지 불러오기
    @PostMapping("/chat/messages/{chatRoomId}")
    public ResponseEntity<List<MessageDTO>> enterChatRoom(
            @PathVariable Long chatRoomId,
            @RequestBody AuthDTO authDTO) {
        Long memberId = authDTO.getMemberId();

        // 채팅방 입장 시, 메시지 상태를 읽음 처리
        messageService.markMessagesAsRead(chatRoomId, memberId);

        // 읽음 처리된 후 메시지 목록을 반환
        List<MessageDTO> messages = messageService.getMessagesByChatRoomId(chatRoomId);

        return ResponseEntity.ok(messages);
    }
}
