package greenart.trade.product.controller;

import greenart.trade.mebmer.dto.AuthDTO;
import greenart.trade.mebmer.entity.BlackList;
import greenart.trade.mebmer.service.BlackListService;
import greenart.trade.product.dto.CategoryDTO;
import greenart.trade.product.dto.ProductDTO;
import greenart.trade.product.dto.ProductImageDTO;
import greenart.trade.product.entity.Category;
import greenart.trade.product.entity.Product;
import greenart.trade.product.entity.ProductImage;
import greenart.trade.product.entity.Status;
import greenart.trade.product.repository.CategoryRepository;
import greenart.trade.product.repository.ProductImageRepository;
import greenart.trade.product.repository.ProductRepository;
import greenart.trade.product.service.CategoryService;
import greenart.trade.product.service.FavoriteService;
import greenart.trade.product.service.ProductService;
import greenart.trade.product.service.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductServiceImpl productServiceImpl;
    private final ProductImageRepository productImageRepository;
    private final FavoriteService favoriteService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final BlackListService blackListService;
    private final HandlerMapping stompWebSocketHandlerMapping;

    @GetMapping("/list")
    public String productList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "false") Boolean includeSoldOut,
            @RequestParam(defaultValue = "latest") String sortOrder,
            Model model,
            @AuthenticationPrincipal AuthDTO authDTO) {

        Pageable pageable = PageRequest.of(page, size);

        // 카테고리 및 검색어로 필터링된 상품 목록 가져오기 (전체 목록을 먼저 가져옵니다)
        Page<ProductDTO> productPage = productService.getFilteredProducts(categoryId, searchKeyword, Pageable.unpaged(), sortOrder, includeSoldOut);
        List<ProductDTO> allProducts = productPage.getContent();  // 전체 상품 목록

        // 블랙리스트 대상 필터링 (로그인한 사용자에 대해)
        if (authDTO != null) {
            List<BlackList> blackList = blackListService.getBlackListByMemberId(authDTO.getMemberId());
            Set<String> blackListedMemberNames = blackList.stream()
                    .map(BlackList::getBlackName)
                    .collect(Collectors.toSet());

            // 블랙리스트 대상의 상품 필터링
            allProducts = allProducts.stream()
                    .filter(product -> !blackListedMemberNames.contains(product.getMemberName()))
                    .collect(Collectors.toList());
        }

        // 필터링된 상품을 페이징 처리
        int start = Math.min(page * size, allProducts.size());
        int end = Math.min((page + 1) * size, allProducts.size());
        List<ProductDTO> filteredProducts = allProducts.subList(start, end);

        // 필터링된 전체 상품 수 및 페이지 수 계산
        long totalFilteredElements = allProducts.size();
        int totalFilteredPages = Math.max(1, (int) Math.ceil((double) totalFilteredElements / size));

        // 새로운 페이지 객체 생성
        productPage = new PageImpl<>(filteredProducts, pageable, totalFilteredElements);

        // 모델에 데이터 전달
        model.addAttribute("productPage", productPage);  // 페이징된 데이터 전달
        model.addAttribute("products", filteredProducts); // 현재 페이지 데이터 전달
        model.addAttribute("currentPage", totalFilteredElements == 0 ? 0 : page); // 데이터가 없으면 0 페이지
        model.addAttribute("totalPages", totalFilteredPages);  // 최소 1 보장
        model.addAttribute("totalElements", totalFilteredElements);  // 필터링된 전체 상품 수

        // 선택된 필터 값을 모델에 추가
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("includeSoldOut", includeSoldOut);

        // 카테고리 목록 추가
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // 상품 이미지 추가
        List<ProductDTO> productDTOList = productServiceImpl.getAllProducts();
        model.addAttribute("productDTOList", productDTOList);

        model.addAttribute("authDTO", authDTO);

        // 각 상품에 대해 사용자의 찜 여부 확인
        if (authDTO != null) {
            Long memberId = authDTO.getMemberId();
            Map<Long, Boolean> favoriteStatusMap = productPage.getContent().stream()
                    .collect(Collectors.toMap(
                            ProductDTO::getProductId,
                            product -> favoriteService.isFavorited(product.getProductId(), memberId)
                    ));
            model.addAttribute("favoriteStatusMap", favoriteStatusMap);
        }

        return "product/list";  // 필터링된 상품 목록 페이지 반환
    }

    // 이미지 파일을 서빙하는 메서드
    @GetMapping("/images/{imageId}")
    public ResponseEntity<Resource> getImage(@PathVariable Long imageId) {
        ProductImage productImage = productImageRepository.findById(imageId).orElseThrow();
        File file = new File("C:/upload/" + productImage.getImgName());
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok().body(resource);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/register")
    public String registerForm(Model model, @AuthenticationPrincipal AuthDTO authDTO) {
        if (authDTO == null) {
            throw new RuntimeException("로그인된 사용자가 없습니다.");
        }
        model.addAttribute("member", authDTO); // 사용자 정보 전달
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product/register";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/register")
    public String register(
            @ModelAttribute("product") @Validated ProductDTO productDTO,
            BindingResult bindingResult, Model model, RedirectAttributes rttr,
            @RequestParam("fileInput") List<MultipartFile> files,
            @RequestParam("imageOrder") String imageOrder, // 순서 데이터 추가
            @AuthenticationPrincipal AuthDTO authDTO) {

        // 유효성 검증
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories()); // 카테고리 목록 추가
            model.addAttribute("product", productDTO); // 입력된 데이터 유지
            model.addAttribute("validationErrors", bindingResult.getAllErrors()); // 에러 정보 추가
            return "/product/register";
        }

        // 멤버 정보 설정
        if (authDTO != null) {
            productDTO.setMemberId(authDTO.getMemberId());
            productDTO.setMemberName(authDTO.getName()); // 멤버 이름 설정
        }

        // 이미지 파일 처리
        List<ProductImageDTO> imageDTOList = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) { // 파일이 있을 때만 처리
                    ProductImageDTO imageDTO = productServiceImpl.saveImage(file);
                    imageDTOList.add(imageDTO);
                }
            }
        }

        // 이미지 순서 설정
        if (!imageOrder.isEmpty()) {
            String[] order = imageOrder.split(",");
            List<ProductImageDTO> orderedList = new ArrayList<>();
            for (String o : order) {
                int index = Integer.parseInt(o);
                if (index >= 0 && index < imageDTOList.size()) {
                    orderedList.add(imageDTOList.get(index));
                }
            }
            productDTO.setImageDTOList(orderedList);
        } else {
            productDTO.setImageDTOList(imageDTOList);
        }

        // 상품 등록
        Long productId = productService.register(productDTO, authDTO);
        rttr.addFlashAttribute("msg", productId + "번 상품이 등록되었습니다.");
        return "redirect:/product/list";
    }

    @GetMapping("/read/{productId}")
    public String readProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal AuthDTO authDTO,
            Model model
    ) {
        // 상품 정보 조회
        ProductDTO product = productService.getProductById(productId);
        if (product == null) {
            model.addAttribute("error", "Product not found");
            return "error/404"; // 존재하지 않는 상품에 대한 에러 페이지
        }

        // 카테고리 이름 조회 후 추가
        CategoryDTO category = categoryService.getCategoryById(product.getCategoryId());
        if (category != null) {
            model.addAttribute("categoryName", category.getName());
        }

        // 조회수 증가
        productService.incrementViewCount(productId);

        // 상품 가격 포맷팅
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedSellPrice = decimalFormat.format(product.getSellPrice());
        model.addAttribute("formattedSellPrice", formattedSellPrice);

        // 인증 여부에 따라 모델에 추가
        if (authDTO != null) {
            Long currentMemberId = authDTO.getMemberId();
            boolean isOwner = product.getMemberId().equals(currentMemberId);

            model.addAttribute("memberName", authDTO.getMemberName());
            model.addAttribute("memberId", currentMemberId);
            model.addAttribute("isOwner", isOwner);
            Map<Long, Boolean> favoriteStatusMap = new HashMap<>();
            Long memberId = authDTO.getMemberId();

            boolean isFavorited = favoriteService.isFavorited(product.getProductId(), memberId);

            // Map에 상품 ID와 찜 상태 추가
            favoriteStatusMap.put(product.getProductId(), isFavorited);
            model.addAttribute("favoriteStatusMap", favoriteStatusMap);
            model.addAttribute("authDTO", authDTO);
        } else {
            model.addAttribute("memberName", null);
            model.addAttribute("isOwner", false);  // 인증되지 않은 사용자는 "isOwner"를 false로 설정
        }

        model.addAttribute("product", product);
        model.addAttribute("images", product.getImageDTOList()); // 이미지 정보 추가
        return "product/read"; // 상세보기 뷰 이름
    }

    @PostMapping("/read/{productId}")
    public String refreshProduct(@PathVariable Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setRefreshedAt(LocalDateTime.now()); // 현재 시간으로 갱신
            productRepository.save(product); // 갱신된 값 저장
        }
        return "redirect:/product/list"; // 삭제 후 목록 페이지로 이동
    }

    @PostMapping("/reserved/{productId}")
    public String reservedProduct(@PathVariable Long productId) {
        ProductDTO product = productService.getProductById(productId);

        if (product != null) {
            // Enum 값으로 직접 비교
            Status currentStatus = Status.valueOf(product.getStatus());

            if (currentStatus == Status.NEW) {
                product.setStatus(Status.RESERVED.toString());
            } else if (currentStatus == Status.RESERVED) {
                product.setStatus(Status.NEW.toString());
            }

            productService.updateProductStatus(product); // 상태 업데이트
        }

        return "redirect:/product/list";
    }

    // 이미지 파일을 서빙하는 메서드
    @GetMapping("/display")
    public ResponseEntity<Resource> displayImage(@RequestParam String fileName) {
        // 이미지 파일 경로 처리
        Path path = Paths.get("C:/upload/" + fileName);  // 파일이 저장된 실제 경로
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 이미지 형식에 맞는 content-type 설정
                .body(resource);
    }

    // 찜 상태 토글 처리
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/favorite/{productId}")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@PathVariable Long productId, @AuthenticationPrincipal AuthDTO authDTO) {
        Long favoriteCount;
        boolean isFavorited = false; // 기본값: 찜하지 않음

        if (authDTO == null) {
            // 로그인하지 않은 경우
            favoriteCount = favoriteService.getFavoriteCount(productId);
        } else {
            // 로그인한 경우 찜 상태 토글 처리
            isFavorited = favoriteService.toggleFavorite(productId, authDTO);
            favoriteCount = favoriteService.getFavoriteCount(productId);
        }

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("isFavorited", isFavorited);
        response.put("favoriteCount", favoriteCount);
        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/favorite")
    public String favoriteList(@AuthenticationPrincipal AuthDTO authDTO, Model model) {
        if (authDTO == null) {
            return "redirect:/login";
        }

        List<Product> favoriteProducts = favoriteService.getFavoritesByMemberId(authDTO);

        List<ProductDTO> productDTOList = productServiceImpl.getAllProducts();
        model.addAttribute("productDTOList", productDTOList);

        model.addAttribute("favoriteProducts", favoriteProducts);
        return "product/favorite";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/favorite/{productId}/remove")
    public ResponseEntity<String> removeFavorite(@PathVariable Long productId, @AuthenticationPrincipal AuthDTO authDTO) {
        try {
            favoriteService.removeFavorite(productId, authDTO.getMemberId());
            return ResponseEntity.ok("{\"success\": true}");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"success\": false, \"message\": \"찜 삭제에 실패했습니다.\"}");
        }
    }

    @GetMapping("/edit/{productId}")
    public String editProduct(@PathVariable Long productId, Model model) {

        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isEmpty()) {
            return "redirect:/product/list";  // 상품이 없으면 목록으로 리다이렉트
        }
        Product product = productOpt.get();
        List<Category> categories = categoryRepository.findAll();
        ProductDTO productDTO = productServiceImpl.entitytoDTO2(product);

        List<String> imageUrls = product.getImages().stream()
                .map(image -> "/upload/" + image.getImgName())
                .collect(Collectors.toList());

        model.addAttribute("product", productDTO);
        model.addAttribute("categories", categories);
        model.addAttribute("imageUrls", imageUrls);

        return "product/edit";
    }

    @PostMapping("/edit/{productId}")
    public String updateProduct(
            @PathVariable Long productId,
            @ModelAttribute("product") @Validated ProductDTO productDTO,
            BindingResult bindingResult, Model model, RedirectAttributes rttr,
            @RequestParam(value = "fileInput", required = false) List<MultipartFile> files,
            @RequestParam("imageOrder") String imageOrder, // 순서 데이터 추가
            @AuthenticationPrincipal AuthDTO authDTO) {

        // 유효성 검증
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("product", productDTO);
            return "/product/edit";
        }

        Product productEntity = productServiceImpl.getProductEntityById(productId); // 실제 엔티티 조회

        // 파일이 실제로 추가되었는지 확인
        boolean hasValidFiles = files != null && files.stream().anyMatch(file -> !file.isEmpty());

        if (hasValidFiles) {
            // 기존 이미지 삭제 및 연관 관계 제거
            if (!productEntity.getImages().isEmpty()) {
                for (ProductImage image : productEntity.getImages()) {
                    productServiceImpl.deleteImage(image.getImgName()); // 실제 파일 삭제
                    image.setProduct(null); // 연관 관계 제거
                }
                productEntity.getImages().clear(); // 엔티티의 컬렉션 초기화
            }

            // 새로운 이미지 추가 (순서대로 처리)
            String[] orderArray = imageOrder.split(","); // 순서대로 분리
            List<MultipartFile> filesList = new ArrayList<>(files); // 파일 리스트를 ArrayList로 변환

            for (int i = 0; i < orderArray.length; i++) {
                int index = Integer.parseInt(orderArray[i]); // 순서 배열에서 해당 순서 가져오기
                if (index >= 0 && index < filesList.size()) { // 유효한 인덱스인지 확인
                    MultipartFile file = filesList.get(index); // 순서에 맞는 파일을 가져옴
                    if (!file.isEmpty()) { // 파일이 있을 때만 처리
                        ProductImage newImage = productServiceImpl.saveImageEntity(file);
                        newImage.setProduct(productEntity); // 연관 관계 설정
                        productEntity.getImages().add(newImage); // 이미지 추가
                    }
                }
            }
        }

        // 다른 필드 업데이트 (예: 제목, 설명 등)
        productEntity.setTitle(productDTO.getTitle());
        productEntity.setCategory(categoryRepository.findById(productDTO.getCategoryId()).get());
        productEntity.setSellPrice(productDTO.getSellPrice());
        productEntity.setDescription(productDTO.getDescription());

        // 엔티티 업데이트
        productRepository.save(productEntity);

        rttr.addFlashAttribute("msg", "상품이 수정되었습니다.");
        return "redirect:/product/read/" + productId;
    }

    @GetMapping("/delete/{productId}")
    public String deleteProduct(@PathVariable Long productId, Model model) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(productId);

        Product productEntity = productServiceImpl.getProductEntityById(productId); // 실제 엔티티 조회

        if (!productEntity.getImages().isEmpty()) {
            for (ProductImage image : productEntity.getImages()) {
                productServiceImpl.deleteImage(image.getImgName()); // 실제 파일 삭제
            }
        }

        productServiceImpl.disableProduct(productDTO);

        model.addAttribute("productDTO", productDTO);
        return "redirect:/product/list";
    }
}