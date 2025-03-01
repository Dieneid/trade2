package greenart.trade.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceOfferDTO {

    @NotNull
    private Long offerId; // 가격 제시 ID
    @NotNull
    private Long memberId; // 회원 ID
    @NotNull
    private Long productId; // 상품 ID
    @NotNull
    private Long offeredPrice; // 제시 가격
    private Boolean isAccept; // 수락 여부
    private LocalDateTime createdAt; // 생성 일자

    public PriceOfferDTO(Long productId, Long offeredPrice, Boolean isAccept, LocalDateTime createdAt) {
        this.productId = productId;
        this.offeredPrice = offeredPrice;
        this.isAccept = isAccept;
        this.createdAt = createdAt;
    }
}
