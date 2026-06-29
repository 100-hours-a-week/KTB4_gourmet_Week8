package KTB4_gourmet_Week8.Assignment.controller;

import KTB4_gourmet_Week8.Assignment.dto.PostLikeResponseDto;
import KTB4_gourmet_Week8.Assignment.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @GetMapping("/posts/{postId}/likes/users/{userId}")
    public PostLikeResponseDto getLikeStatus(
            @PathVariable Long postId,
            @PathVariable Long userId
    ) {
        return postLikeService.getLikeStatus(postId, userId);
    }

    @PostMapping("/posts/{postId}/likes/users/{userId}")
    public PostLikeResponseDto toggleLike(
            @PathVariable Long postId,
            @PathVariable Long userId
    ) {
        return postLikeService.toggleLike(postId, userId);
    }
}