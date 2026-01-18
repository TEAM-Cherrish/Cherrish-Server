package com.sopt.cherrish.global.logging;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Profile({"dev", "local", "test"})
public class TestErrorController {

    @GetMapping("/error")
    public void testError() {
        throw new RuntimeException("Discord 알림 테스트");
    }
}
