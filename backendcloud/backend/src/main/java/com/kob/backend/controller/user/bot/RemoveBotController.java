package com.kob.backend.controller.user.bot;

import com.kob.backend.service.user.Bot.RemoveBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RemoveBotController {
    @Autowired
    public RemoveBotService removeBotService;

    @PostMapping("/api/user/bot/remove/")
    public Map<String, String> removeBot(@RequestParam Map<String, String> data) {
        return removeBotService.removeBot(data);
    }
}
