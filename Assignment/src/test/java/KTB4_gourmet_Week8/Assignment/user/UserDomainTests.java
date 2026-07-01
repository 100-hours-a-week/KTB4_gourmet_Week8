package KTB4_gourmet_Week8.Assignment.user;

import KTB4_gourmet_Week8.Assignment.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("유저 도메인 테스트")
public class UserDomainTests {

    @Test
    @DisplayName("회원 객체를 생성하면 이메일, 비밀번호, 닉네임, 프로필 이미지가 저장된다")
    public void create_user_success() {
        // given
        String email = "test@test.com";
        String password = "Test1234!";
        String nickname = "tester";
        String profileImage = "/uploads/profile/test.png";

        // when
        User user = new User(email, password, nickname, profileImage);

        // then
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(nickname, user.getNickname());
        assertEquals(profileImage, user.getProfileImage());
    }

    @Test
    @DisplayName("회원 닉네임을 수정할 수 있다")
    public void update_nickname_success() {
        // given
        User user = new User(
                "test@test.com",
                "Test1234!",
                "beforeNickname",
                "/uploads/profile/test.png"
        );

        // when
        user.update("afterNickname");

        // then
        assertEquals("afterNickname", user.getNickname());
    }

    @Test
    @DisplayName("회원 프로필 이미지를 수정할 수 있다")
    public void update_profile_image_success() {
        // given
        User user = new User(
                "test@test.com",
                "Test1234!",
                "tester",
                "/uploads/profile/old.png"
        );

        // when
        user.updateProfileImage("/uploads/profile/new.png");

        // then
        assertEquals("/uploads/profile/new.png", user.getProfileImage());
    }

    @Test
    @DisplayName("회원 비밀번호를 수정할 수 있다")
    public void update_password_success() {
        // given
        User user = new User(
                "test@test.com",
                "Test1234!",
                "tester",
                "/uploads/profile/test.png"
        );

        // when
        user.updatePassword("NewTest1234!");

        // then
        assertEquals("NewTest1234!", user.getPassword());
    }

    @Test
    @DisplayName("회원 탈퇴 시 삭제 시간이 기록되고 개인정보가 탈퇴 회원 상태로 변경된다")
    public void delete_user_success() {
        // given
        User user = new User(
                "test@test.com",
                "Test1234!",
                "tester",
                "/uploads/profile/test.png"
        );

        // when
        user.delete();

        // then
        assertNotNull(user.getDeletedAt());
        assertEquals("DELETED_USER", user.getPassword());
        assertNull(user.getProfileImage());

        assertTrue(user.getEmail().startsWith("deleted_user_"));
        assertTrue(user.getEmail().endsWith("@deleted.local"));
        assertTrue(user.getNickname().startsWith("탈퇴회원_"));
    }
}