package com.football.kick_board.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.football.kick_board.application.match.MatchService;
import com.football.kick_board.domain.match.SoccerMatch;
import com.football.kick_board.domain.match.SoccerMatchRepository;
import com.football.kick_board.infrastructure.external.FootballDataClient;
import com.football.kick_board.web.match.model.response.MatchResponse;
import com.football.kick_board.web.match.model.response.MatchesResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

  @InjectMocks
  private MatchService matchService;

  @Mock
  private SoccerMatchRepository soccerMatchRepository;
  @Mock
  private RedisTemplate<String, Object> redisTemplate;
  @Mock
  private ValueOperations<String, Object> valueOperations;

  @Test
  @DisplayName("경기 조회 - 성공 (Cache Miss -> DB 조회 -> Cache 저장)")
  void getMatchesForPeriod_Success_CacheMiss() {
    // [Given] (준비)
    LocalDate start = LocalDate.of(2025, 11, 10);
    LocalDate end = LocalDate.of(2025, 11, 17);
    String leagues = "PL";
    String cacheKey = "matches::2025-11-10::2025-11-17::PL";

    // 1. '가짜' DB가 반환할 '가짜' SoccerMatch 엔티티
    SoccerMatch mockMatch = SoccerMatch.builder()
        .matchId(123L)
        .homeTeam("Team A")
        .matchDate(LocalDateTime.now())
        .build();
    List<SoccerMatch> dbResponse = List.of(mockMatch);

    // 2. [핵심] RedisTemplate Mocking 설정
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    // (1) 캐시 조회(get) -> 'null' (Cache Miss) 반환
    given(valueOperations.get(cacheKey)).willReturn(null);

    // 3. [핵심] Repository Mocking 설정
    // (2) DB 조회(findAllBy...) -> 'dbResponse' 반환
    given(soccerMatchRepository.findAllByMatchDateBetween(any(), any()))
        .willReturn(dbResponse);

    // [When] (실행)
    List<MatchResponse> result = matchService.getMatchesForPeriod(start, end, leagues);

    // [Then] (검증)
    // (3) DTO로 변환되었는지 검증
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getMatchId()).isEqualTo(123L);

    // (4) [!!] Redis에 '저장(set)'이 1번 호출되었는지 검증
    verify(valueOperations).set(cacheKey, result, Duration.ofHours(1));
  }


  // [!!] 대신 "DB 조회 실패" 테스트를 추가합니다. [!!]
  @Test
  @DisplayName("경기 조회 - 실패 (DB 조회 중 예외 발생)")
  void getMatchesForPeriod_Fail_DbThrowsException() {
    // [Given] (준비)
    LocalDate start = LocalDate.of(2025, 11, 10);
    LocalDate end = LocalDate.of(2025, 11, 17);
    String leagues = "PL";
    String cacheKey = "matches::2025-11-10::2025-11-17::PL";

    // 1. Redis Mocking (Cache Miss)
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(cacheKey)).willReturn(null); // 캐시 없음

    // 2. [핵심] Repository Mocking (DB가 예외를 던짐)
    given(soccerMatchRepository.findAllByMatchDateBetween(any(), any()))
        .willThrow(new RuntimeException("DB 통신 오류")); // (DAOException 등)

    // [When] (실행)
    List<MatchResponse> result = matchService.getMatchesForPeriod(start, end, leagues);

    // [Then] (검증)
    // 3. [핵심] Service의 try-catch가 예외를 잡고 '빈 리스트'를 반환하는지 검증
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();

    // 4. (보너스) Redis에는 저장(set)을 시도하지 않았는지 검증
    verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
  }
}
