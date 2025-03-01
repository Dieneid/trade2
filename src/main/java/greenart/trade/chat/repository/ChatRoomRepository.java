package greenart.trade.chat.repository;

import greenart.trade.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 두 사용자의 채팅방을 찾는 쿼리
    @Query("SELECT c FROM ChatRoom c " +
            "WHERE (c.sender.memberId = :senderId AND c.receiver.memberId = :receiverId) " +
            "OR (c.sender.memberId = :receiverId AND c.receiver.memberId = :senderId)")
    Optional<ChatRoom> findChatRoomBetweenMembers(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    Optional<ChatRoom> findByChatRoomId(Long chatRoomId);
}
