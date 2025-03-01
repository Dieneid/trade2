package greenart.trade.mebmer.service;

import greenart.trade.mebmer.dto.AuthDTO;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("로그인 : username = " + username);
        Optional<Member> result = memberRepository.findByEmail(username);
        if(!result.isPresent()){
            throw new UsernameNotFoundException("Check Email or Social");
        }
        Member findMember = result.get();

        // 비활성화된 계정 처리
        if (!findMember.isEnabled()) {
            throw new DisabledException("탈퇴한 회원 입니다. 이용하시려면 회원 복구를 해주세요.");
        }

        // 권한 설정
        Set<GrantedAuthority> authorities = findMember.getRole().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        // 일반 사용자 처리
        AuthDTO authDTO = new AuthDTO(
                findMember.getMemberId(),
                findMember.getEmail(),
                findMember.getPassword(),
                findMember.isFromSocial(),
                authorities
        );
        // 추가 정보 설정
        authDTO.setName(findMember.getName());
        if (findMember.getAddress() != null) {
            authDTO.setAddress(findMember.getAddress());
        }
        authDTO.setMemberName(findMember.getName());
        return authDTO;
    }
}
