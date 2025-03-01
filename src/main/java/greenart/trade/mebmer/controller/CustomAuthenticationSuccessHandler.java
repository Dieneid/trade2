package greenart.trade.mebmer.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();

        // 우선 SavedRequest 확인
        SavedRequest savedRequest = (SavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        String redirectUrl;

        if (savedRequest != null) {
            // SavedRequest가 있으면 해당 경로로 리다이렉트
            redirectUrl = savedRequest.getRedirectUrl();
            session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST"); // 사용 후 삭제
        } else {
            // 세션에 저장된 redirect 값을 확인
            redirectUrl = (String) session.getAttribute("redirect");
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                // redirect 값이 없으면 기본 경로로 설정
                redirectUrl = "/";
            } else {
                session.removeAttribute("redirect"); // 사용 후 삭제
            }
        }

        // 최종 리다이렉트 처리
        response.sendRedirect(redirectUrl);
    }
}



