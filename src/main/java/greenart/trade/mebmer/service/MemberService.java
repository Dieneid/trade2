package greenart.trade.mebmer.service;

import greenart.trade.mebmer.dto.BlackDTO;
import greenart.trade.mebmer.dto.MemberDTO;
import greenart.trade.mebmer.entity.Address;
import greenart.trade.mebmer.entity.BlackList;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.entity.RoleType;
import greenart.trade.mebmer.repository.BlackRepository;
import greenart.trade.mebmer.repository.MemberRepository;
import greenart.trade.product.entity.Review;
import greenart.trade.product.repository.ProductRepository;
import greenart.trade.product.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final ProductRepository productRepository;

    private final BlackRepository blackRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final ReviewRepository reviewRepository;

    public Member createMember(MemberDTO memberDTO) {

        // 이메일 중복 확인
        if(memberRepository.findMemberByEmail(memberDTO.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");
        }

        // 이메일 인증 여부 확인
        if(!memberDTO.isCheckNum()){
            throw new IllegalArgumentException("이메일 인증을 해주세요.");
        }

        // 전화 번호 중복 확인
        if(memberRepository.findMemberByPhoneNumber(memberDTO.getPhoneNumber()).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }

        if(memberRepository.findByName(memberDTO.getName()).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 닉네임 입니다.");
        }


        // 비밀번호 재확인
        if(!memberDTO.getPassword().equals(memberDTO.getCompPassword())){
            throw new IllegalArgumentException("두 비밀번호가 일치하지않습니다.");
        }
        if(memberDTO.getPassword().isEmpty()){
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }
        // 비밀번호 최소 설정
        if(memberDTO.getPassword().length()<5){
            throw new IllegalArgumentException("비밀번호는 최소 5글자 이상이어야 합니다.");
        }

        addressCheck(memberDTO);


        Address address = Address.builder()
                .city(memberDTO.getCity())
                .street(memberDTO.getStreet())
                .zipcode(memberDTO.getZipcode())
                .build();

        Member member = Member.builder()
                .name(memberDTO.getName())
                .password(passwordEncoder.encode(memberDTO.getPassword()))
                .phoneNumber(memberDTO.getPhoneNumber())
                .email(memberDTO.getEmail())
                .profileImageUrl(memberDTO.getProfileImageUrl())
                .address(address)
                .build();
        member.addRole(RoleType.ROLE_USER);
        return memberRepository.save(member);
    }

    public Member createSocialMember(MemberDTO memberDTO) {

        // 이메일 중복 확인
        if(memberRepository.findMemberByEmail(memberDTO.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");
        }

        // 전화 번호 중복 확인
        if(memberRepository.findMemberByPhoneNumber(memberDTO.getPhoneNumber()).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }

        if(memberRepository.findByName(memberDTO.getName()).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 닉네임 입니다.");
        }

        // 비밀번호 재확인
        if(!memberDTO.getPassword().equals(memberDTO.getCompPassword())){
            throw new IllegalArgumentException("두 비밀번호가 일치하지않습니다.");
        }
        if(memberDTO.getPassword().isEmpty()){
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }
        // 비밀번호 최소 설정
        if(memberDTO.getPassword().length()<5){
            throw new IllegalArgumentException("비밀번호는 최소 5글자 이상이어야 합니다.");
        }

        addressCheck(memberDTO);

        Address address = Address.builder()
                .city(memberDTO.getCity())
                .street(memberDTO.getStreet())
                .zipcode(memberDTO.getZipcode())
                .build();

        Member member = Member.builder()
                .name(memberDTO.getName())
                .password(passwordEncoder.encode(memberDTO.getPassword()))
                .phoneNumber(memberDTO.getPhoneNumber())
                .email(memberDTO.getEmail())
                .profileImageUrl(memberDTO.getProfileImageUrl())
                .address(address)
                .fromSocial(true)
                .build();
        member.addRole(RoleType.ROLE_USER);
        return memberRepository.save(member);
    }

    public BlackList createBlackList(Optional<Member> thisMember, BlackDTO blackDTO) {

        if(memberRepository.findByName(blackDTO.getName()).isEmpty()){
            throw new IllegalArgumentException("없는 사용자입니다.");
        }
        Member members = memberRepository.findByName(blackDTO.getName()).get();
        Member mem = thisMember.get();

        if(mem.getName().equals(blackDTO.getName())){
            throw new IllegalArgumentException("자기 자신을 등록할 수 없습니다.");
        }

        // 이름 중복 확인
        if(blackRepository.findByBlackNameAndMember((blackDTO.getName()), mem).isPresent()){
            throw new IllegalArgumentException("이미 차단한 사용자 입니다.");
        }

        BlackList blackList = BlackList.builder()
                .blackEmail(members.getEmail())
                .blackName(members.getName())
                .member(mem)
                .build();

        return blackRepository.save(blackList);
    }

    @Transactional
    public void updateMember(MemberDTO memberDTO) {

        Member member = memberRepository.findMemberByEmail(memberDTO.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("회원 정보를 찾을수 없습니다."));
        Address address = member.getAddress();
        if(address == null){
            address = new Address();
        }
        addressCheck(memberDTO);

        if(!memberDTO.getName().equals(member.getName())){
            if(memberRepository.findByName(memberDTO.getName()).isPresent()){
                throw new IllegalArgumentException("이미 사용중인 닉네임 입니다.");
            }
        }

        updateMemberPhoneNumber(member,memberDTO);


        address.setCity(memberDTO.getCity());
        address.setStreet(memberDTO.getStreet());
        address.setZipcode(memberDTO.getZipcode());

        if(!member.getProfileImageUrl().equals(memberDTO.getProfileImageUrl())){
            if (memberDTO.getProfileImageUrl() != null && !memberDTO.getProfileImageUrl().isEmpty()) {
                String existingImageUrl = member.getProfileImageUrl(); // 기존 URL 가져오기
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    deleteImageFile(existingImageUrl); // 파일 삭제
                }
                member.setProfileImageUrl(memberDTO.getProfileImageUrl()); // 새 URL 저장
            }
        }

        member.setName(memberDTO.getName());
        if(memberDTO.getPassword() != null){
            member.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        }
        member.setAddress(address);
        memberRepository.save(member);
    }


    public void updateMemberPhoneNumber(Member member, MemberDTO memberDTO) {
        String newPhoneNumber = memberDTO.getPhoneNumber();
        String currentPhoneNumber = member.getPhoneNumber();

        if (newPhoneNumber == null || newPhoneNumber.isEmpty()) {
            throw new IllegalArgumentException("전화번호를 입력해 주세요.");
        }
        if (currentPhoneNumber == null || !currentPhoneNumber.equals(newPhoneNumber)) {
            if (memberRepository.findMemberByPhoneNumber(newPhoneNumber).isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
            }
        }

        member.setPhoneNumber(newPhoneNumber);
        memberRepository.save(member);
    }

    public void changePassword(MemberDTO memberDTO) {
        Member member = memberRepository.findMemberByEmail(memberDTO.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("회원 정보를 찾을수 없습니다."));

        // 기존에 쓰던 비밀번호 확인
        if (passwordEncoder.matches(memberDTO.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("기존에 쓰던 비밀번호 입니다.");
        }
        // 비밀번호 재확인
        if(!memberDTO.getPassword().equals(memberDTO.getCompPassword())){
            throw new IllegalArgumentException("두 비밀번호가 일치하지않습니다.");
        }
        // 비밀번호 최소 설정
        if(memberDTO.getPassword().length()<5){
            throw new IllegalArgumentException("비밀번호는 최소 5글자 이상이어야 합니다.");
        }

        member.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        memberRepository.save(member);

    }

    public void deleteMember(MemberDTO memberDTO) {
        Member member = memberRepository.findMemberByEmail(memberDTO.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("회원 정보를 찾을수 없습니다."));
        if(member.getProfileImageUrl()!=null && !member.getProfileImageUrl().isEmpty()){
            deleteImageFile(member.getProfileImageUrl());
        }

        memberRepository.deleteById(member.getMemberId());
    }

    public void disableMember(MemberDTO memberDTO) {
        Member member = memberRepository.findMemberByEmail(memberDTO.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("회원 정보를 찾을수 없습니다."));
        member.setEnabled(false);
        member.setName(member.getName() + "(탈퇴한 유저)");
        memberRepository.save(member);
    }

    private void deleteImageFile(String imageUrl) {
        try {
            if(!imageUrl.equals("/uploads/default-profile.png")){
                String uploadDir = "C:";
                String basePath = uploadDir.endsWith(File.separator) ? uploadDir : uploadDir + File.separator;

                // URL 경로를 파일 시스템 경로로 변환
                String relativePath = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl; // 선행 슬래시 제거
                Path imagePath = Paths.get(basePath, relativePath.replace("/", File.separator));

                // 파일 존재 여부 확인 후 삭제
                if (Files.deleteIfExists(imagePath)) {
                    System.out.println("File deleted successfully: " + imagePath.toAbsolutePath());
                } else {
                    System.out.println("File not found: " + imagePath.toAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지를 삭제하는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public void addressCheck(MemberDTO memberDTO) {
        if(memberDTO.getCity().isEmpty()){
            throw new IllegalArgumentException("주소를 입력해주세요.");
        }
        if(memberDTO.getStreet().isEmpty()){
            throw new IllegalArgumentException("상세 주소를 입력해주세요.");
        }
        if(memberDTO.getZipcode().isEmpty()){
            throw new IllegalArgumentException("우편 번호를 입력해주세요.");
        }
    }

    public Member comeBackMember(MemberDTO memberDTO) {
        Optional<Member> memberOpt = memberRepository.findByEmail(memberDTO.getEmail());
        Member member = memberOpt.orElse(null);

        if(member.isEnabled()){
            throw new IllegalArgumentException("탈퇴한 회원이 아닙니다.");
        }
        // 이메일 인증 여부 확인
        if(!memberDTO.isCheckNum()){
            throw new IllegalArgumentException("이메일 인증을 해주세요.");
        }
        // 비밀번호 재확인
        if(!memberDTO.getPassword().equals(memberDTO.getCompPassword())){
            throw new IllegalArgumentException("두 비밀번호가 일치하지않습니다.");
        }
        if(memberDTO.getPassword().isEmpty()){
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }
        String name = member.getName();
        member.setName(name.substring(0, name.length() - "(탈퇴한 유저)".length()));
        member.setEnabled(true);
        return memberRepository.save(member);
    }

    public MemberDTO getMemberInfo(Long memberId) {
        // 멤버 정보 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다."));

        // 멤버 정보 DTO로 변환
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setEmail(member.getEmail());
        memberDTO.setName(member.getName());

        // 해당 멤버의 리뷰 가져오기
        List<Review> reviews = reviewRepository.findByMemberMemberId(memberId);

        // 평균 별점 계산
        double averageScore = reviews.stream()
                .mapToDouble(review -> review.getEvaluation().getScore())
                .average()
                .orElse(0.0);

        // 평균 별점 설정
        memberDTO.setAverageScore(averageScore);

        return memberDTO;
    }
}
