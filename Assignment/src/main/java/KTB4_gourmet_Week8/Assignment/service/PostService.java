package KTB4_gourmet_Week8.Assignment.service;

import KTB4_gourmet_Week8.Assignment.dto.PostCreateRequestDto;
import KTB4_gourmet_Week8.Assignment.dto.PostPageResponseDto;
import KTB4_gourmet_Week8.Assignment.dto.PostResponseDto;
import KTB4_gourmet_Week8.Assignment.dto.PostUpdateRequestDto;
import KTB4_gourmet_Week8.Assignment.entity.Post;
import KTB4_gourmet_Week8.Assignment.entity.PostImage;
import KTB4_gourmet_Week8.Assignment.entity.PostView;
import KTB4_gourmet_Week8.Assignment.entity.User;
import KTB4_gourmet_Week8.Assignment.exception.PostNotFoundException;
import KTB4_gourmet_Week8.Assignment.exception.UserNotFoundException;
import KTB4_gourmet_Week8.Assignment.repository.CommentRepository;
import KTB4_gourmet_Week8.Assignment.repository.PostImageRepository;
import KTB4_gourmet_Week8.Assignment.repository.PostLikeRepository;
import KTB4_gourmet_Week8.Assignment.repository.PostRepository;
import KTB4_gourmet_Week8.Assignment.repository.PostViewRepository;
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
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostImageRepository postImageRepository;
    private final PostViewRepository postViewRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public PostResponseDto createPost(
            Long userId,
            PostCreateRequestDto request,
            List<MultipartFile> images
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));

        Post post = new Post(
                user,
                request.getTitle(),
                request.getContent()
        );

        Post savedPost = postRepository.save(post);

        if (images != null && !images.isEmpty()) {
            int sortOrder = 0;

            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) {
                    continue;
                }

                String imageUrl = fileStorageService.saveFile(image, "posts");

                boolean thumbnail = sortOrder == 0;

                PostImage postImage = new PostImage(
                        savedPost,
                        imageUrl,
                        sortOrder,
                        thumbnail
                );

                postImageRepository.save(postImage);

                sortOrder++;
            }
        }

        return createPostResponseDto(savedPost);
    }

    public PostPageResponseDto getPosts(int page, int size) {
        Page<Post> postPage = postRepository.findAll(
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        List<PostResponseDto> content = postPage.getContent()
                .stream()
                .map(this::createPostResponseDto)
                .toList();

        return new PostPageResponseDto(
                content,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.hasNext(),
                postPage.hasPrevious()
        );
    }

    @Transactional
    public PostResponseDto getPost(Long postId, Long userId) {
        Post post = findPostById(postId);

        increaseViewCountIfFirstView(post, userId);

        return createPostResponseDto(post);
    }

    @Transactional
    public PostResponseDto updatePost(
            Long postId,
            PostUpdateRequestDto request,
            List<MultipartFile> images
    ) {
        Post post = findPostById(postId);

        post.update(
                request.getTitle(),
                request.getContent()
        );

        if (hasUploadedImages(images)) {
            postImageRepository.deleteByPost_Id(postId);

            for (int index = 0; index < images.size(); index++) {
                MultipartFile image = images.get(index);

                if (image == null || image.isEmpty()) {
                    continue;
                }

                String imageUrl = fileStorageService.saveFile(image, "posts");
                boolean thumbnail = index == 0;

                PostImage postImage = new PostImage(post, imageUrl, index, thumbnail);
                postImageRepository.save(postImage);
            }
        }

        return createPostResponseDto(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = findPostById(postId);

        postImageRepository.deleteByPost_Id(postId);
        postLikeRepository.deleteByPost_Id(postId);
        commentRepository.deleteByPost_Id(postId);
        postViewRepository.deleteByPost_Id(postId);

        postRepository.delete(post);
    }

    private boolean hasUploadedImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return false;
        }

        return images.stream().anyMatch(image -> image != null && !image.isEmpty());
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
    }

    private void increaseViewCountIfFirstView(Post post, Long userId) {
        if (userId == null) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));

        boolean alreadyViewed = postViewRepository.existsByUser_IdAndPost_Id(userId, post.getId());

        if (alreadyViewed) {
            return;
        }

        PostView postView = new PostView(user, post);
        postViewRepository.save(postView);

        post.increaseViewCount();
    }

    private PostResponseDto createPostResponseDto(Post post) {
        long likeCount = postLikeRepository.countByPost_Id(post.getId());
        long commentCount = commentRepository.countByPost_Id(post.getId());

        List<String> imageUrls = postImageRepository
                .findByPost_IdAndDeletedAtIsNullOrderBySortOrderAsc(post.getId())
                .stream()
                .map(PostImage::getImageUrl)
                .toList();

        return new PostResponseDto(post, likeCount, commentCount, imageUrls);
    }
}