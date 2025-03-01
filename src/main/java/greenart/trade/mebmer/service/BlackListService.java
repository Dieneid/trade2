package greenart.trade.mebmer.service;

import greenart.trade.mebmer.entity.BlackList;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.repository.BlackRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlackListService {

    private final BlackRepository blackRepository;

    public BlackListService(BlackRepository blackRepository) {
        this.blackRepository = blackRepository;
    }

    @Transactional
    public void deleteBlackList(String blackName, Member member) {
        blackRepository.deleteByBlackNameAndMember(blackName, member);
    }
    // 특정 memberId를 기준으로 블랙리스트 조회
    @Transactional
    public List<BlackList> getBlackListByMemberId(Long memberId) {
        return blackRepository.findByMember_MemberId(memberId);
    }
}
