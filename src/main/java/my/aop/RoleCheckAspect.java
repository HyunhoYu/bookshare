package my.aop;

import jakarta.servlet.http.HttpServletRequest;
import my.annotation.RequireRole;
import my.common.exception.ErrorCode;
import my.common.exception.ForbiddenException;
import my.enums.Role;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RoleCheckAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {

       ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();

        String role = (String) request.getAttribute("userRole");

        Role[] permittedRoles = requireRole.value();

        for (Role permittedRole : permittedRoles) {
            if (permittedRole.name().equals(role)) return;
        }

        throw new ForbiddenException(ErrorCode.FORBIDDEN);

    }
}

