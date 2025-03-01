package greenart.trade.product.controller;

import greenart.trade.product.dto.CategoryDTO;
import greenart.trade.product.entity.Category;
import greenart.trade.product.repository.CategoryRepository;
import greenart.trade.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @PostMapping("/category/add")
    public ResponseEntity<Map<String, Object>> addCategory(@RequestBody Map<String, String> request) {
        String newCategory = request.get("newCategory");

        // 카테고리 이름이 비어 있으면 Bad Request 반환
        if (newCategory == null || newCategory.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "카테고리 이름을 입력해주세요."));
        }

        Category category = categoryService.addCategory(newCategory);
        category.setName(newCategory);
        categoryRepository.save(category);

        // 성공적으로 저장된 카테고리 반환
        Map<String, Object> response = new HashMap<>();
        response.put("id", category.getCategoryId());
        response.put("name", category.getName());

        return ResponseEntity.ok(response);  // 클라이언트에 반환
    }


    // 카테고리 수정
    @PutMapping("/category/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable Long id, @RequestParam String name) {
        categoryService.updateCategory(id, name); // 이름만 전달
        return ResponseEntity.ok("카테고리가 수정되었습니다.");
    }

    @GetMapping("/category/all")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}
