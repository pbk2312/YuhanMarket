package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.RegionCode;
import hello.yuhanTrip.domain.Room;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.MemberLikeService;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodation")
@Log4j2
public class AccommodationController {

    private final AccommodationService accommodationService;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final MemberLikeService memberLikeService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/accommodations")
    public String listAccommodations(Model model,
                                     @RequestParam(required = false) String region,
                                     @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin,
                                     @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout,
                                     @RequestParam(value = "numGuests", required = false) Integer numGuests,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "12") int size) {

        log.info("숙소 리스트를 조회합니다.... 페이지: {}, 사이즈: {}, 지역: {}, 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}", page, size, region, checkin, checkout, numGuests);

        // 페이지 번호와 사이즈 검증
        page = Math.max(page, 0);

        Page<Accommodation> accommodationsPage;

        if (region != null && !region.isEmpty()) {
            // 지역 코드로 숙소 리스트 조회
            Integer areaCode = RegionCode.getCodeByRegion(region);
            if (areaCode == null) {
                log.error("잘못된 지역 이름: {}", region);
                return "error"; // 잘못된 지역 이름 처리
            }

            // 체크인, 체크아웃, 숙박 인원 수가 널이 아닌 경우 필터링을 포함한 조회
            if (checkin != null && checkout != null && numGuests != null) {
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        String.valueOf(areaCode), checkin, checkout, numGuests, page, size);
            } else {
                // 필터링 없이 전체 숙소 조회
                accommodationsPage = accommodationService.getAccommodationsByAreaCode(String.valueOf(areaCode), page, size);
            }

            model.addAttribute("region", region);
        } else {
            // 전체 숙소 리스트 조회
            if (checkin != null && checkout != null && numGuests != null) {
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        null, checkin, checkout, numGuests, page, size);
            } else {
                accommodationsPage = accommodationService.getAccommodations(page, size);
            }
        }

        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;

        // 페이지 번호 범위 계산
        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);

        // 필터링 조건을 모델에 추가
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("numGuests", numGuests);

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "accommodations"; // 뷰 이름
    }


    @GetMapping("/byregion")
    public String listAccommodationsByRegion(Model model,
                                             @RequestParam(value = "region", required = false) String region,
                                             @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin,
                                             @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout,
                                             @RequestParam(value = "numGuests", required = false) Integer numGuests,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "12") int size) {

        log.info("지역 코드로 숙소 리스트를 조회합니다. 지역: {}, 페이지: {}, 사이즈: {}, 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}", region, page, size, checkin, checkout, numGuests);

        // 페이지 번호와 사이즈 검증
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Page<Accommodation> accommodationsPage;

        if (region != null && !region.isEmpty()) {
            // 지역 코드가 제공된 경우
            Integer areaCode = RegionCode.getCodeByRegion(region);
            if (areaCode == null) {
                log.error("잘못된 지역 이름: {}", region);
                return "error"; // 잘못된 지역 이름 처리
            }

            if (checkin != null && checkout != null && numGuests != null) {
                // 체크인, 체크아웃, 숙박 인원 수가 모두 제공된 경우
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        String.valueOf(areaCode), checkin, checkout, numGuests, page, size);
            } else {
                // 필터링 없이 전체 숙소 조회
                accommodationsPage = accommodationService.getAccommodationsByAreaCode(
                        String.valueOf(areaCode), page, size);
            }

            model.addAttribute("region", region);
        } else {
            // 지역 코드가 제공되지 않은 경우
            if (checkin != null && checkout != null && numGuests != null) {
                // 체크인, 체크아웃, 숙박 인원 수가 모두 제공된 경우
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        null, checkin, checkout, numGuests, page, size);
            } else {
                // 필터링 없이 전체 숙소 조회
                accommodationsPage = accommodationService.getAccommodations(page, size);
            }
        }

        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;

        // 페이지 번호 범위 계산
        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        // 날짜를 포맷팅하여 문자열로 변환
        String checkinFormatted = checkin != null ? checkin.format(formatter) : null;
        String checkoutFormatted = checkout != null ? checkout.format(formatter) : null;

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("region", region);
        model.addAttribute("checkin", checkinFormatted);
        model.addAttribute("checkout", checkoutFormatted);
        model.addAttribute("numGuests", numGuests);

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "accommodations"; // 뷰 이름
    }


    @GetMapping("/info")
    public String getAccommodationInfo(@RequestParam("id") Long id, Model model,
                                       @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
                                       @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
                                       @RequestParam(value = "numGuests", required = false) Integer numberOfGuests) {

        log.info("숙소 정보를 가져옵니다... ID: {}", id);
        log.info("선택 사항 - 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}", checkInDate, checkOutDate, numberOfGuests);



        // 숙소 정보를 가져옵니다.
        Accommodation accommodation = accommodationService.getAccommodationInfo(id);

        // 체크인 및 체크아웃 날짜가 제공되지 않는 경우 모든 객실을 가져옵니다.
        List<Room> availableRooms;
        if (checkInDate == null || checkOutDate == null) {
            availableRooms = accommodation.getRooms(); // 모든 객실을 가져옵니다.
        } else {
            // 예약된 객실을 제외한 사용 가능한 객실 리스트 가져오기
            availableRooms = accommodationService.getAvailableRoomsByAccommodation(id, checkInDate, checkOutDate);
        }
        // 모델에 숙소 정보와 사용 가능한 객실 리스트 추가
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("checkin", checkInDate);
        model.addAttribute("checkout", checkOutDate);
        model.addAttribute("numGuests", numberOfGuests);
        model.addAttribute("accommodation", accommodation);


        return "accommodationInfo"; // 상세 페이지의 뷰 이름
    }


    @GetMapping("/memberLikeHistory")
    public String memberLikeHistory(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "2") int size,
            Model model
    ) {
        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Member member = memberService.findByEmail(userDetails.getUsername());

        Pageable pageable = PageRequest.of(page, size);
        Page<Accommodation> likesByMember = memberLikeService.getLikesByMember(member, pageable);

        model.addAttribute("likesByMember", likesByMember);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", likesByMember.getTotalPages());

        return "likesByMember";
    }


}