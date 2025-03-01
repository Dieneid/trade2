package greenart.trade.mebmer.dto;

import greenart.trade.mebmer.entity.Address;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@ToString
public class AuthDTO extends User implements OAuth2User {

    private Long memberId;
    private String email;
    private String password;
    private boolean fromSocial;
    private String name;
    private String MemberName;
    private Address address;
    private Map<String, Object> attr;

    public AuthDTO(Long memberId, String username, String password, boolean fromSocial,
                         Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.memberId = memberId;
        this.email = username;
        this.password = password;
        this.fromSocial = fromSocial;
    }

    public AuthDTO(Long memberId, String username, String password, boolean fromSocial,
                         Collection<? extends GrantedAuthority> authorities, Map<String, Object> attr) {
        this(memberId,username,password,fromSocial, authorities);
        this.attr = attr;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attr;
    }

}
