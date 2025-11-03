package com.football.kick_board.infrastructure.external;

import com.football.kick_board.web.match.model.response.MatchesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class FootballDataClient {

  private final WebClient footballDataWebClient;

  public MatchesResponse getMatches(String leagueCodes, String dateFrom, String dateTo) {

    try {
      return footballDataWebClient.get() // 주입받은 WebClient 사용
          .uri(uriBuilder -> uriBuilder
              .path("/matches")
              .queryParam("competitions", leagueCodes)
              .queryParam("dateFrom", dateFrom)
              .queryParam("dateTo", dateTo)
              .build())
          .retrieve()
          .bodyToMono(MatchesResponse.class)
          .block();
    }catch (Exception e){
      log.error("축구 경기 정보 조회 중 오류 발생", e);
      return new MatchesResponse();//빈 응답 객체 반환
    }

  }
}
