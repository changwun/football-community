package com.football.kick_board.common.scheduler.match;

import com.football.kick_board.domain.match.SoccerMatch;
import com.football.kick_board.domain.match.SoccerMatchRepository;
import com.football.kick_board.infrastructure.external.FootballDataClient;
import com.football.kick_board.web.match.model.response.MatchesResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchUpdateScheduler {

  private final FootballDataClient footballDataClient;
  private final SoccerMatchRepository soccerMatchRepository;

  // (예시) 매일 새벽 4시 0분에 실행
  @Scheduled(cron = "0 0 4 * * ?")
  @Transactional
  public void updateMatchesBatch() {
    log.info("[Scheduler] 일일 경기 일정 업데이트 작업을 시작합니다.");

    // 1. (선택) 어제 경기 데이터 삭제
    // soccerMatchRepository.deleteAllByMatchDateBefore(LocalDate.now().atStartOfDay());
    // log.info("[Scheduler] 오래된 경기 일정이 삭제되었습니다.");

    // 2. (예시) 오늘부터 +30일간의 'PL' 경기 일정을 가져옴
    LocalDate today = LocalDate.now();
    LocalDate thirtyDaysLater = today.plusDays(30);
    String leagues = "PL"; // (필요한 리그 코드로 수정)

    try {
      // 3. 외부 API 호출
      MatchesResponse response = footballDataClient.getMatches(
          leagues,
          today.toString(),
          thirtyDaysLater.toString()
      );

      if (response == null || response.getMatches() == null) {
        log.warn("[Scheduler] API 응답이 없거나 비어있습니다.");
        return;
      }

      // 4. API 응답(DTO) -> SoccerMatch(Entity)로 변환
      List<SoccerMatch> matchesToSave = response.getMatches().stream()
          .map(this::apiToEntity) // (아래 5번에서 만든 변환 메서드)
          .collect(Collectors.toList());

      // 5. DB에 저장 (PK가 같으면 UPDATE, 없으면 INSERT)
      soccerMatchRepository.saveAll(matchesToSave);

      log.info("[Scheduler] {}건의 경기 일정이 성공적으로 업데이트/저장되었습니다.", matchesToSave.size());

    } catch (Exception e) {
      log.error("[Scheduler] 경기 일정 업데이트 중 오류 발생", e);
    }
  }

  // 5. API 응답(MatchesResponse.Match)을 SoccerMatch(Entity)로 변환하는 헬퍼 메서드
  private SoccerMatch apiToEntity(MatchesResponse.Match apiMatch) {

    Integer homeScore = null;
    Integer awayScore = null;
    if (apiMatch.getScore() != null && apiMatch.getScore().getFullTime() != null) {
      homeScore = apiMatch.getScore().getFullTime().getHome();
      awayScore = apiMatch.getScore().getFullTime().getAway();
    }

    //UtcDate 스트링 반환 시 toLocalDateTime()으로 변환하여 엔티티의 LocalDateTime 필드에 맞게 저장
    LocalDateTime matchDateTime = null;
    if (apiMatch.getUtcDate() != null) {
      matchDateTime = ZonedDateTime.parse(apiMatch.getUtcDate()).toLocalDateTime();
    }

    return SoccerMatch.builder()
        .matchId(apiMatch.getId()) // [!!] PK를 API ID로 직접 설정
        .leagueName(apiMatch.getCompetition() != null ? apiMatch.getCompetition().getName() : null)
        .matchDate(matchDateTime)
        .status(apiMatch.getStatus())
        .homeTeam(apiMatch.getHomeTeam() != null ? apiMatch.getHomeTeam().getName() : null)
        .awayTeam(apiMatch.getAwayTeam() != null ? apiMatch.getAwayTeam().getName() : null)
        .round(apiMatch.getMatchday())
        .stadium(null) // (API 응답에 stadium이 없다면 null 또는 기본값)
        .homeCrest(apiMatch.getHomeTeam() != null ? apiMatch.getHomeTeam().getCrest() : null)
        .awayCrest(apiMatch.getAwayTeam() != null ? apiMatch.getAwayTeam().getCrest() : null)
        .homeScore(homeScore)
        .awayScore(awayScore)
        .build();
  }
}