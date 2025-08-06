package com.football.KickBoard.application.member;

import com.football.KickBoard.common.security.JwtTokenProvider;
import com.football.KickBoard.domain.member.Member;
import com.football.KickBoard.domain.member.MemberRepository;
import com.football.KickBoard.web.member.dto.MemberLoginRequestDto;
import com.football.KickBoard.web.member.dto.MemberLoginResponseDto;
import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public String getCurrentUserId(){
    return (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }


  @Override
  public MemberLoginResponseDto login(MemberLoginRequestDto requestDto){
    Member member = memberRepository.findByuserId(requestDto.getUserId())
        .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 아이디 입니다."));
//비밀번호는 위와 같이 findByuserId처럼 DB에서 직접찾는형식 x개인정보
  if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword() )){
    throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
  }
  //JWT토큰 생성
    String token = jwtTokenProvider.generateToken(member.getUserId());

    return MemberLoginResponseDto.builder()
        .userId(member.getUserId())
        .token(token)
        .build();

  }


  //회원가입 내용
  @Override
  public void signup(MemberSignupRequestDto requestDto) {

    //중복검사
    memberRepository.findByuserId(requestDto.getUserId()).ifPresent(member ->{
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
  }

}


