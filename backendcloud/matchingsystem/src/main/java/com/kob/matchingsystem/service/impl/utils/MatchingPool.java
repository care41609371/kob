package com.kob.matchingsystem.service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MatchingPool implements Runnable {
    private List<Player> players = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private static RestTemplate restTemplate;
    private final static String startGameUrl = "http://127.0.0.1:3000/pk/start/game/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        MatchingPool.restTemplate = restTemplate;
    }

    public void addPlayer(Integer userId, Integer rating, Integer botId) {
        lock.lock();
        try {
            boolean has = false;
            for (Player player : players) {
                if (player.getUserId().equals(userId)) {
                    has = true;
                    player.setWaitingTime(0);
                    break;
                }
            }

            if (!has) {
                players.add(new Player(userId, rating, botId, 0));
            }
        } finally {
            lock.unlock();
        }
    }

    public void removePlayer(Integer userId) {
        List<Player> newPlayers = new ArrayList<>();
        lock.lock();
        try {
            for (Player player : players) {
                if (!player.getUserId().equals(userId)) {
                    newPlayers.add(player);
                }
            }
            players = newPlayers;
        } finally {
            lock.unlock();
        }
    }

    private void increasingWaitingTime() {
        for (Player player : players) {
            player.setWaitingTime(player.getWaitingTime() + 1);
        }
    }

    private boolean checkMatched(Player a, Player b) {
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTimeDelta = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return waitingTimeDelta * 10 >= ratingDelta;
    }

    private void sendResult(Player a, Player b) {
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", a.getUserId().toString());
        data.add("a_bot_id", a.getBotId().toString());
        data.add("b_id", b.getUserId().toString());
        data.add("b_bot_id", b.getBotId().toString());
        restTemplate.postForObject(startGameUrl, data, String.class);
    }

    private void matchPlayers() {
        boolean[] used = new boolean[players.size()];
        for (int i = 0; i < players.size(); i++) {
            if (!used[i]) {
                for (int j = i + 1; j < players.size(); j++) {
                    Player a = players.get(i);
                    Player b = players.get(j);
                    if (!used[j] && checkMatched(a, b)) {
                        sendResult(a, b);
                        used[i] = used[j] = true;
                        break;
                    }
                }
            }
        }

        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (!used[i]) {
                newPlayers.add(players.get(i));
            }
        }
        players = newPlayers;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                increasingWaitingTime();
                matchPlayers();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
