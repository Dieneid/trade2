package greenart.trade.chat.service;

import greenart.trade.chat.dto.ChatRoomDTO;
import greenart.trade.chat.dto.MessageDTO;
import greenart.trade.chat.entity.ChatRoom;
import greenart.trade.chat.entity.Message;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
@Service
public class ChatConverter {
    // ChatRoom 엔티티를 ChatRoomDTO로 변환
    public ChatRoomDTO toChatRoomDTO(ChatRoom chatRoom) {
        return new ChatRoomDTO(
                chatRoom.getChatRoomId(),
                chatRoom.getSender().getProfileImageUrl(),
                chatRoom.getReceiver().getProfileImageUrl(),
                chatRoom.getSender().getMemberId(),    // Sender ID
                chatRoom.getReceiver().getMemberId(),  // Receiver ID
                chatRoom.getSender().getName(),        // Sender Name
                chatRoom.getReceiver().getName(),      // Receiver Name
                chatRoom.getMessages().stream()
                        .map(this::toMessageDTO)       // Message 리스트를 DTO로 변환
                        .collect(Collectors.toList())
        );
    }

    // Message 엔티티를 MessageDTO로 변환
    public MessageDTO toMessageDTO(Message message) {
        return new MessageDTO(
                message.getMessageId(),
                message.getChatRoom().getChatRoomId(), // ChatRoom ID
                message.getSender().getMemberId(),    // Sender ID
                message.getReceiver().getMemberId(),  // Receiver ID
                message.getSender().getName(),        // Sender Name
                message.getReceiver().getName(),      // Receiver Name
                message.getContent(),                 // Content
                message.getCreatedAt(),               // CreatedAt
                message.getStatus().toString()                      // Read status
        );
    }
}
