package greenart.trade.mebmer.controller;

import greenart.trade.mebmer.dto.AuthDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/api/user") // 반드시 이 경로로 매핑
    public Map<String, Object> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("사용자 정보가 없습니다.");
        }

        // 사용자 정보를 가져와서 응답
        Object principal = authentication.getPrincipal();
        AuthDTO authDTO = (AuthDTO) principal;

        Map<String, Object> memberInfo = new HashMap<>();
        memberInfo.put("MemberId", authDTO.getMemberId());
        memberInfo.put("MemberName", authDTO.getMemberName());
        return memberInfo;
    }


}