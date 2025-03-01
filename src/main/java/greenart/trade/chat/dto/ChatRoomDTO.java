package greenart.trade.chat.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {
    private Long chatRoomId;
    private String senderProfileImageUrl;
    private String receiverProfileImageUrl;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String receiverName;
    private List<MessageDTO> messages; // 채팅방의 메시지 목록
}
