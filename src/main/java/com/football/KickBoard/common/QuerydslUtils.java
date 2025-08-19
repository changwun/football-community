package com.football.KickBoard.common;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

public class QuerydslUtils {

  public static <T> List<OrderSpecifier<?>> toOrderSpecifier(Sort sort, PathBuilder<T> entityPath) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
    sort.forEach(order -> {
      Order direction = order.isAscending() ? Order.ASC : Order.DESC;
      String property = order.getProperty();

      // 예시: 날짜 처리
      if ("createdAt".equals(property)) {
        orderSpecifiers.add(new OrderSpecifier<>(direction,
            entityPath.getDate(property, java.time.LocalDateTime.class)));
      } else {
        // 나머지는 문자열 처리
        orderSpecifiers.add(new OrderSpecifier<>(direction,
            entityPath.getString(property)));
      }
    });
    return orderSpecifiers;
  }
}
