package KTB4_gourmet_Week8.Assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResultDto {

    private LoginResponseDto response;

    private String refreshToken;
}