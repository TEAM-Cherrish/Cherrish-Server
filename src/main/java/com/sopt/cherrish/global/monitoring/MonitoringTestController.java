package com.sopt.cherrish.global.monitoring;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring/test")
@Profile({"local", "dev"})
public class MonitoringTestController {

    @GetMapping("/error")
    public void testError() {
        throw new RuntimeException("Test error for monitoring alert");
    }

    @GetMapping("/slow")
    public String testSlow() throws InterruptedException {
        Thread.sleep(2000);
        return "slow response";
    }
}
