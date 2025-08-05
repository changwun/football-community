package com.football.KickBoard.application.member;

import com.football.KickBoard.domain.member.Member;
import com.football.KickBoard.domain.member.MemberRepository;
import com.football.KickBoard.web.member.dto.MemberSignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

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


