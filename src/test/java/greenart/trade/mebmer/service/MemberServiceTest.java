package greenart.trade.mebmer.service;

import greenart.trade.mebmer.dto.MemberDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Test
    void test(){
        for (int i = 0; i <= 2; i++) {
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setEmail("test"+i+"@example.com");
            memberDTO.setPhoneNumber("0101234567"+i);
            memberDTO.setName("Test User"+i);
            memberDTO.setPassword("1234");
            memberDTO.setCompPassword("1234");
            memberDTO.setCity("Seoul"+i);
            memberDTO.setStreet("Some street"+i);
            memberDTO.setZipcode("1234"+i);

            memberService.createMember(memberDTO);
        }


    }



}