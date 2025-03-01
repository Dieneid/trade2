package greenart.trade.mebmer.entity;

import greenart.trade.common.entity.BaseEntity;
import greenart.trade.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number",nullable = true)
    private String phoneNumber;

    private double averageScore;

    @Embedded
    private Address address;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<RoleType> role = new HashSet<RoleType>();

    private boolean fromSocial; // 소셜로그인여부

    private String profileImageUrl;

    public void addRole(RoleType roleType) {
        role.add(roleType);
    }

    @ManyToMany
    private List<Product> favoritelist = new ArrayList<>();

    // 블랙리스트
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlackList> blackLists = new ArrayList<>();

    // 멤버 활성화 상태
    @Builder.Default
    private boolean enabled = true;
}
