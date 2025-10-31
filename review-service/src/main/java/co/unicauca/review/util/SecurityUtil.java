package co.unicauca.review.util;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class SecurityUtil {

    public static Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                try {
                    return Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Invalid X-User-Id header");
                }
            }
        }
        throw new IllegalStateException("X-User-Id header not found");
    }

    public static String getCurrentUserRole() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String roleHeader = request.getHeader("X-User-Role");
            if (roleHeader != null && !roleHeader.isEmpty()) {
                return roleHeader;
            }
        }
        throw new IllegalStateException("X-User-Role header not found");
    }

    public static String getCurrentUserEmail() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String emailHeader = request.getHeader("X-User-Email");
            if (emailHeader != null && !emailHeader.isEmpty()) {
                return emailHeader;
            }
        }
        return null;
    }

    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}

