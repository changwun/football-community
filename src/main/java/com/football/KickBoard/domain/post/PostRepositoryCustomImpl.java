package com.football.KickBoard.domain.post;

import com.football.KickBoard.common.QuerydslUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

@Repository
public class PostRepositoryCustomImpl extends QuerydslRepositorySupport implements
    PostRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public PostRepositoryCustomImpl(EntityManager entityManager) {
    super(Post.class);
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  @Override
  public Page<Post> searchPosts(String keyword, Boolean activeStatus, Pageable pageable) {
    QPost post = QPost.post;
    //동작 쿼리 조건 생성
    Predicate[] predicates = createSearchPredicates(keyword, activeStatus, post);
    //쿼리 실행
    List<Post> content = queryFactory
        .selectFrom(post) // Post 엔티티를 선택
        .where(predicates) // 조건 적용
        .offset(pageable.getOffset()) // 페이징 오프셋 (시작 위치)
        .limit(pageable.getPageSize()) // 페이지 크기
        .orderBy(QuerydslUtils.toOrderSpecifier(pageable.getSort(),
                new PathBuilder<>(Post.class, post.getMetadata().getName()))
            .toArray(new OrderSpecifier[0])) // 정렬 변환 유틸)
        .fetch(); // 쿼리 실행 및 결과 리스트 반환

    //전체 개수 쿼리(페이징 위해서)
    long total = queryFactory
        .selectFrom(post)
        .where(predicates)
        .fetchCount();

    //Page 객체로 묶어 반환
    return new PageImpl<>(content, pageable, total);

  }


  private Predicate[] createSearchPredicates(String keyword, Boolean activeStatus, QPost post) {
    List<BooleanExpression> conditions = new ArrayList<>();

    //활성 상태 조건: activeStatus가 null이 아니면 해당 상태만 조회,null인 경우 active=true인 게시글만 검색
    if (activeStatus != null) {
      conditions.add(post.active.eq(activeStatus));
    } else {
      conditions.add(post.active.isTrue());// 기본적으로 활성 게시글만
    }

    //검색 키워드 조건: keyword가 비어있지 않으면 제목 또는 내용에서 검색
    if (keyword != null && !keyword.isEmpty()) {
      conditions.add(post.title.containsIgnoreCase(keyword)
          .or(post.content.containsIgnoreCase(keyword)));
    }

    //조건 리스트를 predicate 배열로 반환
    return conditions.toArray(new Predicate[0]);

  }

}
