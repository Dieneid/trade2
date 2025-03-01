package greenart.trade.mebmer.repository;

import greenart.trade.mebmer.entity.BlackList;
import greenart.trade.mebmer.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlackRepository extends JpaRepository<BlackList,Long> {

    Optional<BlackList> findByBlackNameAndMember(String blackName, Member member);

    @Modifying
    @Query("DELETE FROM BlackList b WHERE b.blackName = :blackName AND b.member = :member")
    void deleteByBlackNameAndMember(@Param("blackName") String blackName, @Param("member") Member member);

    // 특정 memberId에 해당하는 블랙리스트 조회
    List<BlackList> findByMember_MemberId(Long memberId);
}
