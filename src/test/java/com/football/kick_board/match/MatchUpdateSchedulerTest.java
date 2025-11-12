package com.football.kick_board.match;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.football.kick_board.common.scheduler.match.MatchUpdateScheduler;
import com.football.kick_board.domain.match.SoccerMatchRepository;
import com.football.kick_board.infrastructure.external.FootballDataClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchUpdateSchedulerTest {

  @InjectMocks
  private MatchUpdateScheduler matchUpdateScheduler;

  @Mock
  private FootballDataClient footballDataClient; // [!!] Scheduler가 의존
  @Mock
  private SoccerMatchRepository soccerMatchRepository; // [!!] Scheduler가 의존

  // [!!] 님이 삭제하려던 테스트가 여기로 이사왔습니다. [!!]
  @Test
  @DisplayName("스케줄러 - 실패 (외부 API 예외 발생)")
  void updateMatchesBatch_Fail_ApiThrowsException() {
    // [Given]
    // 1. [핵심] footballDataClient.getMatches가 호출되면 'Exception'을 던짐
    given(footballDataClient.getMatches(anyString(), anyString(), anyString()))
        .willThrow(new RuntimeException("API 통신 오류"));

    // [When]
    matchUpdateScheduler.updateMatchesBatch();

    // [Then]
    // 2. [핵심] Scheduler의 try-catch가 예외를 잡고,
    //    DB에는 아무것도 '저장(saveAll)'하지 않았는지 검증
    verify(soccerMatchRepository, never()).saveAll(any());
    // (로그가 찍혔는지는 Mockito로 검증하기 어렵지만, 로직이 중단되지 않았음을 확인)
  }

  // [!!] 님이 삭제하려던 테스트가 여기로 이사왔습니다. [!!]
  @Test
  @DisplayName("스케줄러 - 실패 (외부 API가 null 반환)")
  void updateMatchesBatch_Fail_ApiReturnsNull() {
    // [Given]
    // 1. [핵심] footballDataClient.getMatches가 'null'을 반환
    given(footballDataClient.getMatches(anyString(), anyString(), anyString()))
        .willReturn(null);

    // [When]
    matchUpdateScheduler.updateMatchesBatch();

    // [Then]
    // 2. [핵심] Service의 null-check 로직이 동작하여,
    //    DB에는 아무것도 '저장(saveAll)'하지 않았는지 검증
    verify(soccerMatchRepository, never()).saveAll(any());
  }
}