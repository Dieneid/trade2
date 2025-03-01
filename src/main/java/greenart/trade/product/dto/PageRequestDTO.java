package greenart.trade.product.dto;

import lombok.*;
import org.springframework.data.domain.PageRequest;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

    @Builder
    @AllArgsConstructor
    @Setter
    @Getter
    @ToString
    public class PageRequestDTO {

        private int page; // 페이지 번호
        private int size; // 한 페이지에 표시할 게시물 수


        public PageRequestDTO(){
            this.page = 1; // 페이지번호 기본값
            this.size = 10; // 게시물 수의 기본값
        }

        public Pageable getPageable(Sort sort){
            return PageRequest.of(page -1, size, sort);
        }
    }
