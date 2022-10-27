package com.kob.backend.service.impl.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserMapper userMapper;

    String[] photos = {
            "https://cdn.acwing.com/media/user/profile/photo/238380_lg_3db6489b4f.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/238390_lg_41a48d154f.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/125398_lg_7f23d55559.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/139102_lg_901507e454.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/144446_lg_4da0fc7c25.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/218415_lg_2800789124.jpg",
    };

    @Override
    public Map<String, String> register(String username, String password, String confirmedPassword) {
        HashMap<String, String> map = new HashMap<>();

        if (username == null) {
            map.put("error_message", "用户名不能为空");
            return map;
        }

        if (password == null || confirmedPassword == null) {
            map.put("error_message", "密码不能为空");
            return map;
        }

        username = username.trim();

        if (username.length() == 0) {
            map.put("error_message", "用户名不能为空");
            return map;
        }

        if (username.length() > 100) {
            map.put("error_message", "用户名长度不能大于100");
            return map;
        }

        if (password.length() == 0 || confirmedPassword.length() == 0) {
            map.put("error_message", "密码不能为空");
            return map;
        }

        if (password.length() > 100 || confirmedPassword.length() > 100) {
            map.put("error_message", "密码长度不能大于100");
            return map;
        }

        if (!password.equals(confirmedPassword)) {
            map.put("error_message", "密码不一致");
            return map;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        List<User> users = userMapper.selectList(queryWrapper);
        if (!users.isEmpty()) {
            map.put("error_message", "用户名已存在");
            return map;
        }

        String encode = passwordEncoder.encode(password);
        String photo = photos[new Random().nextInt(6)];
        User user = new User(null, username, encode, photo, 1500);
        userMapper.insert(user);

        map.put("error_message", "success");
        return map;
    }
}
