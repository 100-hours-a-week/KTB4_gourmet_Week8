/*
package KTB4_gourmet_Week7.Assignment.dto;

import lombok.Getter;
import KTB4_gourmet_Week7.Assignment.entity.Post;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorId = post.getAuthor().getId();
    }
}
*/

package KTB4_gourmet_Week8.Assignment.dto;

import KTB4_gourmet_Week8.Assignment.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostResponseDto {

    private final Long id;
    private final Long userId;
    private final String nickname;
    private final String profileImage;
    private final String title;
    private final String content;
    private final int viewCount;
    private final long likeCount;
    private final long commentCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<String> imageUrls;

    public PostResponseDto(Post post) {
        this(post, 0, 0, List.of());
    }

    public PostResponseDto(Post post, long likeCount, long commentCount) {
        this(post, likeCount, commentCount, List.of());
    }

    public PostResponseDto(Post post, long likeCount, long commentCount, List<String> imageUrls) {
        this.id = post.getId();
        this.userId = post.getUserId();
        this.nickname = post.getUser().getDeletedAt() == null
                ? post.getUser().getNickname()
                : "알 수 없음";
        this.profileImage = post.getUser().getDeletedAt() == null
                ? post.getUser().getProfileImage()
                : null;
        this.title = post.getTitle();
        this.content = post.getContent();
        this.viewCount = post.getViewCount();
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.imageUrls = imageUrls;
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}