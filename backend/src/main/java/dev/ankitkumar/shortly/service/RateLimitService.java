package dev.ankitkumar.shortly.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@AllArgsConstructor
public class RateLimitService {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 120;

    private StringRedisTemplate redisTemplate;

    public boolean isAllowed(String ip){

        String key = "rate:" + ip;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {

            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        return count <= MAX_REQUESTS;
    }
}
