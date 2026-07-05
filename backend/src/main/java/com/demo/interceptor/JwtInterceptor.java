package com.demo.interceptor;

import com.demo.common.Result;
import com.demo.common.ResultCode;
import com.demo.security.CurrentUserHolder;
import com.demo.security.LoginUser;
import com.demo.security.RequirePermission;
import com.demo.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final CurrentUserHolder holder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String path = request.getRequestURI();
        if (holder.isWhitelisted(path)) {
            return true;
        }
        LoginUser user = holder.resolve(request.getHeader("Authorization"));
        if (user == null) {
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(holder.writeJson(Result.error(ResultCode.UNAUTHORIZED)));
            return false;
        }
        UserContext.set(user);

        // 方法级权限注解校验
        HandlerMethod hm = (HandlerMethod) handler;
        RequirePermission rp = hm.getMethodAnnotation(RequirePermission.class);
        if (rp != null && !holder.hasPermission(user, rp.value())) {
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(holder.writeJson(Result.error(ResultCode.FORBIDDEN)));
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
