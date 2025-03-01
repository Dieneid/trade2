package greenart.trade.product.repository;

import greenart.trade.product.entity.Category;
import greenart.trade.product.entity.Product;
import greenart.trade.product.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 카테고리와 검색어를 기준으로 최신순 정렬 (판매 완료 제외)
    Page<Product> findByCategoryAndTitleContainingAndStatusNotInAndEnabledTrueOrderByRefreshedAtDesc(
            Category category, String searchKeyword, List<Status> excludedStatuses, Pageable pageable);

    // 카테고리와 검색어를 기준으로 최신순 정렬 (판매 완료 포함)
    Page<Product> findByCategoryAndTitleContainingAndEnabledTrueOrderByRefreshedAtDesc(
            Category category, String searchKeyword, Pageable pageable);

    // 카테고리와 검색어를 기준으로 낮은 가격순 정렬 (판매 완료 제외)
    Page<Product> findByCategoryAndTitleContainingAndStatusNotInAndEnabledTrueOrderBySellPriceAsc(
            Category category, String searchKeyword, List<Status> excludedStatuses, Pageable pageable);

    // 카테고리와 검색어를 기준으로 낮은 가격순 정렬 (판매 완료 포함)
    Page<Product> findByCategoryAndTitleContainingAndEnabledTrueOrderBySellPriceAsc(
            Category category, String searchKeyword, Pageable pageable);

    // 카테고리 기준 최신순 정렬 (판매 완료 제외)
    Page<Product> findByCategoryCategoryIdAndStatusNotInAndEnabledTrueOrderByRefreshedAtDesc(
            Long categoryId, List<Status> excludedStatuses, Pageable pageable);

    // 카테고리 기준 최신순 정렬 (판매 완료 포함)
    Page<Product> findByCategoryCategoryIdAndEnabledTrueOrderByRefreshedAtDesc(
            Long categoryId, Pageable pageable);

    // 카테고리 기준 낮은 가격순 정렬 (판매 완료 제외)
    Page<Product> findByCategoryCategoryIdAndStatusNotInAndEnabledTrueOrderBySellPriceAsc(
            Long categoryId, List<Status> excludedStatuses, Pageable pageable);

    // 카테고리 기준 낮은 가격순 정렬 (판매 완료 포함)
    Page<Product> findByCategoryCategoryIdAndEnabledTrueOrderBySellPriceAsc(
            Long categoryId, Pageable pageable);

    // 검색어 기준 최신순 정렬 (판매 완료 제외)
    Page<Product> findByTitleContainingAndStatusNotInAndEnabledTrueOrderByRefreshedAtDesc(
            String searchKeyword, List<Status> excludedStatuses, Pageable pageable);

    // 검색어 기준 최신순 정렬 (판매 완료 포함)
    Page<Product> findByTitleContainingAndEnabledTrueOrderByRefreshedAtDesc(
            String searchKeyword, Pageable pageable);

    // 검색어 기준 낮은 가격순 정렬 (판매 완료 제외)
    Page<Product> findByTitleContainingAndStatusNotInAndEnabledTrueOrderBySellPriceAsc(
            String searchKeyword, List<Status> excludedStatuses, Pageable pageable);

    // 검색어 기준 낮은 가격순 정렬 (판매 완료 포함)
    Page<Product> findByTitleContainingAndEnabledTrueOrderBySellPriceAsc(
            String searchKeyword, Pageable pageable);

    // 전체 상품 최신순 정렬 (판매 완료 제외)
    Page<Product> findByStatusNotInAndEnabledTrueOrderByRefreshedAtDesc(
            List<Status> excludedStatuses, Pageable pageable);

    // 전체 상품 최신순 정렬 (판매 완료 포함)
    Page<Product> findAllByEnabledTrueOrderByRefreshedAtDesc(Pageable pageable);

    // 전체 상품 낮은 가격순 정렬 (판매 완료 제외)
    Page<Product> findByStatusNotInAndEnabledTrueOrderBySellPriceAsc(
            List<Status> excludedStatuses, Pageable pageable);

    // 전체 상품 낮은 가격순 정렬 (판매 완료 포함)
    Page<Product> findAllByEnabledTrueOrderBySellPriceAsc(Pageable pageable);


    List<Product> findTop12ByEnabledTrueAndStatusNotInOrderByRefreshedAtDesc(List<Status> excludedStatuses);


    // 특정 회원의 상품 조회 (페이징 지원)
    @Query("SELECT p FROM Product p WHERE p.member.memberId = :memberId AND p.enabled = true ORDER BY p.refreshedAt DESC")
    Page<Product> findByMemberIdAndEnabledTrueOrderByRefreshedAtDesc(
            @Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.member.memberId = :memberId AND p.enabled = true ORDER BY p.refreshedAt DESC")
    List<Product> findByMemberIdAndEnabledTrueOrderByRefreshedAtDesc(@Param("memberId") Long memberId);

}