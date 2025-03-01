package greenart.trade.product.service;

import greenart.trade.mebmer.dto.AuthDTO;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.product.dto.ProductDTO;
import greenart.trade.product.entity.Category;
import greenart.trade.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    public Long register(ProductDTO productDTO, AuthDTO authDTO);  // 상품 등록

    ProductDTO getProductById(Long productId);  // 상품 ID로 조회 (ProductDTO로 수정)

    Product dtoToEntity(ProductDTO dto, Category category, Member member);  // DTO를 엔티티로 변환

    ProductDTO entityToDTO(Product product, Category category);  // 엔티티를 DTO로 변환

    Long getProductOwnerId(Long productId);

    public void incrementViewCount(Long productId);

    public void updateProductStatus(ProductDTO productDTO);

    // 페이징 지원 메서드 추가
    Page<ProductDTO> getFilteredProducts(Long categoryId, String searchKeyword, Pageable pageable, String sortOrder, Boolean includeSoldOut);
}
