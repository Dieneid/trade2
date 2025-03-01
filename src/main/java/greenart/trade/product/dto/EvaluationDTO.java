package greenart.trade.product.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDTO {

    private Long evaluationId;
    private Double score;
}
