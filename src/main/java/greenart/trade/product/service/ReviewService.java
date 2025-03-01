package greenart.trade.product.service;

import greenart.trade.product.dto.ReviewDTO;
import jakarta.transaction.Transactional;

public interface ReviewService {

    @Transactional
    void createReview(Long ProductId, ReviewDTO reviewDTO);
}
