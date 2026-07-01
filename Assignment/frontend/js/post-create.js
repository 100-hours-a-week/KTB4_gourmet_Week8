const backButton = document.querySelector("#back-button");

const postCreateForm = document.querySelector("#post-create-form");
const titleInput = document.querySelector("#title");
const contentInput = document.querySelector("#content");
const imageInput = document.querySelector("#post-image");
const fileName = document.querySelector("#file-name");

const helperText = document.querySelector("#form-helper");
const submitButton = document.querySelector("#submit-button");

let selectedImage = null;

function validatePostForm() {
    const title = titleInput.value.trim();
    const content = contentInput.value.trim();

    helperText.textContent = "";

    if (title.length > 0 && content.length > 0) {
        submitButton.disabled = false;
        submitButton.classList.add("active");
        return true;
    }

    submitButton.disabled = true;
    submitButton.classList.remove("active");
    return false;
}

titleInput.addEventListener("input", function () {
    if (titleInput.value.length > 26) {
        titleInput.value = titleInput.value.slice(0, 26);
    }

    validatePostForm();
});

contentInput.addEventListener("input", validatePostForm);

imageInput.addEventListener("change", function () {
    const file = imageInput.files[0];

    if (!file) {
        selectedImage = null;
        fileName.textContent = "파일을 선택해주세요.";
        return;
    }

    selectedImage = file;
    fileName.textContent = file.name;

    console.log("이미지 선택:", selectedImage);
});

postCreateForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const isValid = validatePostForm();

    if (!isValid) {
        helperText.textContent = "* 제목, 내용을 모두 작성해주세요.";
        return;
    }

    const userId = localStorage.getItem("userId");

    if (!userId) {
        alert("로그인이 필요합니다.");
        window.location.href = "./login.html";
        return;
    }

    const title = titleInput.value.trim();
    const content = contentInput.value.trim();

    try {
        const formData = new FormData();

        formData.append("title", title);
        formData.append("content", content);

        if (imageInput.files.length > 0) {
            Array.from(imageInput.files).forEach(function (image) {
                formData.append("images", image);
            });
        }

        const post = await apiFetch(`/users/${userId}/posts`, {
            method: "POST",
            body: formData
        });

        if (!post) {
            return;
        }

        console.log("게시글 작성 성공:", post);

        const postId = post.postId ?? post.id;

        if (postId) {
            localStorage.setItem("selectedPostId", postId);
        }

        alert("게시글이 작성되었습니다.");
        window.location.href = "./posts.html";
    } catch (error) {
        console.error("게시글 작성 요청 오류:", error);
        helperText.textContent = `* ${error?.message ?? "게시글 작성에 실패했습니다."}`;
    }
});

backButton.addEventListener("click", function () {
    window.location.href = "./posts.html";
});