package com.football.KickBoard.domain.member;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository  extends JpaRepository<LoginHistory, Long> {
  List<LoginHistory> findByMemberIdOrderByLoginAtDesc(Long memberId);
}
