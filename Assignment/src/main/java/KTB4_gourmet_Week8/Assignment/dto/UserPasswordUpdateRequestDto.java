package KTB4_gourmet_Week8.Assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserPasswordUpdateRequestDto {

    @NotBlank(message = "password is required")
    @Size(max = 255, message = "password must be 255 characters or less")
    private String password;
}