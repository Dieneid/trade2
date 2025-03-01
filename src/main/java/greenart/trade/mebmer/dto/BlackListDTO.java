package greenart.trade.mebmer.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class BlackListDTO {

    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;

}
