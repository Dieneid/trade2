package greenart.trade.product.entity;

import greenart.trade.common.entity.BaseEntity;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.product.dto.PriceOfferDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Table(name = "price_offer")
public class PriceOffer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long offerId; // 가격 제시 ID

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품

    @Column(nullable = false)
    private Long offeredPrice; // 제시 가격

    @Column(nullable = false)
    private Boolean isAccept; // 수락 여부

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 일자

    public PriceOfferDTO toDTO() {
        return new PriceOfferDTO(this.product.getProductId(), this.offeredPrice, this.isAccept, this.createdAt);
    }
}
