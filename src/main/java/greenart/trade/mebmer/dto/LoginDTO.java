package greenart.trade.mebmer.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class LoginDTO {
    private String email;
    private String password;
}
