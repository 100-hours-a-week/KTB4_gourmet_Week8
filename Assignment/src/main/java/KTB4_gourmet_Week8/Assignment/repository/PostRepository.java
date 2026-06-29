/*
package KTB4_gourmet_Week7.Assignment.repository;

import KTB4_gourmet_Week7.Assignment.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
*/

package KTB4_gourmet_Week8.Assignment.repository;

import KTB4_gourmet_Week8.Assignment.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    //List<Post> findAllByOrderByIdAsc(); 게시글 목록 조회에서는 내림차순으로 최신순 정렬로 함.

    List<Post> findByUser_IdOrderByIdAsc(Long userId);
}