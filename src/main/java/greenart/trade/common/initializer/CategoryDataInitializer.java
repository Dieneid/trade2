package greenart.trade.common.initializer;

import greenart.trade.product.entity.Category;
import greenart.trade.product.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 카테고리 목록이 비어있으면 기본 카테고리 데이터를 추가
        if (categoryRepository.count() == 0) {
            addCategoryIfNotExist("운동기구");
            addCategoryIfNotExist("의류");
            addCategoryIfNotExist("전자제품");
            addCategoryIfNotExist("가구");
            addCategoryIfNotExist("도서");
            System.out.println("기본 카테고리 데이터가 초기화되었습니다.");
        }
    }

    private void addCategoryIfNotExist(String categoryName) {
        if (categoryRepository.findByName(categoryName).isEmpty()) {
            categoryRepository.save(new Category(categoryName));
        }
    }
}
