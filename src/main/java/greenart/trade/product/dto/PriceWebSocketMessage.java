package greenart.trade.product.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceWebSocketMessage {
    private Long productId;     // 상품 ID
    private Long memberId;      // 회원 ID
    private Long offeredPrice;  // 제시 가격
    private String memberName;  // 제시자의 이름
    private Boolean isAccept;      // 메시지 상태 (e.g., "제시됨", "수락됨")
}