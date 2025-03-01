package greenart.trade.mebmer.entity;


import jakarta.persistence.Embeddable;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Embeddable
public class Address {

    private String city;
    private String street;
    private String zipcode;

}
