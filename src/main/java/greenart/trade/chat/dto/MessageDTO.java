package greenart.trade.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
    private Long messageId;
    @NotNull(message = "ChatRoom ID is required")
    private Long chatRoomId;

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    private String senderName;
    private String receiverName;
    private String content;   // 메시지 내용
    private LocalDateTime createdAt = LocalDateTime.now(); // 메시지 보낸 시간
    private String status;
}
