package com.kob.backend.consumer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAnthentication;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    public static final ConcurrentHashMap<Integer, WebSocketServer> usersPool = new ConcurrentHashMap<>();
    private User user;
    private Session session = null;
    public Game game = null;
    public static UserMapper userMapper;
    public static RecordMapper recordMapper;

    public static BotMapper botMapper;
    public static RestTemplate restTemplate;
    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";



    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
    }
    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        this.session = session;
        Integer userId = JwtAnthentication.getUserId(token);
        this.user = userMapper.selectById(userId);

        if (user != null) {
            usersPool.put(userId, this);
        } else {
            session.close();
        }
    }

    @OnClose
    public void onClose() {
        if (this.user != null) {
            usersPool.remove(this.user.getId());
        }
    }

    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {
        User a = userMapper.selectById(aId);
        User b = userMapper.selectById(bId);
        Bot aBot = botMapper.selectById(aBotId);
        Bot bBot = botMapper.selectById(bBotId);

        Game game = new Game(13, 14, 20, a.getId(), aBot, b.getId(), bBot);
        game.createGraph();

        new Thread(game).start();

        if (usersPool.get(a.getId()) != null) {
            usersPool.get(a.getId()).setGame(game);
        }
        if (usersPool.get(b.getId()) != null) {
            usersPool.get(b.getId()).setGame(game);
        }

        JSONObject respGame = new JSONObject();
        respGame.put("graph", game.getGraph());
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());

        JSONObject respA = new JSONObject();
        respA.put("event", "matching-success");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        if (usersPool.get(a.getId()) != null) {
            usersPool.get(a.getId()).send(respA.toJSONString());
        }

        JSONObject respB = new JSONObject();
        respB.put("event", "matching-success");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        if (usersPool.get(b.getId()) != null) {
            usersPool.get(b.getId()).send(respB.toJSONString());
        }
    }

    private void startMatching(Integer botId) {
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", user.getId().toString());
        data.add("rating", user.getRating().toString());
        data.add("bot_id", botId.toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }

    private void stopMatching() {
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    private void move(Integer d) {
        if (game.getPlayerA().getId().equals(user.getId()) && game.getPlayerA().getBotId().equals(-1)) {
            game.setNextStepA(d);
        } else if (game.getPlayerB().getId().equals(user.getId()) && game.getPlayerB().getBotId().equals(-1)) {
            game.setNextStepB(d);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject data = JSON.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            startMatching(data.getInteger("bot_id"));
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void send(String message) {
        synchronized (usersPool) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}