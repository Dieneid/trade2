package greenart.trade.product.repository;

import greenart.trade.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name); // 카테고리 이름으로 중복 확인
    Optional<Category> findByName(String name);
}
