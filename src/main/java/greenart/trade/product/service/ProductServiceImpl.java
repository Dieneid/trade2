package greenart.trade.product.service;

import greenart.trade.mebmer.dto.AuthDTO;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.repository.MemberRepository;
import greenart.trade.product.dto.ProductDTO;
import greenart.trade.product.dto.ProductImageDTO;
import greenart.trade.product.entity.Category;
import greenart.trade.product.entity.Product;
import greenart.trade.product.entity.ProductImage;
import greenart.trade.product.entity.Status;
import greenart.trade.product.repository.CategoryRepository;
import greenart.trade.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    // 상품 등록
    @Transactional
    @Override
    public Long register(ProductDTO productDTO, AuthDTO authDTO) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        Member loggedInMember = memberRepository.findByEmail(authDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("회원 정보가 없습니다."));

        // 로그인된 사용자가 없으면 예외 처리
        if (loggedInMember == null) {
            throw new RuntimeException("로그인된 사용자가 없습니다.");
        }

        Product product = Product.builder()
                .title(productDTO.getTitle())
                .description(productDTO.getDescription())
                .sellPrice(productDTO.getSellPrice())
                .category(category)
                .refreshedAt(LocalDateTime.now())
                .viewCount(0L) // 기본값 설정
                .member(loggedInMember)  // 로그인된 회원을 상품에 연결
                .favoriteCount(0L)
                .build();

        List<ProductImage> productImages = new ArrayList<>();

        // imageDTOList를 순회하면서 각 ProductImageDTO를 imageDTO로 처리
        for (ProductImageDTO imageDTO : productDTO.getImageDTOList()) {
            ProductImage productImage = new ProductImage();

            productImage.setImgName(imageDTO.getImgName()); // ProductImageDTO에서 이미지 이름 가져오기
            productImage.setProduct(product);  // 제품에 연결

            productImages.add(productImage);
        }

        product.setImages(productImages);

        return productRepository.saveAndFlush(product).getProductId();
    }

    private List<ProductImageDTO> getProductImageDTOList(Product product) {
        // Product의 이미지 리스트를 순회하여 ProductImageDTO로 변환
        return product.getImages().stream()
                .map(image -> new ProductImageDTO(image.getImageId(), image.getImgName()))  // ProductImage에서 필요한 데이터를 추출하여 ProductImageDTO 객체로 변환
                .collect(Collectors.toList());  // 변환된 DTO 객체들을 리스트로 모아서 반환
    }

    private ProductDTO convertToDTO(Product product) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInName = null;

        // 로그인된 사용자가 있다면 그 사용자 이름을 가져옴
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Member) {
                Member member = (Member) principal;
                loggedInName = member.getName();
            }
        }

        // ProductDTO 변환 과정
        return ProductDTO.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .imageDTOList(getProductImageDTOList(product))
                .description(product.getDescription())
                .sellPrice(product.getSellPrice())
                .categoryId(product.getCategory().getCategoryId())
                .status(product.getStatus().toString())
                .viewCount(product.getViewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .refreshedAt(product.getRefreshedAt())
                .memberId(product.getMember() != null ? product.getMember().getMemberId() : null) // member가 null일 경우 null로 설정
                .memberName(product.getMember() != null ? product.getMember().getName() : "익명") // member가 null일 경우 "익명"으로 설정
                .favoriteCount(product.getFavoriteCount())
                .build();
    }


    @Override
    public ProductDTO entityToDTO(Product product, Category category) {
        return ProductDTO.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .imageDTOList(getProductImageDTOList(product))
                .description(product.getDescription())
                .categoryId(category.getCategoryId())
                .status(product.getStatus().toString())
                .sellPrice(product.getSellPrice())
                .viewCount(product.getViewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .refreshedAt(product.getRefreshedAt())
                .memberId(product.getMember() != null ? product.getMember().getMemberId() : null)
                .memberName(product.getMember() != null ? product.getMember().getName() : "익명") // 작성자 이름 설정
                .favoriteCount(product.getFavoriteCount())
                .build();
    }

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public Long getProductOwnerId(Long productId) {
        // 상품을 ID로 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        // 상품의 소유자 ID 반환
        return product.getMember().getMemberId();
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return entityToDTO(product, product.getCategory());
    }

    @Override
    public Product dtoToEntity(ProductDTO dto, Category category, Member member) {
        return Product.builder()
                .title(dto.getTitle())
                .status(Status.valueOf(dto.getStatus()))
                .sellPrice(dto.getSellPrice())
                .viewCount(dto.getViewCount() != null ? dto.getViewCount() : 0L)
                .description(dto.getDescription())
                .category(category)
                .refreshedAt(LocalDateTime.now())
                .member(member)
                .build();
    }

    @Override
    public void incrementViewCount(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setViewCount(product.getViewCount() + 1); // 조회수 증가
        productRepository.save(product); // DB에 저장
    }

    // 상태 업데이트 메서드
    @Transactional
    public void updateProductStatus(ProductDTO productDTO) {
        // DTO -> 엔티티 변환
        Product product = productRepository.findById(productDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        // 상태 변경
        product.setStatus(Status.valueOf(productDTO.getStatus()));

        // DB에 저장
        productRepository.save(product);
    }

    @Override
    public Page<ProductDTO> getFilteredProducts(Long categoryId, String searchKeyword, Pageable pageable, String sortOrder, Boolean includeSoldOut) {
        Page<Product> productPage;

        // 기본 정렬 기준: 최신순
        boolean orderByPriceAsc = "priceAsc".equals(sortOrder);
        List<Status> excludedStatuses = includeSoldOut ? null : List.of(Status.SOLDOUT, Status.RESERVED); // 둘 다 제외

        if (categoryId != null && searchKeyword != null && !searchKeyword.isEmpty()) {
            // 카테고리 ID와 검색어가 모두 있을 때
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

            if (orderByPriceAsc) {
                // 가격순 정렬
                productPage = (includeSoldOut)
                        ? productRepository.findByCategoryAndTitleContainingAndEnabledTrueOrderBySellPriceAsc(category, searchKeyword, pageable)
                        : productRepository.findByCategoryAndTitleContainingAndStatusNotInAndEnabledTrueOrderBySellPriceAsc(category, searchKeyword, excludedStatuses, pageable);
            } else {
                // 최신순 정렬
                productPage = (includeSoldOut)
                        ? productRepository.findByCategoryAndTitleContainingAndEnabledTrueOrderByRefreshedAtDesc(category, searchKeyword, pageable)
                        : productRepository.findByCategoryAndTitleContainingAndStatusNotInAndEnabledTrueOrderByRefreshedAtDesc(category, searchKeyword, excludedStatuses, pageable);
            }
        } else if (categoryId != null) {
            // 카테고리 ID만 있을 때
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

            if (orderByPriceAsc) {
                // 가격순 정렬
                productPage = (includeSoldOut)
                        ? productRepository.findByCategoryCategoryIdAndEnabledTrueOrderBySellPriceAsc(categoryId, pageable)
                        : productRepository.findByCategoryCategoryIdAndStatusNotInAndEnabledTrueOrderBySellPriceAsc(categoryId, excludedStatuses, pageable);
            } else {
                // 최신순 정렬
                productPage = (includeSoldOut)
                        ? productRepository.findByCategoryCategoryIdAndEnabledTrueOrderByRefreshedAtDesc(categoryId, pageable)
                        : productRepository.findByCategoryCategoryIdAndStatusNotInAndEnabledTrueOrderByRefreshedAtDesc(categoryId, excludedStatuses, pageable);
            }
        } else if (searchKeyword != null && !searchKeyword.isEmpty()) {
            // 검색어만 있을 때
            if (orderByPriceAsc) {
                // 가격순 정렬
                productPage = (includeSoldOut)
                        ? productRepository.findByTitleContainingAndEnabledTrueOrderBySellPriceAsc(searchKeyword, pageable)
                        : productRepository.findByTitleContainingAndStatusNotInAndEnabledTrueOrderBySellPriceAsc(searchKeyword, excludedStatuses, pageable);
            } else {
                // 최신순 정렬
                productPage = (includeSoldOut)
                        ? productRepository.findByTitleContainingAndEnabledTrueOrderByRefreshedAtDesc(searchKeyword, pageable)
                        : productRepository.findByTitleContainingAndStatusNotInAndEnabledTrueOrderByRefreshedAtDesc(searchKeyword, excludedStatuses, pageable);
            }
        } else {
            // 카테고리 ID와 검색어가 없을 때
            if (orderByPriceAsc) {
                // 가격순 정렬
                productPage = (includeSoldOut)
                        ? productRepository.findAllByEnabledTrueOrderBySellPriceAsc(pageable)
                        : productRepository.findByStatusNotInAndEnabledTrueOrderBySellPriceAsc(excludedStatuses, pageable);
            } else {
                // 최신순 정렬
                productPage = (includeSoldOut)
                        ? productRepository.findAllByEnabledTrueOrderByRefreshedAtDesc(pageable)
                        : productRepository.findByStatusNotInAndEnabledTrueOrderByRefreshedAtDesc(excludedStatuses, pageable);
            }
        }


        return productPage.map(this::convertToDTO);
    }

    public ProductImageDTO saveImage(MultipartFile file) {
        try {
            String uploadDir = "C:/upload/"; // 실제 저장 디렉토리
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + File.separator + fileName;

            // 디렉토리가 없으면 디렉토리 생성
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file.transferTo(new File(filePath));

            return ProductImageDTO.builder()
                    .imgName(fileName)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }

    public ProductImage saveImageEntity(MultipartFile file) {
        try {
            String uploadDir = "C:/upload/"; // 실제 저장 디렉토리
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + File.separator + fileName;

            // 디렉토리가 없으면 디렉토리 생성
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 파일 저장
            file.transferTo(new File(filePath));

            // ProductImage 엔티티 생성
            return ProductImage.builder()
                    .imgName(fileName)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }

    public ProductDTO entitytoDTO2(Product product) {
        return ProductDTO.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .sellPrice(product.getSellPrice())
                .viewCount(product.getViewCount())
                .description(product.getDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .refreshedAt(product.getRefreshedAt())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)  // Category가 null이 아닐 경우 categoryId 설정
                .memberId(product.getMember() != null ? product.getMember().getMemberId() : null)  // Member가 null이 아닐 경우 memberId 설정
                .memberName(product.getMember() != null ? product.getMember().getName() : null)  // Member가 null이 아닐 경우 memberName 설정
                .firstImageId(product.getFirstImage() != null ? product.getFirstImage().getImageId() : null)  // 첫 번째 이미지 ID 설정
                .build();  // 빌더 패턴을 사용하여 ProductDTO 객체 생성
    }

    public Product getProductEntityById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 상품이 존재하지 않습니다: " + productId));
    }

    // 이미지 삭제 메서드
    public void deleteImage(String fileName) {
        String uploadDir = "C:/upload/";
        File file = new File(uploadDir + File.separator + fileName);
        if (file.exists()) {
            file.delete(); // 파일 삭제
        }
    }

    public void disableProduct(ProductDTO productDTO){
        Product product = productRepository.findById(productDTO.getProductId()).get();
        product.setEnabled(false);
        productRepository.save(product);
    }
}

