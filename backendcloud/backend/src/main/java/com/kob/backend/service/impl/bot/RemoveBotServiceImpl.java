package com.kob.backend.service.impl.bot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.Bot.RemoveBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
public class RemoveBotServiceImpl implements RemoveBotService {
    @Autowired
    private BotMapper botMapper;
    @Override
    public Map<String, String> removeBot(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl)authentication.getPrincipal();
        User user = loginUser.getUser();

        Integer id = Integer.parseInt(data.get("id"));
        Bot bot = botMapper.selectById(id);

        Map<String, String> map = new HashMap<>();

        if (bot == null) {
            map.put("error_message", "Bot不存在或已被删除");
            return map;
        }

        if (!bot.getUserId().equals(user.getId())) {
            map.put("error_message", "没有权限删除该Bot");
            return map;
        }

        botMapper.deleteById(id);
        map.put("error_message", "success");

        return map;
    }
}
