package greenart.trade.chat.service;

import greenart.trade.chat.dto.ChatRoomDTO;
import greenart.trade.chat.entity.ChatRoom;
import greenart.trade.chat.repository.ChatRoomRepository;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatConverter chatConverter;

    public ChatRoom getOrCreateRoom(Long senderId, Long receiverId) {
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found:" + senderId));
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found:" + receiverId));
        return chatRoomRepository.findChatRoomBetweenMembers(senderId, receiverId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .sender(sender)
                            .receiver(receiver)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
    }


    public List<ChatRoom> getChatRoomsByMemberId(Long memberId) {
        List<ChatRoom> allChatRooms = chatRoomRepository.findAll();  // 모든 채팅방 가져오기

        // memberId와 senderId 또는 receiverId가 일치하는 채팅방 필터링
        return allChatRooms.stream()
                .filter(room -> room.getSender().getMemberId().equals(memberId) || room.getReceiver().getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    // 특정 ChatRoom을 ChatRoomDTO로 반환
    public ChatRoomDTO getChatRoomDTO(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found"));
        return chatConverter.toChatRoomDTO(chatRoom);
    }

    // 채팅방 ID로 채팅방을 찾는 메서드
    public ChatRoom findById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found with id: " + chatRoomId));
    }
}
