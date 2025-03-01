package greenart.trade.common.controller;

import greenart.trade.product.dto.CategoryDTO;
import greenart.trade.product.service.CategoryService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {
    private final CategoryService categoryService;
    public GlobalControllerAdvice(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @ModelAttribute("categories")
    public List<CategoryDTO> populateCategories() {
        return categoryService.getAllCategories();
    }
}
