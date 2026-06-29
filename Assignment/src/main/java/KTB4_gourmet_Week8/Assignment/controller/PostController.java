package KTB4_gourmet_Week8.Assignment.controller;

import KTB4_gourmet_Week8.Assignment.dto.PostCreateRequestDto;
import KTB4_gourmet_Week8.Assignment.dto.PostPageResponseDto;
import KTB4_gourmet_Week8.Assignment.dto.PostResponseDto;
import KTB4_gourmet_Week8.Assignment.dto.PostUpdateRequestDto;
import KTB4_gourmet_Week8.Assignment.service.PostService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;

    @PostMapping(value = "/users/{userId}/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponseDto createPost(
            @PathVariable Long userId,

            @RequestParam
            @NotBlank(message = "title is required")
            @Size(max = 100, message = "title must be 100 characters or less")
            String title,

            @RequestParam
            @NotBlank(message = "content is required")
            @Size(max = 65535, message = "content must be 65535 characters or less")
            String content,

            @RequestPart(required = false) List<MultipartFile> images
    ) {
        PostCreateRequestDto request = new PostCreateRequestDto(title, content);

        return postService.createPost(userId, request, images);
    }

    @GetMapping("/posts")
    public PostPageResponseDto getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getPosts(page, size);
    }

    @GetMapping("/posts/{postId}")
    public PostResponseDto getPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId
    ) {
        return postService.getPost(postId, userId);
    }

    @PatchMapping(value = "/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostResponseDto updatePost(
            @PathVariable Long postId,

            @RequestParam
            @NotBlank(message = "title is required")
            @Size(max = 100, message = "title must be 100 characters or less")
            String title,

            @RequestParam
            @NotBlank(message = "content is required")
            @Size(max = 65535, message = "content must be 65535 characters or less")
            String content,

            @RequestPart(required = false) List<MultipartFile> images
    ) {
        PostUpdateRequestDto request = new PostUpdateRequestDto(title, content);

        return postService.updatePost(postId, request, images);
    }

    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }
}