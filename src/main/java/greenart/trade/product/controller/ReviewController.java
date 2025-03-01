package greenart.trade.product.controller;


import greenart.trade.product.dto.ProductDTO;
import greenart.trade.product.dto.ReviewDTO;
import greenart.trade.product.entity.Product;
import greenart.trade.product.entity.Status;
import greenart.trade.product.repository.ProductRepository;
import greenart.trade.product.service.ProductService;
import greenart.trade.product.service.ReviewServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewServiceImpl reviewServiceImpl;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @GetMapping("/complete/{productId}")
    public String completePurchase(@PathVariable Long productId,Model model) {
        ProductDTO product = productService.getProductById(productId);
        model.addAttribute("product", product);
        String memberName = product.getMemberName();
        model.addAttribute("memberName", memberName);
        return "product/complete";
    }

    @PostMapping("/complete/{productId}")
    public ResponseEntity<Map<String, Object>> changeProductStatus(@PathVariable Long productId, Model model) {
        log.info("Received productId: {}", productId);
        ProductDTO product = productService.getProductById(productId);

        Map<String, Object> response = new HashMap<>();

        if (product != null) {
            // 상태를 '판매 완료'로 업데이트
            product.setStatus(Status.SOLDOUT.toString());
            productService.updateProductStatus(product);  // DB에 상태 업데이트

            // JSON 응답
            response.put("status", "success");
            response.put("message", "판매 완료되었습니다.");
            response.put("newStatus", product.getStatus());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Product with ID {} not found", productId);

            // 상품을 찾을 수 없을 때 JSON 응답
            response.put("status", "failure");
            response.put("message", "상품을 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/review/{productId}")
    public String createReview(@PathVariable Long productId, Model model) {
        Optional<Product> productOpt = productRepository.findById(productId);

        Product product = productOpt.get();
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setProductId(productId);

        model.addAttribute("reviewDTO", reviewDTO);
        model.addAttribute("product", product);

        return "product/review";
    }

    @PostMapping("/review/{productId}")
    public String saveReview(@PathVariable Long productId, @ModelAttribute ReviewDTO reviewDTO) {
        Optional<Product> productOpt = productRepository.findById(productId);

        Product product = productOpt.get();
        reviewServiceImpl.createReview(productId, reviewDTO);

        // memberId를 URL에 포함시켜 리다이렉트
        Long memberId = product.getMember().getMemberId();
        return "redirect:/member/memberpage/" + memberId;  // 정확한 memberId로 리다이렉트
    }
    @GetMapping("/check-login")
    public String checkLogin(@PathVariable Long productId) {
        return "/member/login";
    }

}
