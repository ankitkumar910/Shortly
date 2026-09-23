package dev.ankitkumar.shortly.service;

import dev.ankitkumar.shortly.entity.ShortUrl;
import dev.ankitkumar.shortly.exception.DuplicateEntryException;
import dev.ankitkumar.shortly.exception.InvalidShortCodeException;
import dev.ankitkumar.shortly.exception.LongUrlNotFoundException;
import dev.ankitkumar.shortly.repository.UrlRepository;
import dev.ankitkumar.shortly.utils.UrlShortener;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;


@Slf4j
@Service
@Validated
public class UrlService {
    private final UrlRepository repository;
    private final UrlShortener urlShortener;
    private final RedisService redisService;
    @Value("${app.shortly.shorten.host}")
    private String domainName;

    public UrlService(UrlRepository repository, UrlShortener urlShortener, RedisService redisService) {
        this.repository = repository;
        this.urlShortener = urlShortener;
        this.redisService = redisService;
    }


    public String shorten(String url, @Pattern(regexp = "^[0-9a-zA-Z]+", message = "Invalid short code provided.") String customShortCode, LocalDateTime expireAt) {


        if (url.isBlank() || !(url.startsWith("https://") || url.startsWith("http://")))
            throw new IllegalArgumentException("Long url is not valid.");
        ShortUrl shortUrl1 = new ShortUrl();
        System.out.println("CustomShortCode : " + customShortCode);
        shortUrl1.setLongUrl(url);

        ShortUrl shortUrl2 = repository.save(shortUrl1);


        if (customShortCode != null && !customShortCode.isBlank()) {
            if (repository.existsByShortUrl(customShortCode))
                throw new DuplicateEntryException("Provided custom short code is not available.");
            shortUrl1.setShortUrl(customShortCode);

        } else {
            String encodedText = urlShortener.shorten(shortUrl2.getId());
            shortUrl1.setShortUrl(encodedText);
        }


        repository.save(shortUrl2);

        return domainName + "/" + shortUrl1.getShortUrl();
    }

    public String longUrl(@NotEmpty(message = "Short code is required.")  @Pattern(regexp = "^[0-9a-zA-Z]+", message = "Invalid short code provided.") String shortCode) {


        //simulateDelay();
        if (redisService.get(shortCode) != null) return redisService.get(shortCode);

        Long id = UrlShortener.extractId(shortCode);

        if (id == -1) throw new InvalidShortCodeException("Provided ShortenCode is invalid.");
        ShortUrl shortUrl;
        if (repository.existsById(id)) {
            shortUrl = repository.findById(id).orElseThrow(() -> new LongUrlNotFoundException("Url not found corresponding to " + shortCode));

        } else {
            shortUrl = repository.findByShortUrl(shortCode).orElseThrow(() -> new LongUrlNotFoundException("Url not found corresponding to " + shortCode));
        }


        //increase the click count
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        repository.save(shortUrl);

        // save to redis
        redisService.add(shortCode,shortUrl.getLongUrl());

        return shortUrl.getLongUrl();
    }

    private void simulateDelay() {
        System.out.println("Delay Started.Thread : " + Thread.currentThread().getName());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        System.out.println("Delay end.Thread : " + Thread.currentThread().getName());
    }
}
