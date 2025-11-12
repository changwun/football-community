package com.football.kick_board.web.match.model.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchesResponse {

  private Object filters;
  private Object resultSet;
  private List<Match> matches;

  @Getter
  @Setter
  public static class Match {

    private Long id;
    private String utcDate;
    private String status;
    private Integer matchday;
    private String stage;
    private String group;

    private Competition competition;
    private Team homeTeam;
    private Team awayTeam;
    private Score score;

    @Getter
    @Setter
    public static class Competition {

      private Long id;
      private String name;
      private String code;
    }

    @Getter
    @Setter
    public static class Team {

      private Long id;
      private String name;
      private String shortName;
      private String tla;
      private String crest;
    }

    @Getter
    @Setter
    public static class Score {

      private FullTime fullTime;

      @Getter
      @Setter
      public static class FullTime {

        private Integer home;
        private Integer away;
      }
    }

  }

}
