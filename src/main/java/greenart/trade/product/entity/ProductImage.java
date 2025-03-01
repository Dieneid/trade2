package greenart.trade.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "product")
@Entity
@Table(name = "PRODUCT_IMAGE")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;  // 이미지 ID

    @Column(name = "IMG_NAME")
    private String imgName;  // 이미지 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")  // Product와 연결되는 컬럼 이름
    private Product product;  // 해당 이미지가 속한 상품
}
