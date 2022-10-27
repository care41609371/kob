package com.kob.backend.service.impl.record;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import com.kob.backend.service.record.GetRecordListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GetRecordListServiceImpl implements GetRecordListService {
    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public JSONObject getRecordList(Integer page) {
        int pageCapacity = 10;
        IPage<Record> recordPage = new Page<>(page, pageCapacity);
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        List<Record> records = recordMapper.selectPage(recordPage, queryWrapper).getRecords();

        List<JSONObject> items = new LinkedList<>();
        for (Record record : records) {
            User userA = userMapper.selectById(record.getAId());
            User userB = userMapper.selectById(record.getBId());

            JSONObject item = new JSONObject();
            item.put("a_username", userA.getUsername());
            item.put("b_username", userB.getUsername());
            item.put("a_photo", userA.getPhoto());
            item.put("b_photo", userB.getPhoto());
            item.put("record", record);
            String result = "平局";
            if ("A".equals(record.getLoser())) {
                result = "B胜";
            }
            if ("B".equals(record.getLoser())) {
                result = "A胜";
            }
            item.put("result", result);

            items.add(item);
        }

        JSONObject resp = new JSONObject();
        resp.put("records", items);
        resp.put("page_capacity", pageCapacity);
        resp.put("records_count", recordMapper.selectCount(null));
        return resp;
    }
}
