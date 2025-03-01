package greenart.trade.mebmer.controller;

import greenart.trade.mebmer.dto.*;
import greenart.trade.mebmer.entity.Member;
import greenart.trade.mebmer.repository.MemberRepository;
import greenart.trade.mebmer.service.BlackListService;
import greenart.trade.mebmer.service.EmailProcessor;
import greenart.trade.mebmer.service.MailService;
import greenart.trade.mebmer.service.MemberService;
import greenart.trade.product.dto.CategoryDTO;
import greenart.trade.product.dto.ProductDTO;
import greenart.trade.product.entity.Product;
import greenart.trade.product.entity.Review;
import greenart.trade.product.repository.ProductRepository;
import greenart.trade.product.repository.ReviewRepository;
import greenart.trade.product.service.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;


@RequestMapping("/member")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MailService mailService;
    private final BlackListService blackListService;
    private final ProductRepository productRepository;
    private final ProductServiceImpl productServiceImpl;
    private final FavoriteService favoriteService;
    private final CategoryService categoryService;
    private final ReviewServiceImpl reviewServiceImpl;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    // =================================================================== 추가 ========================================
    // =================================================================== 추가 ========================================
    // =================================================================== 추가 ========================================
    // =================================================================== 추가 ========================================
    private int number;

    @GetMapping("/login")
    public String showLoginPage(LoginDTO loginDTO,
                                @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "exception", required = false) String exception,
                                @RequestParam(value = "redirect", required = false) String redirect,
                                Model model, HttpSession session) {
        if (redirect != null && !redirect.isEmpty()) {
            session.setAttribute("redirect", redirect);
        }
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        model.addAttribute("loginDTO", loginDTO);
        return "member/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        MemberDTO memberDTO = new MemberDTO();
        model.addAttribute("memberDTO", memberDTO);
        return "member/register";
    }

    @GetMapping("/socialregister")
    public String showSocialRegisterPage(Authentication authentication, Model model, HttpServletRequest request) {
        String username = authentication.getName();
        Optional<Member> memberOpt = memberRepository.findMemberByEmail(username);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setEmail(member.getEmail());
            memberDTO.setName(member.getName());

            if(member.getPhoneNumber() != null) {
                memberDTO.setPhoneNumber(member.getPhoneNumber());
            }
            if(member.getProfileImageUrl() != null) {
                memberDTO.setProfileImageUrl(member.getProfileImageUrl());
            }
            request.getSession().invalidate();
            memberService.deleteMember(memberDTO);
            if(member.getAddress() != null) {
                if(member.getAddress().getCity() != null) {
                    memberDTO.setCity(member.getAddress().getCity());
                }
                if(member.getAddress().getStreet() != null) {
                    memberDTO.setStreet(member.getAddress().getStreet());
                }
                if(member.getAddress().getZipcode() != null) {
                    memberDTO.setZipcode(member.getAddress().getZipcode());
                }
            }
            model.addAttribute("memberDTO", memberDTO);
        }
        return "member/socialregister";
    }

    @GetMapping("/mypage")
    public String showMyPage(Authentication authentication, @AuthenticationPrincipal AuthDTO authDTO ,Model model) {
        String username = authentication.getName();
        Optional<Member> memberOpt = memberRepository.findMemberByEmail(username);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            MemberDTO memberDTO = memberService.getMemberInfo(member.getMemberId());
            List<Review> reviews = reviewServiceImpl.findReviewsByMemberId(member.getMemberId());
            List<Product> favoriteProducts = favoriteService.getFavoritesByMemberId(authDTO);
            List<ProductDTO> productDTOList = productServiceImpl.getAllProducts();
            List<Product> myproductList = productRepository.findByMemberIdAndEnabledTrueOrderByRefreshedAtDesc(member.getMemberId());

            List<String> profileImageUrls = new ArrayList<>();

            for (Review review : reviews) {
                String authUsername = review.getAuthorUsername();
                Optional<Member> writerOpt = memberRepository.findByName(authUsername);

                if (writerOpt.isPresent()) {
                    Member writer = writerOpt.get();
                    // 프로필 이미지 URL 생성
                    String profileImageUrl = writer.getProfileImageUrl(); // 상대 경로 처리
                    // 기본 이미지 처리
                    if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                        profileImageUrl = "/images/default.png";
                    }
                    profileImageUrls.add(profileImageUrl);
                }
            }

            model.addAttribute("profileImageUrls", profileImageUrls);

            List<ProductDTO> productDTOs = myproductList.stream()
                    .map(product -> {
                        ProductDTO productDTO = new ProductDTO();
                        productDTO.setMemberId(product.getMember().getMemberId());
                        productDTO.setTitle(product.getTitle());
                        productDTO.setSellPrice(product.getSellPrice());
                        productDTO.setViewCount(product.getViewCount());
                        productDTO.setDescription(product.getDescription());
                        productDTO.setStatus(product.getStatus().toString());
                        productDTO.setCreatedAt(product.getCreatedAt());
                        productDTO.setUpdatedAt(product.getUpdatedAt());
                        productDTO.setRefreshedAt(product.getRefreshedAt());
                        productDTO.setMemberName(product.getMember().getName());
                        productDTO.setProductId(product.getProductId());
                        return productDTO;
                    })
                    .collect(Collectors.toList());


            double averageScore = memberDTO.getAverageScore();
            int fullStars = (int)averageScore;
            double partialStarPercent = (averageScore - fullStars) * 100;
            String partialStarStyle = String.format("--percent: %.2f%%", partialStarPercent);

            model.addAttribute("fullStars", fullStars);
            model.addAttribute("partialStarStyle", partialStarStyle);
            model.addAttribute("favoriteProducts", favoriteProducts);
            model.addAttribute("productDTOList", productDTOList);
            model.addAttribute("myproductList", productDTOs);

            if (!member.isFromSocial()){ // 일반 로그인 사용자
                memberDTO.setProfileImageUrl(member.getProfileImageUrl());
                memberDTO.setPhoneNumber(member.getPhoneNumber());
                memberDTO.setCity(member.getAddress().getCity());
                memberDTO.setStreet(member.getAddress().getStreet());
                memberDTO.setZipcode(member.getAddress().getZipcode());
            }

            if(member.isFromSocial()){ // 소셜 로그인 사용자
                if(!member.getPhoneNumber().isEmpty()){
                    memberDTO.setPhoneNumber(member.getPhoneNumber());
                }
                if(!member.getProfileImageUrl().isEmpty()){
                    memberDTO.setProfileImageUrl(member.getProfileImageUrl());
                }
                if(!member.getAddress().getCity().isEmpty()){
                    memberDTO.setCity(member.getAddress().getCity());
                }
                if(!member.getAddress().getStreet().isEmpty()){
                    memberDTO.setStreet(member.getAddress().getStreet());
                }
                if(!member.getAddress().getZipcode().isEmpty()){
                    memberDTO.setZipcode(member.getAddress().getZipcode());
                }
            }
            model.addAttribute("memberDTO", memberDTO);
            model.addAttribute("reviews", reviews);
            model.addAttribute("memberId", member.getMemberId());
        }
        return "member/mypage";
    }

    @GetMapping("/memberpage/{memberId}")
    public String showMemberPage(@PathVariable Long memberId, Model model, @AuthenticationPrincipal AuthDTO authDTO) {
        Optional<Member> memberOpt = memberRepository.findById(memberId);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            MemberDTO memberDTO = memberService.getMemberInfo(member.getMemberId());
            List<Review> reviews = reviewServiceImpl.findReviewsByMemberId(member.getMemberId());
            List<ProductDTO> productDTOList = productServiceImpl.getAllProducts();
            List<Product> memberProductList = productRepository.findByMemberIdAndEnabledTrueOrderByRefreshedAtDesc(member.getMemberId());

            for (Review review : reviews) {
                String authUsername = review.getAuthorUsername();
                Member writers = memberRepository.findByName(authUsername).get();
                // 프로필 이미지 URL 생성
                String profileImageUrl = writers.getProfileImageUrl(); // 상대 경로 처리
                // 기본 이미지 처리
                if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                    profileImageUrl = "/images/default.png";
                    System.out.println("profileImageUrl = " + profileImageUrl);
                }
                model.addAttribute("profileImageUrl", profileImageUrl);
            }

            List<ProductDTO> productDTOs = memberProductList.stream()
                    .map(product -> {
                        ProductDTO productDTO = new ProductDTO();
                        productDTO.setMemberId(product.getMember().getMemberId());
                        productDTO.setTitle(product.getTitle());
                        productDTO.setSellPrice(product.getSellPrice());
                        productDTO.setViewCount(product.getViewCount());
                        productDTO.setDescription(product.getDescription());
                        productDTO.setStatus(product.getStatus().toString());
                        productDTO.setCreatedAt(product.getCreatedAt());
                        productDTO.setUpdatedAt(product.getUpdatedAt());
                        productDTO.setRefreshedAt(product.getRefreshedAt());
                        productDTO.setMemberName(product.getMember().getName());
                        productDTO.setProductId(product.getProductId());
                        return productDTO;
                    })
                    .collect(Collectors.toList());

            double averageScore = memberDTO.getAverageScore();
            int fullStars = (int)averageScore;
            double partialStarPercent = (averageScore - fullStars) * 100;
            String partialStarStyle = String.format("--percent: %.2f%%", partialStarPercent);

            model.addAttribute("fullStars", fullStars);
            model.addAttribute("partialStarStyle", partialStarStyle);
            model.addAttribute("productDTOList", productDTOList);
            model.addAttribute("memberProductList", productDTOs);

            if (!member.isFromSocial()){ // 일반 로그인 사용자
                memberDTO.setProfileImageUrl(member.getProfileImageUrl());
            }

            if(member.isFromSocial()){ // 소셜 로그인 사용자
                if(!member.getProfileImageUrl().isEmpty()){
                    memberDTO.setProfileImageUrl(member.getProfileImageUrl());
                }
            }

            model.addAttribute("authDTO", authDTO);
            memberDTO.setMemberId(memberId);
            model.addAttribute("memberDTO", memberDTO);
            model.addAttribute("reviews", reviews);
            model.addAttribute("memberId", member.getMemberId());
        }
        return "member/memberpage";
    }

    @GetMapping("/update")
    public String showUpdate(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<Member> memberOpt = memberRepository.findMemberByEmail(username);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setEmail(member.getEmail());
            memberDTO.setName(member.getName());
            memberDTO.setAverageScore(member.getAverageScore());
            if (!member.isFromSocial()){ // 일반로그인사용자
                memberDTO.setProfileImageUrl(member.getProfileImageUrl());
                memberDTO.setPhoneNumber(member.getPhoneNumber());
                memberDTO.setCity(member.getAddress().getCity());
                memberDTO.setStreet(member.getAddress().getStreet());
                memberDTO.setZipcode(member.getAddress().getZipcode());
            }
            if(member.isFromSocial()){ // 소셜 로그인 사용자
                if(!member.getPhoneNumber().isEmpty()){
                    memberDTO.setPhoneNumber(member.getPhoneNumber());
                }
                if(!member.getProfileImageUrl().isEmpty()){
                    memberDTO.setProfileImageUrl(member.getProfileImageUrl());
                }
                if(!member.getAddress().getCity().isEmpty()){
                    memberDTO.setCity(member.getAddress().getCity());
                }
                if(!member.getAddress().getStreet().isEmpty()){
                    memberDTO.setStreet(member.getAddress().getStreet());
                }
                if(!member.getAddress().getZipcode().isEmpty()){
                    memberDTO.setZipcode(member.getAddress().getZipcode());
                }
            }
            model.addAttribute("memberDTO", memberDTO);
        }
        return "member/update";
    }

    @GetMapping("/password")
    public String showPassword(Model model) {
        model.addAttribute("memberDTO", new MemberDTO());
        return "member/password";
    }

    @GetMapping("/delete")
    public String deleteMember(Authentication authentication, HttpServletRequest request,RedirectAttributes rttr) {
        MemberDTO memberDTO = new MemberDTO();
        String email = authentication.getName();
        memberDTO.setEmail(email);
        request.getSession().invalidate();
        memberService.disableMember(memberDTO);
        rttr.addFlashAttribute("message","회원 탈퇴 완료");
        return "redirect:/";
    }

    @GetMapping("/selectReg")
    public String showSelectReg() {
        return "member/selectReg";
    }

    @GetMapping("/mailCheck")
    public ResponseEntity<?> MailCheck(@RequestParam String checkNumber) {
        boolean isMatch = checkNumber.equals(String.valueOf(number));
        return ResponseEntity.ok(isMatch);
    }

    @GetMapping("/black")
    public String black(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<Member> memberOpt = memberRepository.findMemberByEmail(username);

        List<BlackListDTO> blackListDTOS = new ArrayList<>();
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            if (member.getBlackLists() != null && !member.getBlackLists().isEmpty()) {
                blackListDTOS = member.getBlackLists().stream()
                        .map(blackList -> new BlackListDTO(
                                blackList.getBlackId(),
                                blackList.getBlackEmail(),
                                blackList.getBlackName(),
                                blackList.getCreatedAt()
                        ))
                        .collect(Collectors.toList());
            }

            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setEmail(member.getEmail());
            model.addAttribute("memberDTO", memberDTO);

            BlackDTO blackDTO = new BlackDTO();
            model.addAttribute("blackDTO", blackDTO);
        }

        model.addAttribute("blackListDTOS", blackListDTOS);

        // 메시지 초기화
        model.addAttribute("message", model.getAttribute("message") == null ? "" : model.getAttribute("message"));
        model.addAttribute("blackError", model.getAttribute("blackError") == null ? "" : model.getAttribute("blackError"));

        return "member/black";
    }

    @GetMapping("/comeback")
    public String showComeback(MemberDTO memberDTO, Model model) {

        model.addAttribute("memberDTO", memberDTO);
        return "member/comeback";
    }

    @GetMapping("/rePassword")
    public String showRePassword(MemberDTO memberDTO, Model model) {
        model.addAttribute("memberDTO", memberDTO);
        return "member/rePassword";
    }

    @GetMapping("/{memberId}/list")
    public String showMemberList(@PathVariable Long memberId,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "16") int size,
                                 Model model,
                                 @AuthenticationPrincipal AuthDTO authDTO) {

        Pageable pageable = PageRequest.of(page, size);

        // 특정 사용자가 등록한 상품 목록을 페이징으로 가져오기
        Page<Product> productPage = productRepository.findByMemberIdAndEnabledTrueOrderByRefreshedAtDesc(memberId, pageable);

        // 상품을 DTO로 변환
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(product -> {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setMemberId(product.getMember().getMemberId());
                    productDTO.setTitle(product.getTitle());
                    productDTO.setSellPrice(product.getSellPrice());
                    productDTO.setViewCount(product.getViewCount());
                    productDTO.setDescription(product.getDescription());
                    productDTO.setCreatedAt(product.getCreatedAt());
                    productDTO.setUpdatedAt(product.getUpdatedAt());
                    productDTO.setRefreshedAt(product.getRefreshedAt());
                    productDTO.setMemberName(product.getMember().getName());
                    productDTO.setProductId(product.getProductId());
                    return productDTO;
                })
                .collect(Collectors.toList());

        // 카테고리 목록 추가
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // 페이징된 상품 목록을 모델에 추가
        model.addAttribute("productPage", productPage); // Page 객체
        model.addAttribute("products", productDTOs); // 현재 페이지 상품 데이터

        // 상품 이미지 추가
        List<ProductDTO> productDTOList = productServiceImpl.getAllProducts();
        model.addAttribute("productDTOList", productDTOList);

        model.addAttribute("authDTO", authDTO);

        // 각 상품에 대해 사용자의 찜 여부 확인
        if (authDTO != null) {
            Long memberId1 = authDTO.getMemberId();
            Map<Long, Boolean> favoriteStatusMap = productDTOs.stream()
                    .collect(Collectors.toMap(
                            ProductDTO::getProductId,
                            product -> favoriteService.isFavorited(product.getProductId(), memberId1)
                    ));

            model.addAttribute("favoriteStatusMap", favoriteStatusMap);
        }

        return "product/list";
    }

    @GetMapping("/{memberId}/info")
    @ResponseBody
    public ResponseEntity<?> getMemberAverage(@PathVariable Long memberId) {
        Optional<Member> byId = memberRepository.findById(memberId);

        if (byId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Member not found");
        }
        MemberDTO memberInfo = memberService.getMemberInfo(memberId);

        Member member = byId.get();
        Map<String, Object> response = new HashMap<>();

        System.out.println("memberId = " + memberId);
        System.out.println("member.getAverageScore() = " + member.getAverageScore());
        response.put("averageScore", memberInfo.getAverageScore());
        response.put("name", member.getName()); // 추가적인 정보 포함 가능
        return ResponseEntity.ok(response);
    }

    //========================================================= 추가 ================================================
    //========================================================= 추가 ================================================
    @PostMapping("/check-emails")
    @ResponseBody
    public Map<String, String> checkEmails(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();
        try {
            // randomCode 값 추출
            int randomCode = (int) request.get("randomCode");
            System.out.println("Received Random Code: " + randomCode);

            EmailProcessor emailProcessor = new EmailProcessor();
            emailProcessor.setLastCheckedDate(new Date()); // 클릭 시점 저장

            String phoneNumber = null;
            String carrier = null;

            // 최대 3분 동안 2초 간격으로 이메일 확인
            for (int i = 0; i < 90; i++) {
                emailProcessor.checkEmails(randomCode);

                phoneNumber = emailProcessor.getLatestPhoneNumber();
                carrier = emailProcessor.getLatestCarrier();

                if (phoneNumber != null && carrier != null) {
                    break; // 이메일이 확인되면 즉시 종료
                }

                Thread.sleep(2000); // 2초 대기
            }

            if (phoneNumber != null && carrier != null) {
                response.put("phoneNumber", phoneNumber);
                response.put("carrier", carrier);
            } else {
                response.put("error", "3분 동안 새로운 이메일에서 전화번호를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            response.put("error", "An error occurred: " + e.getMessage());
        }
        return response;
    }

    //========================================================= 추가 ================================================
    //========================================================= 추가 ================================================

    @PostMapping("/register")
    public String createMember(MemberDTO memberDTO, RedirectAttributes rttr, HttpServletRequest request, Model model) {
        try {
            memberService.createMember(memberDTO);
            request.login(memberDTO.getEmail(), memberDTO.getPassword());


            rttr.addFlashAttribute("message","회원 가입 완료");
            return "redirect:/";
        } catch (IllegalArgumentException | ServletException e) {
            model.addAttribute("registerError", e.getMessage());
            model.addAttribute("memberDTO", memberDTO);
            return "member/register";
        }
    }

    @PostMapping("/update")
    public String updateMember(MemberDTO memberDTO, Authentication authentication, RedirectAttributes rttr) {
        try {
            String username = authentication.getName();

            memberDTO.setEmail(username);

            memberService.updateMember(memberDTO);

            rttr.addFlashAttribute("message", "회원 정보 수정 완료");
            return "redirect:/member/mypage";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/update";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "알 수 없는 오류가 발생했습니다.");
            return "redirect:/member/update";
        }
    }


    @PostMapping("/socialregister/create")
    public String createSocialMember(@ModelAttribute MemberDTO memberDTO, RedirectAttributes rttr, HttpServletRequest request, Model model) {
        try {
            memberService.createSocialMember(memberDTO);
            request.login(memberDTO.getEmail(), memberDTO.getPassword());

            rttr.addFlashAttribute("message", "회원 가입 완료");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("memberDTO", memberDTO);
            return "member/socialregister";
        } catch (Exception e) {
            model.addAttribute("error", "알 수 없는 오류가 발생했습니다.");
            model.addAttribute("memberDTO", memberDTO);
            return "member/socialregister";
        }
    }

    @PostMapping("/password")
    public String updatePassword(MemberDTO memberDTO, Authentication authentication, RedirectAttributes rttr) {
        try {
            String username = authentication.getName();
            memberDTO.setEmail(username);
            memberService.changePassword(memberDTO);

            rttr.addFlashAttribute("message", "비밀 번호 변경 완료");
            return "redirect:/member/update";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/password";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "알 수 없는 오류가 발생했습니다.");
            return "redirect:/member/password";
        }
    }

    @PostMapping("/mailSend")
    public ResponseEntity<Map<String, Object>> mailSend(@RequestParam String email) {
        Map<String, Object> map = new HashMap<>();
        try {
            System.out.println("이메일 전송 시작: " + email);
            number = mailService.sendMail(email);
            System.out.println("인증 번호 생성 완료: " + number);

            map.put("success", Boolean.TRUE);
            map.put("number", String.valueOf(number));
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("success", Boolean.FALSE);
            map.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
        }
    }

    @PostMapping("/black")
    public String blackListAdd(BlackDTO blackDTO, MemberDTO memberDTO, RedirectAttributes rttr, Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            memberDTO.setEmail(email);
            Optional<Member> member = memberRepository.findMemberByEmail(memberDTO.getEmail());

            memberService.createBlackList(member,blackDTO);

            rttr.addFlashAttribute("message","차단 완료");
            return "redirect:/member/black";
        }catch (IllegalArgumentException e) {
            String email = authentication.getName();
            Optional<Member> memberOpt = memberRepository.findMemberByEmail(email);

            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                List<BlackListDTO> blackListDTOS = member.getBlackLists().stream()
                        .map(blackList -> new BlackListDTO(
                                blackList.getBlackId(),
                                blackList.getBlackEmail(),
                                blackList.getBlackName(),
                                blackList.getCreatedAt()
                        ))
                        .collect(Collectors.toList());
                model.addAttribute("blackListDTOS", blackListDTOS);
            }

            model.addAttribute("blackError", e.getMessage());
            model.addAttribute("blackDTO", blackDTO);
            model.addAttribute("memberDTO", memberDTO);
            return "member/black";
        }
    }

    @PostMapping("/blackModal")
    @ResponseBody
    public ResponseEntity<String> blackListModalAdd(@RequestBody BlackDTO request, Authentication authentication) {
        try {
            // 로그인한 사용자 정보 가져오기
            String userEmail = authentication.getName(); // 이메일 기반으로 사용자 정보 가져옴
            Optional<Member> loggedInMember = memberRepository.findMemberByEmail(userEmail);

            if (!loggedInMember.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인된 사용자를 찾을 수 없습니다.");
            }

            // 블랙리스트 추가 처리
            Optional<Member> blackById = memberRepository.findById(request.getMemberId());
            if (!blackById.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("차단하려는 사용자를 찾을 수 없습니다.");
            }
            Member member = blackById.get();
            BlackDTO blackDTO = new BlackDTO();
            blackDTO.setName(member.getName());
            blackDTO.setEmail(member.getEmail());

            memberService.createBlackList(loggedInMember, blackDTO);
            return ResponseEntity.ok("차단이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("차단 실패: " + e.getMessage());
        }
    }

    @PostMapping("/blackDelete")
    public String removeBlackList(@RequestParam(required = false) List<String> blackListName, RedirectAttributes rttr, Authentication authentication) {
        try {
            if (blackListName == null || blackListName.isEmpty()) {
                rttr.addFlashAttribute("blackError", "선택된 사용자가 없습니다.");
                return "redirect:/member/black";
            }

            String email = authentication.getName();
            Optional<Member> memberOpt = memberRepository.findMemberByEmail(email);

            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();

                blackListName.forEach(blackName -> {
                    blackListService.deleteBlackList(blackName, member);
                });

                rttr.addFlashAttribute("message", "선택한 사용자가 차단 해제되었습니다.");
            } else {
                rttr.addFlashAttribute("blackError", "사용자를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            rttr.addFlashAttribute("blackError", "차단 해제 중 오류가 발생했습니다. " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/member/black";
    }

    @PostMapping("/comeback")
    public String comeBackMember(MemberDTO memberDTO, RedirectAttributes rttr, Model model) {
        try {

            memberService.comeBackMember(memberDTO);

            rttr.addFlashAttribute("message", "회원 복구 완료");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("memberDTO", memberDTO);
            model.addAttribute("error", e.getMessage());
            return "member/comeback";
        } catch (Exception e) {
            model.addAttribute("memberDTO", memberDTO);
            model.addAttribute("error", "알 수 없는 오류가 발생했습니다.");
            return "member/comeback";
        }
    }

    @PostMapping("/rePassword")
    public String rePassword(MemberDTO memberDTO,RedirectAttributes rttr, Model model) {
        try {
            memberService.changePassword(memberDTO);

            rttr.addFlashAttribute("message", "비밀 번호 변경 완료");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("memberDTO", memberDTO);
            model.addAttribute("error", e.getMessage());
            return "member/rePassword";
        } catch (Exception e) {
            model.addAttribute("memberDTO", memberDTO);
            model.addAttribute("error", "알 수 없는 오류가 발생했습니다.");
            return "member/rePassword";
        }
    }

}
