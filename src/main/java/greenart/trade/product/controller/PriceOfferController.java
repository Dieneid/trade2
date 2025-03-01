package greenart.trade.product.controller;

import greenart.trade.product.dto.PriceOfferDTO;
import greenart.trade.product.dto.PriceWebSocketMessage;
import greenart.trade.product.service.PriceOfferService;
import greenart.trade.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Log4j2
public class PriceOfferController {

    private final PriceOfferService priceOfferService;
    private final ProductService productService;

    @MessageMapping("/price") // 발행 경로 : rec/price
    public PriceWebSocketMessage handlePriceOffer(PriceWebSocketMessage message) {
        Long productId = message.getProductId();
        Long memberId = message.getMemberId();
        String memberName = message.getMemberName();

        Long productOwnerId = productService.getProductOwnerId(productId);

        // 반환 메시지 생성
        PriceWebSocketMessage responseMessage = PriceWebSocketMessage.builder()
                .productId(productId)
                .memberId(memberId)
                .offeredPrice(message.getOfferedPrice())
                .memberName(memberName)
                .isAccept(false)
                .build();

        // 상품 등록자와 제시자가 동일한 경우
        if (productOwnerId.equals(memberId)) {
            log.warn("사용자가 자신의 상품에 가격을 제시하려고 함. memberId={}, productId={}", memberId, productId);
            responseMessage.setIsAccept(false);
            return responseMessage;
        }

        // 가격 범위 유효성 검사
        Long sellPrice = productService.getProductById(productId).getSellPrice();
        Long lowerLimit = Math.round(sellPrice * 0.9);
        if (message.getOfferedPrice() < lowerLimit || message.getOfferedPrice() > sellPrice) {
            log.warn("제시 가격이 허용 범위를 벗어남. memberId={}, offeredPrice={}, 범위: {} ~ {}",
                    memberId, message.getOfferedPrice(), lowerLimit, sellPrice);
            responseMessage.setIsAccept(false);
            return responseMessage;
        }

        // 가격 제시 처리
        PriceOfferDTO priceOffer = PriceOfferDTO.builder()
                .productId(productId)
                .memberId(memberId)
                .offeredPrice(message.getOfferedPrice())
                .createdAt(LocalDateTime.now())
                .build();
        priceOfferService.savePriceOffer(priceOffer);

        responseMessage.setIsAccept(true);
        return responseMessage;
    }
}
