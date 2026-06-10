package com.course.mapreduce.config;

import com.course.mapreduce.controller.AuthController;
import com.course.mapreduce.dto.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            return true;
        }

        LoginUser user = currentUser(request);
        if (user == null) {
            reject(request, response, HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return false;
        }

        if (isAdminPath(path) && !"ADMIN".equals(user.role())) {
            reject(request, response, HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return false;
        }
        return true;
    }

    private boolean isPublicPath(String path) {
        return path.equals("/login.html")
                || path.equals("/api/auth/login")
                || path.startsWith("/styles.css")
                || path.startsWith("/login.js")
                || path.startsWith("/favicon");
    }

    private boolean isAdminPath(String path) {
        return path.equals("/admin.html") || path.startsWith("/api/admin");
    }

    private LoginUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute(AuthController.SESSION_USER);
        return user instanceof LoginUser loginUser ? loginUser : null;
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, int status, String message) throws IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"" + message + "\"}");
        } else {
            response.sendRedirect("/login.html");
        }
    }
}
