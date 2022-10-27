package com.kob.backend.service.impl.ranklist;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import com.kob.backend.service.ranklist.GetRankListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetRankListServiceImpl implements GetRankListService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public JSONObject getRankList(Integer page) {
        int pageCapacity = 10;
        IPage<User> userPage = new Page<>(page, pageCapacity);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("rating");
        List<User> users = userMapper.selectPage(userPage, queryWrapper).getRecords();
        for (User user : users) {
            user.setPassword("");
        }
        JSONObject resp = new JSONObject();
        resp.put("page_capacity", pageCapacity);
        resp.put("users", users);
        resp.put("users_count", userMapper.selectCount(null));
        return resp;
    }
}
