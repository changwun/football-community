package com.football.KickBoard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class KickBoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(KickBoardApplication.class, args);
	}

}
// PR받은거 작업,열었을 때 해야할 작업 멤버리스트 createdAt,updatedAt 자동화
// 머지 후 브랜치 새로 열어서 게시글 작성 엔티티 부터 만들기.