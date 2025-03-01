package greenart.trade.chat.service;

import greenart.trade.chat.dto.MessageDTO;
import greenart.trade.chat.entity.ChatRoom;
import greenart.trade.chat.entity.Message;
import greenart.trade.chat.entity.Status;
import greenart.trade.chat.repository.ChatRoomRepository;
import greenart.trade.chat.repository.MessageRepository;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatConverter chatConverter;

    @Transactional
    public void saveMessage(MessageDTO messageDTO) {
        try {
            // MessageDTO 확인 로그
            log.debug("MessageDTO received: {}", messageDTO);

            // 채팅방 검증
            ChatRoom chatRoom = chatRoomRepository.findByChatRoomId(messageDTO.getChatRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid chat room ID: " + messageDTO.getChatRoomId()));
            log.debug("ChatRoom lookup result: {}", chatRoom);

            // 발신자 검증
            Member sender = memberRepository.findById(messageDTO.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid sender ID: " + messageDTO.getSenderId()));
            log.debug("Sender lookup result: {}", sender);

            // 수신자 검증
            Member receiver = memberRepository.findById(messageDTO.getReceiverId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid receiver ID: " + messageDTO.getReceiverId()));
            log.debug("Receiver lookup result: {}", receiver);

            // 메시지 객체 생성
            Message message = Message.builder()
                    .chatRoom(chatRoom)
                    .sender(sender)
                    .receiver(receiver)
                    .content(messageDTO.getContent())
                    .createdAt(messageDTO.getCreatedAt())
                    .status(Status.valueOf(messageDTO.getStatus()))
                    .build();
            log.debug("Message object built: {}", message);

            // 메시지 저장
            messageRepository.save(message);
            log.info("Message saved successfully: {}", message);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input while saving message: {}", e.getMessage());
            throw e; // 필드 검증 실패는 재전달
        } catch (Exception e) {
            log.error("Unexpected error saving message: ", e);
            throw new RuntimeException("Error saving message", e);
        }
    }



    // 메시지를 MessageDTO로 변환하여 반환
    public List<MessageDTO> getMessagesByChatRoomId(Long chatRoomId) {
        // 채팅방의 메시지를 가져옴
        List<Message> messages = messageRepository.findMessagesByChatRoomId(chatRoomId);

        // 메시지 목록을 DTO로 변환
        List<MessageDTO> messageDTOList = new ArrayList<>();
        for (Message message : messages) {
            MessageDTO messageDTO = MessageDTO.builder()
                    .messageId(message.getMessageId())
                    .chatRoomId(message.getChatRoom().getChatRoomId())
                    .senderId(message.getSender().getMemberId())
                    .receiverId(message.getReceiver().getMemberId())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .status(message.getStatus().toString())
                    .senderName(message.getSender().getName()) // 추가된 부분: senderName
                    .receiverName(message.getReceiver().getName()) // 추가된 부분: receiverName
                    .build();
            messageDTOList.add(messageDTO);
        }
        return messageDTOList;
    }

    // 특정 Message를 MessageDTO로 반환
    public MessageDTO getMessageDTO(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("message not found"));
        return chatConverter.toMessageDTO(message);
    }

    public void markMessagesAsRead(Long chatRoomId, Long memberId) {
        List<Message> messages = messageRepository.findMessagesByChatRoomId(chatRoomId)
                .stream()
                .filter(message -> message.getReceiver().getMemberId().equals(memberId))
                .peek(message -> message.setStatus(Status.UNREAD))
                .collect(Collectors.toList());
        messageRepository.saveAll(messages);
    }
}
