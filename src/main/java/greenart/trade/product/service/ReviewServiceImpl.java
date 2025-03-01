package greenart.trade.product.service;

import greenart.trade.mebmer.dto.AuthDTO;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.product.dto.ReviewDTO;
import greenart.trade.product.entity.Evaluation;
import greenart.trade.product.entity.Product;
import greenart.trade.product.entity.Review;
import greenart.trade.product.repository.EvaluationRepository;
import greenart.trade.product.repository.ProductRepository;
import greenart.trade.product.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final EvaluationRepository evaluationRepository;

    @Transactional
    @Override
    public void createReview(Long productId, ReviewDTO reviewDTO) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthDTO authDTO = (AuthDTO) authentication.getPrincipal();
        String username = authDTO.getMemberName();

        Member member = product.getMember();

        Review review = new Review();
        review.setMessage(reviewDTO.getMessage());
        review.setProduct(product);
        review.setMember(member);
        review.setAuthorUsername(username);

        reviewRepository.save(review);

        Evaluation evaluation = new Evaluation();
        evaluation.setReview(review);
        evaluation.setScore(reviewDTO.getEvaluationDTO().getScore());
        evaluationRepository.save(evaluation);
    }

    // 특정 memberId에 해당하는 리뷰 목록을 조회
    public List<Review> findReviewsByMemberId(Long memberId) {
        return reviewRepository.findByMemberMemberId(memberId);
    }
}
