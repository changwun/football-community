package com.football.KickBoard.domain.member;

import com.football.KickBoard.common.QuerydslUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;


public class MemberRepositoryImpl implements MemberRepositoryCustom{

  private final JPAQueryFactory queryFactory;

  public MemberRepositoryImpl(EntityManager em){
    this.queryFactory = new JPAQueryFactory(em);
  }
  @Override
  public Page<Member> searchMembers(Boolean activeStatus, String searchKeyword, Pageable pageable){
    QMember member = QMember.member;

    BooleanBuilder builder = new BooleanBuilder();

    //활성화 상태 여부
    if (activeStatus != null) {
      builder.and(member.active.eq(activeStatus));
    }
    //검색조건
    if (searchKeyword != null && !searchKeyword.isBlank()) {
      builder.and(
          member.userId.containsIgnoreCase(searchKeyword)
              .or(member.email.containsIgnoreCase(searchKeyword))
              .or(member.nickname.containsIgnoreCase(searchKeyword))
      );
    }

// 🔎 결과 조회 (페이징)
    List<Member> results = queryFactory
        .selectFrom(member)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(QuerydslUtils.toOrderSpecifier(pageable.getSort(), new PathBuilder<>(Member.class, member.getMetadata().getName()))
            .toArray(new OrderSpecifier[0])) // 정렬 변환 유틸
        .fetch();

    // 전체 카운트 쿼리
    Long total = queryFactory
        .select(member.count())
        .from(member)
        .where(builder)
        .fetchOne();

    return PageableExecutionUtils.getPage(results, pageable, () -> total != null ? total : 0L);
  }
}


