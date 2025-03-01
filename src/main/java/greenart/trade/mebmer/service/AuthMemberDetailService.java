package greenart.trade.mebmer.service;

import greenart.trade.mebmer.dto.AuthDTO;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.entity.RoleType;
import greenart.trade.mebmer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class AuthMemberDetailService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String clientName = userRequest.getClientRegistration().getClientName();
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = null;
        String name = null;

        if(clientName.equals("Google")){
            email = (String) oAuth2User.getAttributes().get("email");
            name = (String) oAuth2User.getAttributes().get("name");
            if(name==null){
                name = email;
            }
        } else if (clientName.equals("Naver")) {
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            email = (String) response.get("email");
            name = (String) response.get("name");
        } else if (clientName.equals("Kakao")) {
            Map<String, Object> attributes = oAuth2User.getAttributes();

            LinkedHashMap accountMap = (LinkedHashMap) attributes.get("kakao_account");
            email = (String) accountMap.get("email");
            LinkedHashMap propertiesMap = (LinkedHashMap) attributes.get("properties");
            name = (String) propertiesMap.get("nickname");

            if (email == null) {
                throw new OAuth2AuthenticationException("카카오 이메일을 가져올 수 없습니다.");
            }
            if (name == null) {
                name = email; // 이름이 없다면 이메일을 이름으로 사용
            }
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("이메일 정보를 가져올 수 없습니다.");
        }

        Member member = saveSocialMember(email, name);
        AuthDTO authDTO = new AuthDTO(
                member.getMemberId(),
                member.getEmail(),
                member.getPassword(),
                true,
                member.getRole().stream().map(
                                role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList()),
                oAuth2User.getAttributes()
        );
        authDTO.setAddress(member.getAddress());
        authDTO.setMemberName(member.getName());
        return authDTO;
    }

    private Member saveSocialMember(String email, String name) {
        return memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 새로운 소셜 회원 저장
                    Member member = Member.builder()
                            .email(email)
                            .name("") // 기본 이름 설정
                            .password(passwordEncoder.encode("1234")) // 기본 비밀번호 설정
                            .fromSocial(true)
                            .build();
                    member.addRole(RoleType.ROLE_USER);
                    return memberRepository.save(member);
                });
    }

}
