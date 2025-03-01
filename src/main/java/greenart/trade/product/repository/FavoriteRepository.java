package greenart.trade.product.repository;

import greenart.trade.mebmer.entity.Member;
import greenart.trade.product.entity.Favorite;
import greenart.trade.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    // 회원과 상품으로 찜 항목을 찾기
    Favorite findByMemberAndProduct(Member member, Product product);

    // 특정 회원의 찜 목록을 모두 가져오기
    List<Favorite> findAllByMember(Member member);

    // 특정 상품에 대한 찜한 사용자 수 조회
    Long countByProduct(Product product);
}
