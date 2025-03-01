package greenart.trade.mebmer.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        System.out.println("로그인 실패 ..................");

        String email = request.getParameter("email");

        String errorMessage;
        if (exception instanceof BadCredentialsException || exception instanceof InternalAuthenticationServiceException){
            errorMessage="아이디 또는 비밀번호가 맞지 않습니다.";
        }else if (exception instanceof UsernameNotFoundException){
            errorMessage="존재하지 않는 아이디 입니다.";
        }
        else {
            errorMessage="(회원 탈퇴 등)알 수 없는 이유로 로그인이 안되고 있습니다.";
        }
        setDefaultFailureUrl("/login?error=true&exception=" + errorMessage + "&email=" + email);
        errorMessage= URLEncoder.encode(errorMessage,"UTF-8");
        setDefaultFailureUrl("/member/login?error=true&exception=" + errorMessage + "&email=" + email);
        super.onAuthenticationFailure(request, response, exception);
    }
}
