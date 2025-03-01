package greenart.trade.product.service;

import greenart.trade.product.dto.CategoryDTO;
import greenart.trade.product.entity.Category;
import greenart.trade.product.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 카테고리 수정
    @Transactional
    public void updateCategory(Long id, String newName) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        existingCategory.setName(newName); // 이름만 업데이트
        categoryRepository.save(existingCategory); // 저장
    }

    // 카테고리 조회 (DTO 반환)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        return new CategoryDTO(category.getCategoryId(), category.getName());
    }

    // 카테고리 추가
    public Category addCategory(String newCategoryName) {
        if (categoryRepository.existsByName(newCategoryName)) {
            throw new RuntimeException("이미 존재하는 카테고리입니다.");
        }
        Category category = new Category();
        category.setName(newCategoryName);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> new CategoryDTO(category.getCategoryId(), category.getName()))
                .toList();
    }

    // 카테고리 삭제
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
