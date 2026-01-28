package com.sopt.cherrish.global.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 현재 인증된 사용자 정보를 주입받기 위한 커스텀 어노테이션.
 *
 * <p>컨트롤러 메서드 파라미터에 사용하여 {@link UserPrincipal}을 주입받습니다.</p>
 *
 * <pre>{@code
 * @GetMapping("/me")
 * public ResponseEntity<?> getMe(@CurrentUser UserPrincipal userPrincipal) {
 *     Long userId = userPrincipal.getUserId();
 *     // ...
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentUser {
}
