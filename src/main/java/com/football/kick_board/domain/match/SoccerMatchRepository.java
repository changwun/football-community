package com.football.kick_board.domain.match;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoccerMatchRepository extends JpaRepository<SoccerMatch, Long> {

  // MatchService에서 사용할 '기간별 조회' 쿼리 메서드
  List<SoccerMatch> findAllByMatchDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  // (선택) 스케줄러가 오래된 데이터를 삭제할 때 사용할 쿼리 메서드
  void deleteAllByMatchDateBefore(LocalDateTime date);
}