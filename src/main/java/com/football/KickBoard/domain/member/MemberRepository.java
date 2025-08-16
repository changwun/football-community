package com.football.KickBoard.domain.member;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByUserId(String userId);

  //활성 상태별 회원 조회
  Page<Member> findByActive(boolean active, Pageable pageable);
  //검색어로 회원 조회(유저아이디, 이메일, 닉네임 중 하나라도 포함)
  Page<Member> findByUserIdContainingOrEmailContainingOrNicknameContaining(
      String userId, String email,String nickname,Pageable pageable);
  //활성 상태와 검색어 모두 적용한 회원 조회
  Page<Member> findByActiveAndUserIdContainingOrActiveAndEmailContainingOrActiveAndNicknameContaining(
      boolean active1, String userId,
      boolean active2, String email,
      boolean active3, String nickname,
      Pageable pageable);

}
