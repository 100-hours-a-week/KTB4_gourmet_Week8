package KTB4_gourmet_Week8.Assignment.controller;

import KTB4_gourmet_Week8.Assignment.dto.LoginRequestDto;
import KTB4_gourmet_Week8.Assignment.dto.UserPageResponseDto;
import KTB4_gourmet_Week8.Assignment.dto.UserPasswordUpdateRequestDto;
import KTB4_gourmet_Week8.Assignment.dto.UserResponseDto;
import KTB4_gourmet_Week8.Assignment.dto.UserSignupRequestDto;
import KTB4_gourmet_Week8.Assignment.dto.UserUpdateRequestDto;
import KTB4_gourmet_Week8.Assignment.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto signup(
            @RequestParam
            @NotBlank(message = "email is required")
            @Email(message = "email format is invalid")
            @Size(max = 100, message = "email must be 100 characters or less")
            String email,

            @RequestParam
            @NotBlank(message = "password is required")
            @Size(max = 255, message = "password must be 255 characters or less")
            String password,

            @RequestParam
            @NotBlank(message = "nickname is required")
            @Size(max = 50, message = "nickname must be 50 characters or less")
            String nickname,

            @RequestPart(required = false) MultipartFile profileImage
    ) {
        UserSignupRequestDto request = new UserSignupRequestDto(email, password, nickname);

        return userService.signup(request, profileImage);
    }

    @PostMapping("/login")
    public UserResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return userService.login(request);
    }

    @GetMapping
    public UserPageResponseDto getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.getUsers(page, size);
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto updateUser(
            @PathVariable Long userId,

            @RequestParam
            @NotBlank(message = "nickname is required")
            @Size(max = 50, message = "nickname must be 50 characters or less")
            String nickname,

            @RequestPart(required = false) MultipartFile profileImage
    ) {
        UserUpdateRequestDto request = new UserUpdateRequestDto(nickname);

        return userService.updateUser(userId, request, profileImage);
    }

    @PatchMapping("/{userId}/password")
    public UserResponseDto updatePassword(
            @PathVariable Long userId,
            @Valid @RequestBody UserPasswordUpdateRequestDto request
    ) {
        return userService.updatePassword(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}