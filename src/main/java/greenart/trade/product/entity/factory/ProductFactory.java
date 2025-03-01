package greenart.trade.product.entity.factory;

import greenart.trade.product.dto.CategoryDTO;
import greenart.trade.product.dto.ProductDTO;
import greenart.trade.product.entity.Category;
import greenart.trade.product.entity.Product;
import greenart.trade.product.entity.ProductImage;
import greenart.trade.product.entity.Status;
import greenart.trade.product.service.CategoryService;

import java.util.stream.Collectors;

public class ProductFactory {

    private final CategoryService categoryService;

    // CategoryService를 주입받기 위한 생성자
    public ProductFactory(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ProductDTO를 기반으로 Product 엔티티 객체를 생성
    public Product createProduct(ProductDTO productDTO) {
        // 카테고리 조회
        CategoryDTO categoryDTO = categoryService.getCategoryById(productDTO.getCategoryId());

        // Product 엔티티 객체 생성
        Product product = Product.builder()
                .title(productDTO.getTitle())
                .status(Status.valueOf(productDTO.getStatus()))  // Status는 Enum 타입으로 처리
                .sellPrice(productDTO.getSellPrice())
                .viewCount(productDTO.getViewCount())
                .description(productDTO.getDescription())
                .category(new Category(categoryDTO.getCategoryId(), categoryDTO.getName()))  // 카테고리 설정
                .build();

        // ProductImageDTO 리스트를 ProductImage 엔티티 리스트로 변환하여 설정
        if (productDTO.getImageDTOList() != null) {
            product.setImages(productDTO.getImageDTOList().stream()
                    .map(imageDTO -> new ProductImage(imageDTO.getImageId(), imageDTO.getImgName(), product))  // 이미지 객체 생성
                    .collect(Collectors.toList()));
        }

        // 생성된 Product 엔티티 반환
        return product;
    }
}
