package com.football.KickBoard.application.member;

import com.football.KickBoard.common.security.JwtTokenProvider;
import com.football.KickBoard.domain.member.Member;
import com.football.KickBoard.domain.member.MemberRepository;
import com.football.KickBoard.web.member.dto.MemberLoginRequestDto;
import com.football.KickBoard.web.member.dto.MemberLoginResponseDto;
import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

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

    Member member = memberRepository.findByuserId(requestDto.getUserId())
        .orElseThrow(() -> {
          logger.warn("로그인 실패 - 존재하지 않는 아이디: {}", requestDto.getUserId());
          return new IllegalArgumentException("존재하지 않는 아이디 입니다.");
        });

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
    memberRepository.findByuserId(requestDto.getUserId()).ifPresent(member -> {
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
        .build();

    // 저장
    memberRepository.save(member);
    logger.info("회원가입 성공: userId: {}", requestDto.getUserId());
  }

}


