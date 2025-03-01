package greenart.trade.product.service;

import greenart.trade.mebmer.entity.Member;
import greenart.trade.product.dto.PriceOfferDTO;
import greenart.trade.product.entity.PriceOffer;
import greenart.trade.product.entity.Product;
import greenart.trade.product.repository.PriceOfferRepository;
import greenart.trade.product.repository.ProductRepository;
import greenart.trade.mebmer.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PriceOfferService {

    private final PriceOfferRepository priceOfferRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    // Constructor injection for repositories
    public PriceOfferService(PriceOfferRepository priceOfferRepository,
                             ProductRepository productRepository,
                             MemberRepository memberRepository) {
        this.priceOfferRepository = priceOfferRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
    }

    // 가격 제시 저장 메서드
    public PriceOffer savePriceOffer(PriceOfferDTO dto) {
        // 상품과 회원을 조회
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // PriceOffer 엔티티 생성
        PriceOffer offer = PriceOffer.builder()
                .product(product)
                .member(member)
                .offeredPrice(dto.getOfferedPrice())
                .isAccept(false)  // 기본적으로 수락 여부는 false로 설정
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now()) // 생성일자 설정
                .build();

        // PriceOffer 저장
        return priceOfferRepository.save(offer);
    }

    // 가격 제시 수락 메서드
    public PriceOffer acceptOffer(Long offerId) {
        PriceOffer offer = priceOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        offer.setIsAccept(true);
        return priceOfferRepository.save(offer);
    }
}
