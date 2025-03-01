package greenart.trade.mebmer.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class BlackDTO {

    private Long memberId;
    private String email;
    private String name;
}
