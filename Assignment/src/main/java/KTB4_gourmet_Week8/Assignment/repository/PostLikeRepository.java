package KTB4_gourmet_Week8.Assignment.repository;

import KTB4_gourmet_Week8.Assignment.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByUser_IdAndPost_Id(Long userId, Long postId);

    Optional<PostLike> findByUser_IdAndPost_Id(Long userId, Long postId);

    long countByPost_Id(Long postId);

    void deleteByPost_Id(Long postId);

    void deleteByUser_Id(Long userId);
}