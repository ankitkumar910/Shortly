package dev.ankitkumar.shortly.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class RedisService {

    private StringRedisTemplate redisTemplate;

    public String get(String shortCode) {
        String value = redisTemplate.opsForValue().get(shortCode);
        if(value != null) log.info("Value found in Redis: shortCode = {}",shortCode);
        else log.info("Value isn't found in Redis: shortCode = {}",shortCode);

        return value;
    }

    public void add(String shortCode, String longUrl, Instant expiration) {

        if(shortCode == null) return;
        if(longUrl == null) return;

       if(expiration != null){
           Duration duration = Duration.between(Instant.now(),expiration);
           redisTemplate.opsForValue().set(shortCode,longUrl,duration);
       }else {
           redisTemplate.opsForValue().set(shortCode,longUrl);
       }

        log.info("Value added to Redis: shortCode = {}",shortCode);
    }

    public void increaseClickCount(String shortCode) {
        if (shortCode == null) return;
        redisTemplate.opsForValue().increment("click:" + shortCode);
    }

    public Map<String, Long> getAllClickCounts() {


        Map<String,Long > clickCounts = new HashMap<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match("click:*")
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {

            while (cursor.hasNext()) {

                String key = cursor.next();

                String shortCode = key.substring("click:".length());

                String value = redisTemplate.opsForValue().get(key);

                if (value != null) {
                    clickCounts.put(shortCode, Long.parseLong(value));
                    redisTemplate.opsForValue().set(key,"0");

                }
            }
        }

        return clickCounts;
    }
}
