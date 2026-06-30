package KTB4_gourmet_Week8.Assignment.dto;

import KTB4_gourmet_Week8.Assignment.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {

    private UserResponseDto user;

    private TokenInfoDto token;

    public static LoginResponseDto of(
            User user,
            String accessToken,
            long expiresIn
    ) {
        return new LoginResponseDto(
                new UserResponseDto(user),
                new TokenInfoDto(accessToken, expiresIn)
        );
    }
}