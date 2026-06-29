package KTB4_gourmet_Week8.Assignment.repository;

import KTB4_gourmet_Week8.Assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndIdNot(String nickname, Long userId);

    Optional<User> findByEmailAndPassword(String email, String password);
}