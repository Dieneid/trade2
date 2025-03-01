package greenart.trade.common.controller;

import greenart.trade.product.dto.ProductDTO;
import greenart.trade.product.entity.Status;
import greenart.trade.product.repository.ProductRepository;
import greenart.trade.product.service.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductRepository productRepository;
    private final ProductServiceImpl productServiceImpl;

    @GetMapping({"/","/main"})
    public String main(Model model) {
        List<Status> excludeStatuses = Arrays.asList(Status.SOLDOUT, Status.RESERVED);
        model.addAttribute("products", productRepository.findTop12ByEnabledTrueAndStatusNotInOrderByRefreshedAtDesc(excludeStatuses));

        List<ProductDTO> productDTOList = productServiceImpl.getAllProducts();
        model.addAttribute("productDTOList", productDTOList);

        return "main";
    }
}
