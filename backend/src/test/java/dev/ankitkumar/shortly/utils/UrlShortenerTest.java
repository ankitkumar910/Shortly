package dev.ankitkumar.shortly.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerTest {

    private final UrlShortener urlShortener = new UrlShortener();
    @Test
    void shorten() {
       String encodedText =  urlShortener.shorten(10000L);


        Assertions.assertEquals("1C",urlShortener.shorten(100L));
        Assertions.assertEquals("2Bi",encodedText);

        Assertions.assertThrows(IllegalArgumentException.class,()-> urlShortener.shorten(-1L));
    }

    @Test
    void test1(){
        System.out.println("Hello Ankit!");
    }


}