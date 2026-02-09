package my.aop;

import jakarta.servlet.http.HttpServletRequest;
import my.annotation.RequireRole;
import my.common.exception.ErrorCode;
import my.common.exception.ForbiddenException;
import my.enums.Role;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Aspect
@Component
public class RoleCheckAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();

        String role = (String) request.getAttribute("userRole");

        Role[] permittedRoles = requireRole.value();

        boolean hasPermission = false;
        for (Role permittedRole : permittedRoles) {
            if (permittedRole.name().equals(role)) {
                hasPermission = true;
                break;
            }
        }

        if (!hasPermission) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        if (requireRole.checkOwnership() && Role.BOOK_OWNER.name().equals(role)) {
            checkOwnership(request);
        }
    }

    private void checkOwnership(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVariables == null || !pathVariables.containsKey("id")) {
            return;
        }

        Long pathId = Long.parseLong(pathVariables.get("id"));
        Long userId = (Long) request.getAttribute("userId");

        if (!userId.equals(pathId)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }
    }
}

