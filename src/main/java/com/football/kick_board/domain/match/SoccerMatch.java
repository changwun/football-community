package com.football.kick_board.domain.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "soccer_match")
public class SoccerMatch {

  @Id
  @Column(name = "match_id")
  private Long matchId; // API가 제공하는 고유 ID를 PK로 사용

  @Column(name = "league_name")
  private String leagueName;

  @Column(name = "match_date")
  private LocalDateTime matchDate;

  @Column(name = "home_team")
  private String homeTeam;

  @Column(name = "away_team")
  private String awayTeam;

  @Column(name = "round")
  private Integer round;

  @Column(name = "stadium")
  private String stadium;

  // e.g., homeCrest, awayCrest, status, homeScore, awayScore 등
  private String status;
  private String homeCrest;
  private String awayCrest;
  private Integer homeScore;
  private Integer awayScore;


  @Builder
  public SoccerMatch(Long matchId, String leagueName, LocalDateTime matchDate, String homeTeam,
      String awayTeam, Integer round, String stadium, String status,
      String homeCrest, String awayCrest, Integer homeScore, Integer awayScore) {
    this.matchId = matchId;
    this.leagueName = leagueName;
    this.matchDate = matchDate;
    this.homeTeam = homeTeam;
    this.awayTeam = awayTeam;
    this.round = round;
    this.stadium = stadium;
    this.status = status;
    this.homeCrest = homeCrest;
    this.awayCrest = awayCrest;
    this.homeScore = homeScore;
    this.awayScore = awayScore;
  }
}