package com.kob.backend.consumer.utils;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game implements Runnable {
    private final Integer rows;
    private final Integer cols;
    private final Integer innerWallsCount;
    private final Integer[][] graph;
    private final static Integer[] dx = {-1, 0, 1, 0};
    private final static Integer[] dy = {0, 1, 0, -1};
    private final Player playerA, playerB;
    private Integer nextStepA = null;
    private Integer nextStepB = null;
    private final ReentrantLock lock = new ReentrantLock();
    private String status = "playing";
    private String loser = null;
    private final static String botRunUrl = "http://127.0.0.1:3002/bot/add/";

    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
             lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    public Game(Integer rows, Integer cols, Integer innerWallsCount, Integer aId, Bot aBot, Integer bId, Bot bBot) {
        this.rows = rows;
        this.cols = cols;
        this.innerWallsCount = innerWallsCount;
        this.graph = new Integer[rows][cols];

        Integer aBotId = -1, bBotId = -1;
        String aBotCode = "", bBotCode = "";

        if (aBot != null) {
            aBotId = aBot.getId();
            aBotCode = aBot.getContent();
        }
        if (bBot != null) {
            bBotId = bBot.getId();
            bBotCode = bBot.getContent();
        }

        playerA = new Player(aId, aBotId, aBotCode, rows - 2, 1, new ArrayList<>());
        playerB = new Player(bId, bBotId, bBotCode, 1, cols - 2, new ArrayList<>());
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public Integer[][] getGraph() {
        return graph;
    }

    private boolean checkConnectivity(int sx, int sy, int tx, int ty) {
        if (tx == sx && ty == sy) {
            return true;
        }
        graph[sx][sy] = 1;

        for (int i = 0; i < 4; i++) {
            int xx = sx + dx[i], yy = sy + dy[i];
            if (xx < 0 || xx >= rows || yy < 0 || yy >= cols || graph[xx][yy] == 1) {
                continue;
            }
            if (checkConnectivity(xx, yy, tx, ty)) {
                graph[sx][sy] = 0;
                return true;
            }
        }

        graph[sx][sy] = 0;
        return false;
    }

    private boolean draw() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                graph[i][j] = 0;
            }
        }

        for (int r = 0; r < rows; r++) {
            graph[r][0] = graph[r][cols - 1] = 1;
        }

        for (int c = 0; c < cols; c++) {
            graph[0][c] = graph[rows - 1][c] = 1;
        }

        Random random = new Random();
        for (int i = 0; i < innerWallsCount / 2; i++) {
            for (int j = 0; j < 1000; j++) {
                int r = random.nextInt(rows);
                int c = random.nextInt(cols);
                if (graph[r][c] == 1 || graph[rows - 1 - r][cols - 1 - c] == 1) continue;
                if (r == rows - 2 && c == 1 || c == cols - 2 && r == 1) continue;

                graph[r][c] = graph[rows - 1 - r][cols - 1 - c] = 1;
                break;
            }
        }

        return checkConnectivity(rows - 2, 1, 1, cols - 2);
    }

    public void createGraph() {
        for (int i = 0; i < 1000; i++) {
            if (draw()) {
                break;
            }
        }
    }

    private String getGameInfo(Player player) { // 编码局面信息
        Player me, you;
        if (player.getId().equals(playerA.getId())) {
            me = playerA;
            you = playerB;
        } else {
            me = playerB;
            you = playerA;
        }
        return getGraphString() + "#"
                + me.getSx() + "#" + me.getSy() + "#(" + me.getStepsString() + ")#"
                + you.getSx() + "#" + you.getSy() + "#(" + you.getStepsString() + ")#";
    }

    private void sendBotCode(Player player) {
        if (player.getBotId().equals(-1)) {
            return;
        }
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getId().toString());
        data.add("code", player.getCode());
        data.add("game_info", getGameInfo(player));
        WebSocketServer.restTemplate.postForObject(botRunUrl, data, String.class);
    }

    private boolean nextStep() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        sendBotCode(playerA);
        sendBotCode(playerB);

        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) {
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private boolean checkValid(List<Cell> a, List<Cell> b) {
        int n = a.size();
        Cell cell = a.get(n - 1);
        if (graph[cell.getX()][cell.getY()] == 1) {
            return false;
        }

        for (int i = 0; i < n - 1; i++) {
            if (cell.equals(a.get(i))) {
                return false;
            }
        }

        for (int i = 0; i < n - 1; i++) {
            if (cell.equals(b.get(i))) {
                return false;
            }
        }

        return true;
    }

    private void judge() {
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();
        boolean validA = checkValid(cellsA, cellsB);
        boolean validB = checkValid(cellsB, cellsA);

        if (!validB || !validA) {
            status = "finished";

            if (!validB && !validA) {
                loser = "all";
            } else if (!validB) {
                loser = "B";
            } else {
                loser = "A";
            }
        }
    }

    private void sendAll(String message) {
        if (WebSocketServer.usersPool.get(playerA.getId()) != null) {
            WebSocketServer.usersPool.get(playerA.getId()).send(message);
        }
        if (WebSocketServer.usersPool.get(playerB.getId()) != null) {
            WebSocketServer.usersPool.get(playerB.getId()).send(message);
        }
    }

    private void sendMove() {
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            sendAll(resp.toJSONString());
            nextStepA = nextStepB = null;
        } finally {
            lock.unlock();
        }
    }

    private String getGraphString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                stringBuilder.append(graph[i][j]);
            }
        }
        return stringBuilder.toString();
    }

    private void updateRating(Player player, Integer rating) {
        User user = WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    private void saveToDatabase() {
        Integer ratingA = WebSocketServer.userMapper.selectById(playerA.getId()).getRating();
        Integer ratingB = WebSocketServer.userMapper.selectById(playerB.getId()).getRating();

        if ("A".equals(loser)) {
            ratingA -= 2;
            ratingB += 5;
        } else if ("B".equals(loser)) {
            ratingA += 5;
            ratingB -= 2;
        }

        updateRating(playerA, ratingA);
        updateRating(playerB, ratingB);

        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerA.getStepsString(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerB.getStepsString(),
                getGraphString(),
                loser,
                new Date()
        );

        WebSocketServer.recordMapper.insert(record);
    }

    private void sendResult() {
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDatabase();
        sendAll(resp.toJSONString());
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {
            if (nextStep()) {
                judge();
                if ("playing".equals(status)) {
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                lock.lock();
                try {
                    if (nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if (nextStepA == null) {
                        loser = "A";
                    } else {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }
    }
}
