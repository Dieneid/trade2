package greenart.trade.product.dto;

import greenart.trade.product.entity.Product;
import greenart.trade.product.entity.ProductImage;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long productId;

    @NotBlank(message = "공백X")
    @Size(max = 16, message = "제목은 16글자 이하로 작성해주세요.")
    private String title;

    @Builder.Default
    private List<ProductImageDTO> imageDTOList = new ArrayList<>();

    private String status;

    @NotNull(message = "가격은 필수 입력 항목입니다.")
    @Min(value = 0, message = "가격은 0원 이상입니다.")
    @Digits(integer = 10, fraction = 0, message = "가격은 숫자만 입력 가능합니다.")
    private Long sellPrice;

    private Long viewCount;

    @Size(max = 1000, message = "상품 설명은 1000자 이하로 입력해주세요.")
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime refreshedAt;

    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long categoryId;

    private Long memberId;
    private String memberName;

    private Long firstImageId; // 첫 번째 이미지 ID

    // Product 객체를 매개변수로 받는 생성자
    public ProductDTO(Product product) {
        this.productId = product.getProductId();  // Product 객체에서 productId 값을 가져옴
        this.title = product.getTitle();  // Product 객체에서 title 값을 가져옴
        this.sellPrice = product.getSellPrice();  // Product 객체에서 sellPrice 값을 가져옴
        this.viewCount = product.getViewCount();  // Product 객체에서 viewCount 값을 가져옴
        this.description = product.getDescription();  // Product 객체에서 description 값을 가져옴
        this.createdAt = product.getCreatedAt();  // Product 객체에서 createdAt 값을 가져옴
        this.updatedAt = product.getUpdatedAt();  // Product 객체에서 updatedAt 값을 가져옴
        this.refreshedAt = product.getRefreshedAt();  // Product 객체에서 refreshedAt 값을 가져옴
        this.memberName = product.getMember().getName();
        this.memberId = product.getMember().getMemberId();

        // 첫 번째 이미지를 처리하기 위한 코드
        ProductImage firstImage = product.getFirstImage();
        this.firstImageId = (firstImage != null) ? firstImage.getImageId() : null;
    }
    private Long favoriteCount;
}
