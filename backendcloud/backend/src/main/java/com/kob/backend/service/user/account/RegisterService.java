package com.kob.backend.service.user.account;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface RegisterService {
    Map<String, String> register(String username, String password, String confirmPassword);
}
