package greenart.trade.product.dto;

import greenart.trade.common.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO extends BaseEntity {
    private Long reviewId; // 후기 ID
    private Long memberId; // 회원 ID
    private Long productId; // 상품 ID

    private EvaluationDTO evaluationDTO;

    @NotNull(message = "리뷰 내용은 필수입니다.") // null이 아니어야 함
    private String message; // 후기 메시지
    private LocalDateTime createdAt;
}
