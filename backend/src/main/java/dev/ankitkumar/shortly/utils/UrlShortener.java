package dev.ankitkumar.shortly.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UrlShortener {




    private static final String CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;

    public static Long extractId(String shortCode) {

        if (shortCode == null || shortCode.isEmpty()) {
            return -1L;
        }


        long id = 0;

        for (int i = 0; i < shortCode.length(); i++) {

            char c = shortCode.charAt(i);
            int value = CHARS.indexOf(c);

            if (value == -1) {
                log.warn("Invalid character: {}", c);
                throw new IllegalArgumentException("Invalid character in short code: " + c);
            }

            id = id * BASE + value;
        }

        return id;
    }


    public String shorten(Long id) {

        if (id < 0) throw new IllegalArgumentException(
                "Invalid arguments."
        );

        if (id == 0) return "0";

        StringBuilder sb = new StringBuilder();
        long num = id;

        while (num > 0) {
            int remainder = (int) (num % BASE);
            sb.append(CHARS.charAt(remainder));
            num /= BASE;
        }

        return sb.reverse().toString();
    }


}
