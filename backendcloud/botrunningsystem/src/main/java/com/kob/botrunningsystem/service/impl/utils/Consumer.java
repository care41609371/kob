package com.kob.botrunningsystem.service.impl.utils;

import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class Consumer extends Thread{
    private Bot bot;
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    public void startTimeout(long time, Bot bot) {
        this.bot = bot;
        this.start();

        try {
            this.join(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt();
        }
    }

    private String getCodeWithUuid(String code, String uuid) {
        int k = code.indexOf(" implements java.util.function.Supplier<Integer>");
        return code.substring(0, k) + uuid + code.substring(k);
    }

    @Override
    public void run() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        Supplier<Integer> botInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.Bot" + uuid,
                getCodeWithUuid(bot.getCode(), uuid)
        ).create().get();

        File file = new File("input.txt");
        try (PrintWriter fout = new PrintWriter(file)) {
            fout.println(bot.getGameInfo());
            fout.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Integer move = botInterface.get();

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("direction", move.toString());
        data.add("user_id", bot.getUserId().toString());
        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
}
