package dev.ankitkumar.shortly.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

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
}
