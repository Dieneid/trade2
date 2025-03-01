package greenart.trade.product.repository;

import greenart.trade.product.entity.PriceOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceOfferRepository extends JpaRepository<PriceOffer, Long> {
    List<PriceOffer> findByProduct_ProductId(Long productId);
}
