package com.football.kick_board.application.match;

import com.football.kick_board.infrastructure.external.FootballDataClient;
import com.football.kick_board.web.match.model.response.MatchResponse;
import com.football.kick_board.web.match.model.response.MatchesResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

  private final FootballDataClient footballDataClient;

  public List<MatchResponse> getMatchesForPeriod(LocalDate startDate, LocalDate endDate, String leagueCodes) {
    try {
      // API 호출
      MatchesResponse response = footballDataClient.getMatches(
          leagueCodes,
          startDate.format(DateTimeFormatter.ISO_DATE),
          endDate.format(DateTimeFormatter.ISO_DATE)
      );

      // 응답 검증
      if (response == null || response.getMatches() == null) {
        return Collections.emptyList();
      }

      // DTO 변환
      return response.getMatches().stream()
          .map(this::toMatchResponse)
          .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("경기 일정 조회 중 오류 발생", e);
      return Collections.emptyList();
    }
  }

  // 다음 주 경기 조회 (7일)
  public List<MatchResponse> getNextWeekMatches(LocalDate startDate, String leagueCodes) {
    LocalDate endDate = startDate.plusDays(7);
    return getMatchesForPeriod(startDate, endDate, leagueCodes);
  }

  // 월간 경기 조회
  public List<MatchResponse> getMonthlyMatches(int year, int month, String leagueCodes) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1).minusDays(1); // 해당 월의 마지막 날
    return getMatchesForPeriod(startDate, endDate, leagueCodes);
  }

  private MatchResponse toMatchResponse(MatchesResponse.Match match) {
    // Score는 null일 수 있으므로 안전하게 처리
    Integer homeScore = null;
    Integer awayScore = null;
    if (match.getScore() != null && match.getScore().getFullTime() != null) {
      homeScore = match.getScore().getFullTime().getHome();
      awayScore = match.getScore().getFullTime().getAway();
    }

    return MatchResponse.builder()
        .matchId(match.getId())
        .matchDate(match.getUtcDate())
        .status(match.getStatus())
        .competitionName(match.getCompetition() != null ? match.getCompetition().getName() : null)
        .homeTeamName(match.getHomeTeam() != null ? match.getHomeTeam().getName() : null)
        .awayTeamName(match.getAwayTeam() != null ? match.getAwayTeam().getName() : null)
        .homeScore(homeScore)
        .awayScore(awayScore)
        .matchday(match.getMatchday())
        .homeCrest(match.getHomeTeam() != null ? match.getHomeTeam().getCrest() : null)
        .awayCrest(match.getAwayTeam() != null ? match.getAwayTeam().getCrest() : null)
        .build();
  }
}