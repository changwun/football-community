package com.football.kick_board.application.match;

import com.football.kick_board.domain.match.SoccerMatch;
import com.football.kick_board.domain.match.SoccerMatchRepository;
import com.football.kick_board.web.match.model.response.MatchResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter; // [!!] 1. IMPORT 추가
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MatchService {

  private final SoccerMatchRepository soccerMatchRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  // [!!] 2. 날짜 포맷터를 미리 정의합니다 (e.g., "2025-11-12 21:00")
  private static final DateTimeFormatter DTO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


  public List<MatchResponse> getMatchesForPeriod(LocalDate startDate, LocalDate endDate, String leagueCodes) {

    String cacheKey = "matches::" + startDate.toString() + "::" + endDate.toString() + "::" + leagueCodes;

    try {
      @SuppressWarnings("unchecked")
      List<MatchResponse> cachedMatches = (List<MatchResponse>) redisTemplate.opsForValue().get(cacheKey);

      if (cachedMatches != null && !cachedMatches.isEmpty()) {
        log.info("Cache HIT: Redis에서 경기 일정을 반환합니다. key={}", cacheKey);
        return cachedMatches;
      }
    } catch (Exception e) {
      log.warn("Redis 캐시 조회 중 오류 발생: {}", e.getMessage());
    }

    log.info("Cache MISS: DB에서 경기 일정을 조회합니다. key={}", cacheKey);
    try {
      LocalDateTime startDateTime = startDate.atStartOfDay();
      LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

      List<SoccerMatch> matches = soccerMatchRepository.findAllByMatchDateBetween(startDateTime, endDateTime);

      List<MatchResponse> responseList = matches.stream()
          .map(this::entityToResponse)
          .collect(Collectors.toList());

      try {
        redisTemplate.opsForValue().set(cacheKey, responseList, Duration.ofHours(1));
        log.info("Redis 캐시 저장 완료. key={}", cacheKey);
      } catch (Exception e) {
        log.warn("Redis 캐시 저장 중 오류 발생: {}", e.getMessage());
      }

      return responseList;

    } catch (Exception e) {
      log.error("DB에서 경기 일정 조회 중 오류 발생", e);
      return Collections.emptyList();
    }
  }


  // 월간 경기 조회
  public List<MatchResponse> getMonthlyMatches(int year, int month, String leagueCodes) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1).minusDays(1);
    return getMatchesForPeriod(startDate, endDate, leagueCodes);
  }

  // 6. DB Entity(SoccerMatch)를 DTO(MatchResponse)로 변환하는 헬퍼 메서드
  private MatchResponse entityToResponse(SoccerMatch entity) {

    // [!!] 4. LocalDateTime -> String 포맷팅 [!!]
    String formattedMatchDate = (entity.getMatchDate() != null)
        ? entity.getMatchDate().format(DTO_DATE_FORMATTER)
        : null;

    return MatchResponse.builder()
        .matchId(entity.getMatchId())
        .matchDate(formattedMatchDate) // [!!] 포맷팅된 String을 사용
        .status(entity.getStatus())
        .competitionName(entity.getLeagueName())
        .homeTeamName(entity.getHomeTeam())
        .awayTeamName(entity.getAwayTeam())
        .homeScore(entity.getHomeScore())
        .awayScore(entity.getAwayScore())
        .matchday(entity.getRound())
        .homeCrest(entity.getHomeCrest())
        .awayCrest(entity.getAwayCrest())
        .build();
  }
}