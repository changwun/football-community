package com.football.kick_board.application.member;

import com.football.kick_board.common.security.JwtTokenProvider;
import com.football.kick_board.domain.member.LoginHistoryRepository;
import com.football.kick_board.domain.member.Member;
import com.football.kick_board.domain.member.MemberRepository;
import com.football.kick_board.domain.member.Role;
import com.football.kick_board.web.member.model.request.MemberListRequest;
import com.football.kick_board.web.member.model.response.MemberListResponse;
import com.football.kick_board.web.member.model.request.MemberLoginRequest;
import com.football.kick_board.web.member.model.response.MemberLoginResponse;
import com.football.kick_board.web.member.model.response.MemberResponse;
import com.football.kick_board.web.member.model.request.MemberSignupRequest;
import com.football.kick_board.web.member.model.request.MemberWithdrawRequest;
import com.football.kick_board.web.member.model.request.PasswordChangeRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  private final LoginHistoryRepository loginHistoryRepository;


  //회원 리스트 검색(관리자 기능)
  @Override
  public Page<MemberListResponse> getMemberListForAdmin(MemberListRequest requestDto) {
    Pageable pageable = requestDto.toPageable();

    Page<Member> memberPage = memberRepository.searchMembers(
        requestDto.getActiveStatus(),  // Boolean
        requestDto.getSearchKeyword(), // String
        pageable
    );

    return memberPage.map(MemberListResponse::new);
  }

  //회원 탈퇴 서비스 구현
  @Override
  @Transactional
  public void withdrawMember(String userId, MemberWithdrawRequest requestDto) {
    //회원 조회
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

    if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    member.deactivate();
  }

  @Override
  @Transactional
  public void changePassword(String userId, PasswordChangeRequest requestDto) {
    //회원 조회
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

    //현재 비밀번호 확인
    if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())) {
      throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
    }
    //새 비밀번호 확인 비밀번호 일치 확인
    if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
      throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    }
    //비밀번호 변경
    String encodedPassword = passwordEncoder.encode(requestDto.getNewPassword());
    member.updatePassword(encodedPassword);

  }


  //본인 정보 조회(UserId 기준)
  @Override
  @Transactional(readOnly = true)
  public MemberResponse getMemberInfo(String userId) {
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

    return MemberResponse.fromEntity(member);

  }

  //관리자용 : PK로 조회(권한 체크 포함)
  @Override
  @Transactional(readOnly = true)
  public MemberResponse getMemberInfoByIdForAdmin(Long id) {
    //대상 회원 조회
    Member target = memberRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("대상 화원을 찾을 수 없습니다."));
    return MemberResponse.fromEntity(target);
  }

  //관리자용: userId로 조회
  @Override
  @Transactional(readOnly = true)
  public MemberResponse getMemberInfoByUserIdForAdmin(String userId) {

    Member target = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("대상 회원을 찾을 수 없습니다."));
    return MemberResponse.fromEntity(target);
  }




  @Override
  public String getCurrentUserId() {
    String currentUser = (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    return currentUser;
  }


  @Override
  public MemberLoginResponse login(MemberLoginRequest requestDto, HttpServletRequest request) {

    Member member = memberRepository.findByUserId(requestDto.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디 입니다."));

    //회원이 비활성 상태인지 확인
    if (!member.isActive()) {
      throw new IllegalArgumentException("탈퇴되었거나 비활성 상태인 계정입니다.");
    }

    //비밀번호는 위와 같이 findByuserId처럼 DB에서 직접찾는형식 x개인정보
    if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    //마지막 로그인 시간 업데이트
    member.updateLastLoginAt();
    memberRepository.save(member);

    //JWT토큰 생성
    String token = jwtTokenProvider.generateToken(member.getUserId());

    return MemberLoginResponse.builder()
        .userId(member.getUserId())
        .token(token)
        .build();

  }






  //회원가입 내용
  @Override
  public void signup(MemberSignupRequest requestDto) {
    //중복검사
    memberRepository.findByUserId(requestDto.getUserId()).ifPresent(member -> {
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
        .createdAt(LocalDateTime.now())
        .build();

    // 저장
    memberRepository.save(member);
  }

}


