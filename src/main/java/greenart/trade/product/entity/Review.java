package greenart.trade.product.entity;

import greenart.trade.mebmer.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "trading_review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId; // 후기 ID

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품

    @OneToOne(mappedBy = "review")
    @JoinColumn(name = "evaluation_id")  // Evaluation과 연결되는 컬럼 이름
    private Evaluation evaluation;

    @Column(length = 255)
    @NotNull(message = "리뷰 내용은 필수입니다.")
    private String message; // 후기 메시지

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 일자

    private String authorUsername;
}