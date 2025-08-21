package com.football.KickBoard.application.post;

import com.football.KickBoard.domain.member.Member;
import com.football.KickBoard.domain.member.MemberRepository;
import com.football.KickBoard.domain.post.Post;
import com.football.KickBoard.domain.post.PostRepository;
import com.football.KickBoard.web.post.model.request.PostCreateRequest;
import com.football.KickBoard.web.post.model.request.PostListRequest;
import com.football.KickBoard.web.post.model.response.PostResponse;
import com.football.KickBoard.web.post.model.request.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor// final 필드들에 대한 생성자 자동 생성 및 의존성 주입
public class PostServiceImpl implements PostService{

  private final PostRepository postRepository;
  private final MemberRepository memberRepository;

  //게시글 등록
  @Override
  @Transactional
  public PostResponse createdPost(String currentUserId, PostCreateRequest requestDto) {
    Member author = memberRepository.findByUserId(currentUserId)
        .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

    Post post = Post.builder()
        .title(requestDto.getTitle())
        .content(requestDto.getContent())
        .author(author)
        .build();

    Post savedPost = postRepository.save(post);
    return new PostResponse(savedPost);
  }

  //게시글 목록 조회
  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> getPosts(PostListRequest requestDto) {
    Pageable pageable = PageRequest.of(
        requestDto.getPage(),
        requestDto.getSize(),
        Sort.Direction.fromString(requestDto.getSortDirection()),
        requestDto.getSortBy()
    );
    Page<Post> postPage = postRepository.searchPosts(
        requestDto.getKeyword(),
        requestDto.getActiveStatus(),
        pageable
    );
    return postPage.map(PostResponse::new);
  }

  //게시글 상세 조회
  @Override
  @Transactional
  public PostResponse getPostById(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    if (!post.isActive()) {
      throw new IllegalArgumentException("삭제되었거나 비활성 상태인 게시글입니다.");
    }
    post.increaseViewCount();
    return new PostResponse(post);

  }

  //게시글 수정
  @Override
  @Transactional
  public PostResponse updatePost(String currentUserId, Long postId,
      PostUpdateRequest requestDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    if (!post.isActive()) {
      throw new IllegalArgumentException("삭제되었거나 비활성 상태인 게시글은 수정할 수 없습니다.");
    }
    if (!post.getAuthor().getUserId().equals(currentUserId)) {
      throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
    }
    post.update(requestDto.getTitle(), requestDto.getContent());
    return new PostResponse(post);
  }

  //게시글 삭제(소프트 삭제)
  @Override
  @Transactional
  public void deletePost(String currentUserId, Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    if (!post.isActive()) {
      throw new IllegalArgumentException("이미 삭제 된 게시글입니다.");
    }
    if (!post.getAuthor().getUserId().equals(currentUserId)) {
      throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
    }
    post.deactivate();
  }

  //이름도 구분해서 소문자로 바꾸기 _언더바나
  //파인드바이유저 아이디 보다 익시스트? 다 가져오기 때문에
  // 로그인을 동일한 아이디로 동일하게 한다면 동시에 가입하면 에러로 처리 할 수도 있음(사용자 입장에서는 사용 중 아이디로 확인.)
  // 빌더 대신 엑세서스+ 체인 해가지고 트루
  // 멤버에서 롤은(멤버리스폰스디티오ㅓ) 이넘으로 바꿔가지고 사용
  // 비밀번호 더블 체크 변경 같은 경우 서버가 체크가 하는 경우가 많음(최근은) , f2가 하는경우가 많음
  //패스워드패인지("탈퇴확인") 리퀘스트 디티오 -> 한번 더 체크가 f2에서 하는 경우가 많음
  //로깅 필터 수정 필요.(빼고 적용)로거 처리 같은경우 @S4F뭐시기 어노테이션 사용
  //글로벌 익셉션 -> 메세지 필드 , 디테일 필드 따로 만들어서 반환하는게 좋을 것 같음.(세분화,구조화 된 익스턴스 필요)
  //내부 외부 코드 분류
  //만약 서비스 구현체 따로 분리하는게 하나씩이면 서비스 로직 안에서 한번에 처리 해도 되긴 함
  // 매 클래스 마다 커런트유저 아이디 구현 안했으면 좋겠 차라리 따로 빼던가 해도 되니깐.
  // 회원가입 내용중에 컨트롤러 에서 비밀번호 암호화 같은 경우 한번만 쓰이기 때문에 변수로 만들어서 쓰기 보다는 빌더에서 바로 쓰는게 좋음.
  // final 자주 사용하는거 웬만하면 다 붙이는게 낫긴함.

}

