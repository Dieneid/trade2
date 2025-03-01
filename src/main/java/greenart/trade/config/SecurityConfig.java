package greenart.trade.config;

import greenart.trade.mebmer.controller.CustomAuthenticationSuccessHandler;
import greenart.trade.mebmer.controller.LoginFailureHandler;
import greenart.trade.mebmer.controller.LogoutCustomHandler;
import greenart.trade.mebmer.controller.SocialLoginSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    LoginFailureHandler loginFailureHandler;

    @Autowired
    LogoutCustomHandler logoutCustomHandler;

    @Autowired
    SocialLoginSuccessHandler socialLoginSuccessHandler;

    @Autowired
    CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        // 특정 URL 패턴은 모든 사용자에게 허용
                        .requestMatchers("/uploads/**", "/upload/image", "/member/login", "/member/logout","/product/favorite/**","member/check-emails").permitAll()
                        // 로그인된 사용자만 경로에 접근할 수 있도록 설정
                        .requestMatchers("/product/register","/member/messagePage","/sendMessage","/product/complete/**","member/mypage",
                                "member/update","member/password", "chat/**").authenticated()
                        // 그 외 나머지 요청은 모두 허용
                        .anyRequest().permitAll())
                .formLogin(login -> login
                        .loginPage("/member/login")  // 로그인 페이지 설정
                        .loginProcessingUrl("/member/login")  // 로그인 처리 URL 설정
                        .usernameParameter("email")  // 로그인 시 사용할 파라미터 이름
                        .failureHandler(loginFailureHandler)  // 로그인 실패 시 처리할 핸들러 설정
                        .successHandler(customAuthenticationSuccessHandler))// 로그인 성공 후 리디렉션 처리
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessHandler(logoutCustomHandler) // Bean으로 관리되는 핸들러 사용
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .requestCache(cache -> cache
                        .requestCache(new HttpSessionRequestCache() {
                            @Override
                            public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
                                if (!"/member/login".equals(request.getRequestURI())) {
                                    super.saveRequest(request, response);
                                }
                            }
                        }))
                .oauth2Login(oAuth2 -> oAuth2
                        .successHandler(socialLoginSuccessHandler)  // 소셜 로그인 성공 핸들러
                        .loginPage("/member/login"));  // 소셜 로그인 시 사용할 로그인 페이지 설정

        return http.build();
    }

}
