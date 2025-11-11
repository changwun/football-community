package com.football.kick_board.match;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.football.kick_board.application.match.MatchService;
import com.football.kick_board.web.match.model.response.MatchResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional // H2 DB 롤백 (이 테스트는 DB를 안쓰지만, 일관성을 위해)
@TestPropertySource(properties = { // H2 DB 강제
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@SpringBootTest
@AutoConfigureMockMvc
class MatchControllerTest {

  @Autowired
  private MockMvc mockMvc;

  // [!!핵심!!]
  // MatchService는 외부 API를 호출하므로, 컨트롤러 테스트에서는
  // Service 자체를 가짜(@MockitoBean)로 만듭니다.
  @org.springframework.test.context.bean.override.mockito.MockitoBean
  private MatchService matchService;

  @Test
  @DisplayName("[성공] 다음 주 경기 조회 (기본값)")
  void getNextWeekMatches_Success_Default() throws Exception {
    // [Given] (준비)
    // 1. '가짜' MatchService가 반환할 '가짜' 응답 생성
    MatchResponse mockResponse = MatchResponse.builder()
        .matchId(123L)
        .homeTeamName("Team A")
        .awayTeamName("Team B")
        .build();
    List<MatchResponse> responseList = List.of(mockResponse);

    // 2. [핵심] '가짜' Service가 이 데이터를 반환하도록 설정
    // (LocalDate.now()는 테스트 시점에 따라 달라지므로 any(LocalDate.class) 사용)
    given(matchService.getMatchesForPeriod(any(LocalDate.class), any(LocalDate.class), anyString()))
        .willReturn(responseList);

    // [When & Then] (실행 및 검증)
    mockMvc.perform(
            get("/api/matches/next-week") // (파라미터 없음)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].matchId").value(123L))
        .andExpect(jsonPath("$[0].homeTeamName").value("Team A"))
        .andDo(print());
  }

  @Test
  @DisplayName("[성공] 월간 경기 조회")
  void getMonthlyMatches_Success() throws Exception {
    // [Given]
    // 1. '가짜' MatchService가 반환할 '가짜' 응답 생성
    MatchResponse mockResponse = MatchResponse.builder()
        .matchId(456L)
        .homeTeamName("Team C")
        .awayTeamName("Team D")
        .build();
    List<MatchResponse> responseList = List.of(mockResponse);

    // 2. [핵심] '가짜' Service가 이 데이터를 반환하도록 설정
    given(matchService.getMonthlyMatches(2025, 11, "PL"))
        .willReturn(responseList);

    // [When & Then]
    mockMvc.perform(
            get("/api/matches/monthly")
                .param("year", "2025")
                .param("month", "11")
                .param("leagues", "PL")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].matchId").value(456L))
        .andExpect(jsonPath("$[0].homeTeamName").value("Team C"))
        .andDo(print());
  }
}