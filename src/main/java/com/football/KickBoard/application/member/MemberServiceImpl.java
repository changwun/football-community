package com.football.KickBoard.application.member;

import com.football.KickBoard.common.security.JwtTokenProvider;
import com.football.KickBoard.domain.member.Member;
import com.football.KickBoard.domain.member.MemberRepository;
import com.football.KickBoard.domain.member.Role;
import com.football.KickBoard.web.member.dto.MemberListRequestDto;
import com.football.KickBoard.web.member.dto.MemberListResponseDto;
import com.football.KickBoard.web.member.dto.MemberLoginRequestDto;
import com.football.KickBoard.web.member.dto.MemberLoginResponseDto;
import com.football.KickBoard.web.member.dto.MemberResponseDto;
import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;
import com.football.KickBoard.web.member.dto.MemberWithdrawRequestDto;
import com.football.KickBoard.web.member.dto.PasswordChangeRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  //회원 리스트 검색(관리자 기능)
  @Override
  @PreAuthorize("hasRole('ADMIN)")
  public Page<MemberListResponseDto> getMemberListForAdmin(MemberListRequestDto requestDto){
    logger.info("관리자 회원 목록 조회 요청: {}",requestDto.toString());

    Pageable pageable = PageRequest.of(
        requestDto.getPage(),
        requestDto.getSize(),
        Sort.Direction.fromString(requestDto.getSortDirection()),
        requestDto.getSortBy()
    );
    Page<Member> memberPage;
    //null이 아니면 서치키워드 반환, null이면 빈 문자열 처리
    String searchKeyword = requestDto.getSearchKeyword() != null ?
        requestDto.getSearchKeyword() : "";
    //검색 조건에 맞춰서 호출
    if (requestDto.getActiveStatus() != null){
      if (!searchKeyword.isEmpty()){
        memberPage = memberRepository.findByActiveAndUserIdContainingOrActiveAndEmailContainingOrActiveAndNicknameContaining(
            requestDto.getActiveStatus(), searchKeyword,
            requestDto.getActiveStatus(), searchKeyword,
            requestDto.getActiveStatus(), searchKeyword,
            pageable
        );
      }else {
        memberPage = memberRepository.findByActive(requestDto.getActiveStatus(),
            pageable);
      }
    } else if (!searchKeyword.isEmpty()) {
      memberPage = memberRepository.findByUserIdContainingOrEmailContainingOrNicknameContaining(
          searchKeyword,searchKeyword,searchKeyword, pageable
      );
    }else {
      memberPage = memberRepository.findAll(pageable);
    }
    return memberPage.map(MemberListResponseDto::new);
  }

  //회원 탈퇴 서비스 구현
  @Override
  @Transactional
  public void withrawMember(String userId, MemberWithdrawRequestDto requestDto) {
    logger.info("회원 탈퇴 시도: userId={}", userId);
    //회원 조회
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> {
          logger.warn("회원 탈퇴 실패 - 존재하지 않는 아이디: {}", userId);
          return new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        });
    if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
      logger.warn("회원 탈퇴 실패 - 비밀번호 불일치: userId={}", userId);
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    member.deactivate();
    logger.info("회원 탈퇴 처리 성공: userId={}", userId);
  }

  @Override
  @Transactional
  public void changePassword(String userId, PasswordChangeRequestDto requestDto) {
    logger.info("비밀번호 변경 시도: userId{}", userId);
    //회원 조회
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> {
          logger.warn("비밀번호 변경 실패 - 존재하지 않는 아이디: {}", userId);
          return new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        });
    //현재 비밀번호 확인
    if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())) {
      logger.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: {}", userId);
      throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
    }
    //새 비밀번호 확인 비밀번호 일치 확인
    if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
      logger.warn("비밀번호 변경 실패 - 새 비밀번호 불일치: {}", userId);
      throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    }
    //비밀번호 변경
    String encodedPassword = passwordEncoder.encode(requestDto.getNewPassword());
    member.updatePassword(encodedPassword);

    logger.info("비밀번호 변경 성공: userId{}", userId);

  }


  //본인 정보 조회(UserId 기준)
  @Override
  @Transactional(readOnly = true)
  public MemberResponseDto getMemberInfo(String userId) {
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

    return toDto(member);

  }

  //관리자용 : PK로 조회(권한 체크 포함)
  @Override
  @Transactional(readOnly = true)
  @PreAuthorize("hasRole('ADMIN')")
  public MemberResponseDto getMemberInfoByIdForAdmin(Long id) {
    //대상 회원 조회
    Member target = memberRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("대상 화원을 찾을 수 없습니다."));
    return toDto(target);
  }

  //관리자용: userId로 조회
  @Override
  @Transactional(readOnly = true)
  @PreAuthorize("hasRole('ADMIN')")
  public MemberResponseDto getMemberInfoByUserIdForAdmin(String userId) {

    Member target = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("대상 회원을 찾을 수 없습니다."));
    return toDto(target);
  }

  //중복되어 사용하는 정보 toDto 처리
  private MemberResponseDto toDto(Member m) {
    return new MemberResponseDto(
        m.getId(),
        m.getUserId(),
        m.getNickname(),
        m.getEmail(),
        m.getFavoriteTeam(),
        m.getRole() != null ? m.getRole().name() : "USER"
    );
  }


  @Override
  public String getCurrentUserId() {
    String currentUser = (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    logger.debug("현재 로그인한 사용자 ID: {}", currentUser);
    return currentUser;
  }


  @Override
  public MemberLoginResponseDto login(MemberLoginRequestDto requestDto) {
    logger.info("로그인 시도: userId: {}", requestDto.getUserId());

    Member member = memberRepository.findByUserId(requestDto.getUserId())
        .orElseThrow(() -> {
          logger.warn("로그인 실패 - 존재하지 않는 아이디: {}", requestDto.getUserId());
          return new IllegalArgumentException("존재하지 않는 아이디 입니다.");
        });
    //회원이 비활성 상태인지 확인
    if (!member.isActive()) {
      logger.warn("로그인 실패 - 비활성/탈퇴된 계정: {}", requestDto.getUserId());
      throw new IllegalArgumentException("탈퇴되었거나 비활성 상태인 계정입니다.");
    }

    //비밀번호는 위와 같이 findByuserId처럼 DB에서 직접찾는형식 x개인정보
    if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
      logger.warn("로그인 실패 - 비밀번호 불일치: {}", requestDto.getUserId());
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    //JWT토큰 생성
    String token = jwtTokenProvider.generateToken(member.getUserId());
    logger.info("로그인 성공: userId: {}", requestDto.getUserId());

    return MemberLoginResponseDto.builder()
        .userId(member.getUserId())
        .token(token)
        .build();

  }


  //회원가입 내용
  @Override
  public void signup(MemberSignupRequestDto requestDto) {
    logger.info("회원가입 시도: userId= {}", requestDto.getUserId());
    //중복검사
    memberRepository.findByUserId(requestDto.getUserId()).ifPresent(member -> {
      logger.warn("회원가입 실패 - 중복된 아이디: {}", requestDto.getUserId());
      throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
    });
    //비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

    //멤버 엔티티 생성
    Member member = Member.builder()
        .userId(requestDto.getUserId())
        .password(encodedPassword)
        .email(requestDto.getEmail())
        .nickname(requestDto.getNickname())
        .favoriteTeam(requestDto.getFavoriteTeam())
        .role(Role.USER) //기본값 일반유저
        .active(true)
        .build();

    // 저장
    memberRepository.save(member);
    logger.info("회원가입 성공: userId: {}", requestDto.getUserId());
  }

}


