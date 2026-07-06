package com.demo.controller.auth;

import com.demo.common.Result;
import com.demo.dto.LoginDTO;
import com.demo.dto.PasswordChangeDTO;
import com.demo.security.CurrentUserHolder;
import com.demo.security.LoginUser;
import com.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUserHolder holder;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success("登录成功", authService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("已注销", null);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        return Result.success(authService.currentInfo());
    }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(HttpServletRequest request) {
        // refresh 在白名单内，需手动解析旧 token
        LoginUser user = holder.resolve(request.getHeader("Authorization"));
        if (user == null) {
            return Result.error(401, "token 已失效，请重新登录");
        }
        return Result.success("刷新成功", authService.refresh(user.getUserId()));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid PasswordChangeDTO dto) {
        authService.changePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码修改成功，请重新登录", null);
    }
}
