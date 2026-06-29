package KTB4_gourmet_Week8.Assignment.service;

import KTB4_gourmet_Week8.Assignment.dto.LoginRequestDto;
import KTB4_gourmet_Week8.Assignment.dto.UserListResponseDto;
import KTB4_gourmet_Week8.Assignment.dto.UserPageResponseDto;
import KTB4_gourmet_Week8.Assignment.dto.UserPasswordUpdateRequestDto;
import KTB4_gourmet_Week8.Assignment.dto.UserResponseDto;
import KTB4_gourmet_Week8.Assignment.dto.UserSignupRequestDto;
import KTB4_gourmet_Week8.Assignment.dto.UserUpdateRequestDto;
import KTB4_gourmet_Week8.Assignment.entity.User;
import KTB4_gourmet_Week8.Assignment.exception.DuplicateEmailException;
import KTB4_gourmet_Week8.Assignment.exception.DuplicateNicknameException;
import KTB4_gourmet_Week8.Assignment.exception.InvalidLoginException;
import KTB4_gourmet_Week8.Assignment.exception.UserNotFoundException;
import KTB4_gourmet_Week8.Assignment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public UserResponseDto signup(UserSignupRequestDto request, MultipartFile profileImage) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }

        String profileImageUrl = fileStorageService.saveFile(profileImage, "profile");

        User user = new User(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                profileImageUrl
        );

        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser);
    }

    public UserResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmailAndPassword(
                request.getEmail(),
                request.getPassword()
        ).orElseThrow(() -> new InvalidLoginException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (user.getDeletedAt() != null) {
            throw new InvalidLoginException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return new UserResponseDto(user);
    }

    public UserPageResponseDto getUsers(int page, int size) {
        Page<User> userPage = userRepository.findAll(
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.ASC, "id")
                )
        );

        List<UserListResponseDto> content = userPage.getContent()
                .stream()
                .map(UserListResponseDto::new)
                .toList();

        return new UserPageResponseDto(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    public UserResponseDto getUser(Long userId) {
        User user = findUserById(userId);

        return new UserResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto request, MultipartFile profileImage) {
        User user = findUserById(userId);

        if (userRepository.existsByNicknameAndIdNot(request.getNickname(), userId)) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }

        user.update(request.getNickname());

        String profileImageUrl = fileStorageService.saveFile(profileImage, "profile");

        if (profileImageUrl != null) {
            user.updateProfileImage(profileImageUrl);
        }

        return new UserResponseDto(user);
    }

    @Transactional
    public UserResponseDto updatePassword(Long userId, UserPasswordUpdateRequestDto request) {
        User user = findUserById(userId);

        user.updatePassword(request.getPassword());

        return new UserResponseDto(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);

        user.delete();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));
    }
}