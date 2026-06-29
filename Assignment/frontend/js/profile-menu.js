const headerProfileButton = document.querySelector("#header-profile-button");
const profileDropdown = document.querySelector("#profile-dropdown");

const goEditProfile = document.querySelector("#go-edit-profile");
const goEditPassword = document.querySelector("#go-edit-password");
const logoutButton = document.querySelector("#logout-button");


if (headerProfileButton && profileDropdown) {
    headerProfileButton.addEventListener("click", function (event) {
        event.stopPropagation();

        const userId = localStorage.getItem("userId");

        if (!userId) {
            alert("로그인이 필요합니다.");
            window.location.href = "./login.html";
            return;
        }

        profileDropdown.classList.toggle("show");
    });

    document.addEventListener("click", function () {
        profileDropdown.classList.remove("show");
    });

    profileDropdown.addEventListener("click", function (event) {
        event.stopPropagation();
    });
}

if (goEditProfile) {
    goEditProfile.addEventListener("click", function () {
        window.location.href = "./edit-profile.html";
    });
}

if (goEditPassword) {
    goEditPassword.addEventListener("click", function () {
        window.location.href = "./edit-password.html";
    });
}

if (logoutButton) {
    logoutButton.addEventListener("click", function () {
        localStorage.clear();
        window.location.href = "./login.html";
    });
}

function renderHeaderProfileImage() {
    const profileImage = localStorage.getItem("profileImage");

    if (!headerProfileButton || !profileImage) {
        return;
    }

    headerProfileButton.style.backgroundImage = `url(${API_BASE_URL}${profileImage})`;
    headerProfileButton.style.backgroundSize = "cover";
    headerProfileButton.style.backgroundPosition = "center";
}

renderHeaderProfileImage();