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
