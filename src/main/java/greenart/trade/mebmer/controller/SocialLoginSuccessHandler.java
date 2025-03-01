package greenart.trade.mebmer.controller;

import greenart.trade.mebmer.dto.AuthDTO;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Optional;

@Component
public class SocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Autowired
    private MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        AuthDTO authDTO = (AuthDTO) authentication.getPrincipal();

        boolean fromSocial = authDTO.isFromSocial();
        Optional<Member> optionalMember = memberRepository.findByEmail(authDTO.getEmail());

        HttpSession session = request.getSession();
        String redirectUrl = (String) session.getAttribute("redirect"); // 이전 요청 URL 저장된 세션 값
        session.removeAttribute("redirect"); // 사용 후 제거

        if (fromSocial) {
            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();

                // 비활성화된 계정 처리
                if (!member.isEnabled()) {
                    // SecurityContext 초기화
                    SecurityContextHolder.clearContext();
                    request.getSession().invalidate();

                    // 비활성화된 계정 메시지와 함께 로그인 페이지로 리다이렉트
                    response.sendRedirect(request.getContextPath() + "/member/login?error=true&exception="
                            + URLEncoder.encode("탈퇴한 회원입니다. 이용하시려면 회원 복구를 해주시기 바랍니다.", "UTF-8"));
                    return;
                }

                // 전화번호 또는 주소가 없는 경우 추가 정보 입력 페이지로 이동
                if (member.getPhoneNumber() == null || member.getPhoneNumber().isEmpty() ||
                        member.getAddress() == null || member.getAddress().getCity().isEmpty()) {
                    redirectStrategy.sendRedirect(request, response, "/member/socialregister");
                    return;
                }
            } else {
                // 신규 사용자 -> 추가 정보 입력 페이지로 리다이렉트
                redirectStrategy.sendRedirect(request, response, "/member/socialregister");
                return;
            }
        }

        // 이전 요청 URL이 없는 경우 기본 경로로 설정
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            SavedRequest savedRequest = (SavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
            if (savedRequest != null) {
                redirectUrl = savedRequest.getRedirectUrl(); // SavedRequest에서 URL 가져오기
                session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
            } else {
                redirectUrl = "/"; // 기본 경로
            }
        }

        // 최종적으로 로그인 이전 URL로 리다이렉트
        redirectStrategy.sendRedirect(request, response, redirectUrl);
    }
}


