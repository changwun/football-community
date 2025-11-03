package com.football.kick_board.web.match;

import com.football.kick_board.application.match.MatchService;
import com.football.kick_board.web.match.model.response.MatchResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
@Slf4j
public class MatchController {

  private final MatchService matchService;

  //특정 날짜 기준으로 다음 7일간의 경기 일정을 조회하는 엔드포인트입
  @GetMapping("/next-week")
  public ResponseEntity<List<MatchResponse>> getNextWeekMatches(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false, defaultValue = "PL") String leagues) {

    log.info("경기 일정 조회 요청: date={}, leagues={}", date, leagues);

    // 날짜가 지정되지 않았다면 오늘 날짜 사용
    LocalDate targetDate = date != null ? date : LocalDate.now();

    List<MatchResponse> matches = matchService.getMatchesForPeriod(targetDate,
        targetDate.plusDays(7), leagues);

    return ResponseEntity.ok(matches);
  }

  //특정 월의 모든 경기 일정을 조회하는 엔드포인트
  @GetMapping("/monthly")
  public ResponseEntity<List<MatchResponse>> getMonthlyMatches(
      @RequestParam int year,
      @RequestParam int month,
      @RequestParam(required = false, defaultValue = "PL") String leagues) {

    log.info("Request received for monthly matches: year={}, month={}, leagues={}", year, month,
        leagues);

    List<MatchResponse> matches = matchService.getMonthlyMatches(year, month, leagues);
    return ResponseEntity.ok(matches);
  }


}