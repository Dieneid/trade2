package greenart.trade.product.entity;

import greenart.trade.common.entity.BaseEntity;
import greenart.trade.mebmer.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = { "images", "category", "member" }) // 순환 참조 방지
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;  // 상품 ID

    @Column(nullable = false)
    private String title;  // 상품 판매 제목

    @Enumerated(EnumType.STRING) // Persisting enum as its name
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.NEW;  // 상품 상태 (판매 중, 예약중, 판매 완료)

    @Column(name = "sell_price")
    private Long sellPrice;  // 가격

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(nullable = false)
    private String description;  // 상품 설명

    @Column(name = "refreshed_at")
    private LocalDateTime refreshedAt;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id")
    private Category category;  // 카테고리

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();  // 상품 이미지 리스트

    @ManyToOne(fetch = FetchType.LAZY)  // 회원과의 다대일 관계 설정
    @JoinColumn(name = "member_id")  // 외래 키 설정
    private Member member;  // 상품을 등록한 회원

    public Product(Long productId) {
        this.productId = productId;
    }

    @Transient
    private ProductImage firstImage; // 첫 번째 이미지를 위한 필드 (데이터베이스와 연결되지 않음)

    public ProductImage getFirstImage() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }
    @Column(nullable = false ,name = "favorite_count")
    @Builder.Default
    private Long favoriteCount = 0L;

    @Builder.Default
    private boolean enabled = true;
}
