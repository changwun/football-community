package com.football.kick_board.web.match;

import com.football.kick_board.application.match.MatchService;
import com.football.kick_board.web.match.model.response.MatchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "5. 경기 일정 (Match) API", description = "외부 API(V2.0) 또는 DB/Redis 캐시(V2.0)에서 경기 일정을 조회하는 API")
public class MatchController {

  private final MatchService matchService;

  //특정 날짜 기준으로 다음 7일간의 경기 일정을 조회하는 엔드포인트입
  @GetMapping("/next-week")
  @Operation(summary = "주간 경기 일정 조회", description = "(비로그인 가능) 지정된 날짜(기본값: 오늘)로부터 7일간의 경기 일정을 조회합니다.\n\n- (V2.0) 이 데이터는 Redis(L2) 또는 DB(L1)에 캐시된 데이터입니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공 (경기 일정 목록 반환)")
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
  @Operation(summary = "월간 경기 일정 조회", description = "(비로그인 가능) 지정된 연도/월의 경기 일정을 조회합니다.\n\n- (V2.0) 이 데이터는 Redis(L2) 또는 DB(L1)에 캐시된 데이터입니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공 (경기 일정 목록 반환)")
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