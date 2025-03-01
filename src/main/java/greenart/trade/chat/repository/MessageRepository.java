package greenart.trade.chat.repository;

import greenart.trade.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.receiver WHERE m.chatRoom.chatRoomId = :chatRoomId")
    List<Message> findMessagesByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
