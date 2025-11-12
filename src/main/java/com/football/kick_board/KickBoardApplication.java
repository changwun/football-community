package com.football.kick_board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class KickBoardApplication {

  public static void main(String[] args) {
    SpringApplication.run(KickBoardApplication.class, args);
  }
  //엔티티 만들 때 필요하지 않거나 사용하지 않는 어노테이션은 지우는게 나음(확인 후 실 사용하는 것만 체크 후 나머지 삭제)
  // 빌더 대신 @엑세서스 + 체인 해가지고 트루(개발 순위 후순위)
  //포스트 까지는 서비스 체계 변경 완료 이후 코멘트, 라이크, 매치 서비스에서 엔티티 불러오는 작업해야함.
}
