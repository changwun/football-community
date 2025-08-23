package com.football.kick_board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class KickBoardApplication {

  public static void main(String[] args) {
    SpringApplication.run(KickBoardApplication.class, args);
  }
  //엔티티 만들 때 필요하지 않거나 사용하지 않는 어노테이션은 지우는게 나음(확인 후 실 사용하는 것만 체크 후 나머지 삭제)
  // 빌더 대신 엑세서스 + 체인 해가지고 트루(개발 순위 후순위)
  //글로벌 익셉션 -> 메세지 필드 , 디테일 필드 따로 만들어서 반환하는게 좋을 것 같음.(세분화,구조화 된 익스턴스 필요)
  // 자주 사용하는거 웬만하면 final 다 붙여서 사용하는게 좋다고함.

}
