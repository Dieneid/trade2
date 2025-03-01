package greenart.trade.product.repository;

import greenart.trade.product.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMemberMemberId(Long memberId);
}
