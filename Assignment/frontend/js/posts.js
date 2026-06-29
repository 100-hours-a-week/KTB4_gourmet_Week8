const writeButton = document.querySelector("#write-button");
const postList = document.querySelector("#post-list");

let currentPage = 0;
const pageSize = 10;
let isLoading = false;
let hasMore = true;

function formatCount(count) {
    if (!count) {
        return 0;
    }

    if (count >= 100000) {
        return Math.floor(count / 1000) + "k";
    }

    if (count >= 10000) {
        return Math.floor(count / 1000) + "k";
    }

    if (count >= 1000) {
        return Math.floor(count / 1000) + "k";
    }

    return count;
}

function formatDate(dateValue) {
    if (!dateValue) {
        return "";
    }

    return String(dateValue).replace("T", " ").slice(0, 19);
}

function getPostId(post) {
    return post.postId ?? post.id;
}

function getPostTitle(post) {
    return post.title ?? "제목 없음";
}

function getWriterName(post) {
    return post.nickname ?? post.writer ?? post.writerName ?? post.authorNickname ?? "작성자";
}

function getThumbnailUrl(post) {
    if (!post.imageUrls || post.imageUrls.length === 0) {
        return null;
    }

    return `${API_BASE_URL}${post.imageUrls[0]}`;
}

function getWriterProfileImageUrl(post) {
    const profileImage = post.profileImage ?? post.writerProfileImage ?? post.authorProfileImage;

    if (!profileImage) {
        return null;
    }

    return `${API_BASE_URL}${profileImage}`;
}

function createPostCard(post) {
    const article = document.createElement("article");
    article.className = "post-card";

    const postId = getPostId(post);
    const thumbnailUrl = getThumbnailUrl(post);
    const writerProfileImageUrl = getWriterProfileImageUrl(post);

    article.dataset.postId = postId;

    article.innerHTML = `
        <div class="post-content">
            <div class="post-card-top">
                <div class="post-card-text">
                    <h2 class="post-title">${getPostTitle(post)}</h2>

                    <div class="post-stats">
                        <span>좋아요 ${formatCount(post.likeCount)}</span>
                        <span>댓글 ${formatCount(post.commentCount)}</span>
                        <span>조회수 ${formatCount(post.viewCount)}</span>
                    </div>
                </div>

                ${thumbnailUrl ? `
                    <div class="post-thumbnail-wrapper">
                        <img class="post-thumbnail" src="${thumbnailUrl}" alt="게시글 썸네일">
                    </div>
                ` : ""}
            </div>
        </div>

        <div class="post-writer">
            <div class="writer-image"></div>
            <span class="writer-name">${getWriterName(post)}</span>
            <span class="post-date">${formatDate(post.createdAt)}</span>
        </div>
    `;

    const writerImage = article.querySelector(".writer-image");

    if (writerImage && writerProfileImageUrl) {
        writerImage.style.backgroundImage = `url(${writerProfileImageUrl})`;
        writerImage.style.backgroundSize = "cover";
        writerImage.style.backgroundPosition = "center";
    }

    article.addEventListener("click", function () {
        localStorage.setItem("selectedPostId", postId);
        window.location.href = "./post-detail.html";
    });

    return article;
}

function renderEmptyMessage() {
    postList.innerHTML = `
        <p class="empty-message">아직 작성된 게시글이 없습니다.</p>
    `;
}

async function fetchPosts() {
    if (isLoading || !hasMore) {
        return;
    }

    isLoading = true;

    try {
        const response = await fetch(`${API_BASE_URL}/posts?page=${currentPage}&size=${pageSize}`);

        if (!response.ok) {
            throw new Error("게시글 목록 조회 실패");
        }

        const pageData = await response.json();
        const posts = pageData.content ?? [];

        console.log("게시글 목록 조회 성공:", pageData);

        if (currentPage === 0 && posts.length === 0) {
            renderEmptyMessage();
            hasMore = false;
            return;
        }

        posts.forEach(function (post) {
            const postCard = createPostCard(post);
            postList.appendChild(postCard);
        });

        hasMore = pageData.hasNext;
        currentPage = pageData.page + 1;
    } catch (error) {
        console.error(error);

        if (currentPage === 0) {
            postList.innerHTML = `
                <p class="empty-message">게시글 목록을 불러오지 못했습니다.</p>
            `;
        }
    } finally {
        isLoading = false;
    }
}

function handleInfiniteScroll() {
    const scrollTop = window.scrollY;
    const windowHeight = window.innerHeight;
    const documentHeight = document.documentElement.scrollHeight;

    const isBottom = scrollTop + windowHeight >= documentHeight - 100;

    if (isBottom) {
        fetchPosts();
    }
}

writeButton.addEventListener("click", function () {
    const userId = localStorage.getItem("userId");

    if (!userId) {
        alert("로그인이 필요합니다.");
        window.location.href = "./login.html";
        return;
    }

    window.location.href = "./post-create.html";
});

window.addEventListener("scroll", handleInfiniteScroll);

fetchPosts();