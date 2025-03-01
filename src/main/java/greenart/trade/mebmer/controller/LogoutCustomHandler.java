package greenart.trade.mebmer.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogoutCustomHandler implements LogoutSuccessHandler {


    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Referer 헤더 확인
        String referer = request.getHeader("Referer");

        if (referer != null && referer.contains("/member/mypage")) {
            response.sendRedirect("/main");
        } else if (referer != null && referer.contains("/member/update")) {
            response.sendRedirect("/main");

        } else if (referer != null && referer.contains("/product/favorite")) {
            response.sendRedirect("/main");

        } else if (referer != null) {
            // Referer가 있는 경우 해당 페이지로 이동
            response.sendRedirect(referer);
        } else {
            // Referer가 없을 경우 기본 로그인 페이지로 이동
            response.sendRedirect("/login?logout");
        }
    }
}
