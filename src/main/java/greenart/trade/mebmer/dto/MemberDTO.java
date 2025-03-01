package greenart.trade.mebmer.dto;


import greenart.trade.product.dto.ProductDTO;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class MemberDTO {

    private Long memberId;
    private String name;
    private String email;
    private String password;
    private String compPassword;
    private String phoneNumber;
    private String profileImageUrl;

    private double averageScore;

    private boolean checkNum = false;

    // 주소
    private String city;
    private String street;
    private String zipcode;

    private List<ProductDTO> favoriteProducts;
}
