package com.football.kick_board.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.football.kick_board.application.match.MatchService;
import com.football.kick_board.infrastructure.external.FootballDataClient;
import com.football.kick_board.web.match.model.response.MatchResponse;
import com.football.kick_board.web.match.model.response.MatchesResponse;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

  @InjectMocks
  private MatchService matchService;

  @Mock
  private FootballDataClient footballDataClient;

  @Test
  @DisplayName("경기 조회 - 성공")
  void getMatchesForPeriod_Success() {
    // [Given] (준비)
    LocalDate start = LocalDate.of(2025, 11, 10);
    LocalDate end = LocalDate.of(2025, 11, 17);
    String leagues = "PL";

    // 1. 외부 API의 '가짜' 응답(MatchesResponse) 생성
    MatchesResponse.Match mockApiMatch = new MatchesResponse.Match();
    // (MatchesResponse.Match에 Setter가 없다고 가정하고 ReflectionTestUtils 사용)
    org.springframework.test.util.ReflectionTestUtils.setField(mockApiMatch, "id", 123L);
    org.springframework.test.util.ReflectionTestUtils.setField(mockApiMatch, "status", "SCHEDULED");
    // ... (필요한 다른 가짜 데이터 세팅) ...

    MatchesResponse mockApiResponse = new MatchesResponse();
    org.springframework.test.util.ReflectionTestUtils.setField(mockApiResponse, "matches", List.of(mockApiMatch));

    // 2. [핵심] footballDataClient.getMatches가 호출되면 '가짜' 응답을 반환
    given(footballDataClient.getMatches(leagues, "2025-11-10", "2025-11-17"))
        .willReturn(mockApiResponse);

    // [When] (실행)
    List<MatchResponse> result = matchService.getMatchesForPeriod(start, end, leagues);

    // [Then] (검증)
    // 1. DTO(MatchResponse)로 잘 변환되었는지 검증
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getMatchId()).isEqualTo(123L);
    assertThat(result.get(0).getStatus()).isEqualTo("SCHEDULED");
  }

  @Test
  @DisplayName("경기 조회 - 실패 (외부 API 예외 발생)")
  void getMatchesForPeriod_Fail_ApiThrowsException() {
    // [Given]
    LocalDate start = LocalDate.of(2025, 11, 10);
    LocalDate end = LocalDate.of(2025, 11, 17);
    String leagues = "PL";

    // 1. [핵심] footballDataClient.getMatches가 호출되면 'Exception'을 던짐
    given(footballDataClient.getMatches(leagues, "2025-11-10", "2025-11-17"))
        .willThrow(new RuntimeException("API 통신 오류"));

    // [When]
    List<MatchResponse> result = matchService.getMatchesForPeriod(start, end, leagues);

    // [Then]
    // 2. [핵심] Service의 try-catch가 예외를 잡고 '빈 리스트'를 반환하는지 검증
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("경기 조회 - 실패 (외부 API가 null 반환)")
  void getMatchesForPeriod_Fail_ApiReturnsNull() {
    // [Given]
    LocalDate start = LocalDate.of(2025, 11, 10);
    LocalDate end = LocalDate.of(2025, 11, 17);
    String leagues = "PL";

    // 1. [핵심] footballDataClient.getMatches가 'null'을 반환
    given(footballDataClient.getMatches(leagues, "2025-11-10", "2025-11-17"))
        .willReturn(null);

    // [When]
    List<MatchResponse> result = matchService.getMatchesForPeriod(start, end, leagues);

    // [Then]
    // 2. [핵심] Service의 null-check 로직이 '빈 리스트'를 반환하는지 검증
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }
}
