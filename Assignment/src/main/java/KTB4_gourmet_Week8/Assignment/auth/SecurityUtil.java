package KTB4_gourmet_Week8.Assignment.auth;

import KTB4_gourmet_Week8.Assignment.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long getLoginUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }

        return Long.valueOf(authentication.getPrincipal().toString());
    }

    public static void validateLoginUser(Long targetUserId) {
        Long loginUserId = getLoginUserId();

        if (!loginUserId.equals(targetUserId)) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
    }
}