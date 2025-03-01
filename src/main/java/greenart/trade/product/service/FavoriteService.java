package greenart.trade.product.service;

import greenart.trade.mebmer.dto.AuthDTO;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.repository.MemberRepository;
import greenart.trade.product.entity.Favorite;
import greenart.trade.product.entity.Product;
import greenart.trade.product.repository.FavoriteRepository;
import greenart.trade.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;

    // 찜 상태 토글 메서드
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public boolean toggleFavorite(Long productId, @AuthenticationPrincipal AuthDTO authDTO) {
        // 로그인된 사용자 정보 가져오기
        Member member = memberRepository.findById(authDTO.getMemberId())
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
        // 상품 정보 가져오기
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품 정보를 찾을 수 없습니다."));

        if (product.getFavoriteCount() == null) {
            product.setFavoriteCount(0L);  // null인 경우 0으로 초기화
        }
        // 이미 찜 목록에 있으면 찜 제거, 없으면 찜 목록에 추가
        Favorite existingFavorite = favoriteRepository.findByMemberAndProduct(member, product);
        if (existingFavorite != null) {
            // 찜 목록에서 제거
            favoriteRepository.delete(existingFavorite);
            product.setFavoriteCount(product.getFavoriteCount() - 1);
            productRepository.save(product);
            return false; // 찜 해제 상태 반환
        } else {
            // 새로운 찜 항목 생성 후 저장
            Favorite newFavorite = new Favorite(member, product);
            favoriteRepository.save(newFavorite);
            product.setFavoriteCount(product.getFavoriteCount() + 1);
            productRepository.save(product);
            return true; // 찜 상태 반환
        }
    }
    // 찜한 상품인지 확인
    public boolean isFavorited(Long productId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();
        return favoriteRepository.findByMemberAndProduct(member, product) != null;
    }

    public Long getFavoriteCount(Long productId) {
        // 상품 정보 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품 정보를 찾을 수 없습니다."));
        // 찜한 사용자 수 반환
        return favoriteRepository.countByProduct(product);
    }

    // 로그인된 사용자의 찜 목록 조회
    public List<Product> getFavoritesByMemberId(AuthDTO authDTO) {
        Member member = memberRepository.findById(authDTO.getMemberId()).orElseThrow();

        // 회원의 찜 목록을 Favorite 엔티티로 조회하고, 해당 상품(Product)을 추출하여 반환
        List<Favorite> favorites = favoriteRepository.findAllByMember(member);

        // Favorite 리스트에서 Product 리스트로 변환하여 반환
        return favorites.stream()
                .map(Favorite::getProduct) // Favorite 객체에서 Product 추출
                .collect(Collectors.toList()); // Product 리스트로 수집
    }
    // 찜 삭제 메서드
    @Transactional
    public void removeFavorite(Long productId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품 정보를 찾을 수 없습니다."));

        // 해당 상품에 대한 찜 항목을 찾고 삭제
        Favorite favorite = favoriteRepository.findByMemberAndProduct(member, product);
        if (favorite != null) {
            favoriteRepository.delete(favorite); // 찜 목록에서 해당 항목 제거
            product.setFavoriteCount(product.getFavoriteCount() - 1);
            productRepository.save(product);
        }
    }
}


