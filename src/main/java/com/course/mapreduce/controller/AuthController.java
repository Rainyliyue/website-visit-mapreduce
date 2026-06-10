package com.course.mapreduce.controller;

import com.course.mapreduce.dto.LoginRequest;
import com.course.mapreduce.dto.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public static final String SESSION_USER = "LOGIN_USER";

    @PostMapping("/login")
    public LoginUser login(@RequestBody LoginRequest request, HttpSession session) {
        if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
            LoginUser user = new LoginUser("admin", "ADMIN");
            session.setAttribute(SESSION_USER, user);
            return user;
        }
        if ("user".equals(request.getUsername()) && "user123".equals(request.getPassword())) {
            LoginUser user = new LoginUser("user", "USER");
            session.setAttribute(SESSION_USER, user);
            return user;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
    }

    @GetMapping("/me")
    public LoginUser me(HttpSession session) {
        Object user = session.getAttribute(SESSION_USER);
        if (user instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Map.of("message", "logout");
    }
}
