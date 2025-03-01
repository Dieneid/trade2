package greenart.trade.mebmer.repository;

import greenart.trade.mebmer.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @EntityGraph(attributePaths = {"role"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("select m from Member m where m.email= :email")
    Optional<Member> findByEmail(
            @Param("email") String email);

    Optional<Member> findMemberByPhoneNumber(String phoneNumber); // 사용자 휴대 전화번호 조회

    Optional<Member> findMemberByEmail(String email); // 사용자 이메일 조회

    Optional<Member> findByName(String username);

}
