package com.kob.botrunningsystem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool implements Runnable{
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Queue<Bot> botQueue = new LinkedList<>();

    public void addBot(Integer userId, String code, String gameInfo) {
        lock.lock();
        try {
            botQueue.add(new Bot(userId, code, gameInfo));
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void consume(Bot bot) {
        new Consumer().startTimeout(2000, bot);
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();
            if (botQueue.isEmpty()) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            } else {
                Bot bot = botQueue.remove();
                lock.unlock();
                consume(bot);
            }
        }
    }
}
