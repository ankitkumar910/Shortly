package dev.ankitkumar.shortly.controller;

import dev.ankitkumar.shortly.dto.ResponseDto;
import dev.ankitkumar.shortly.exception.InvalidShortCodeException;
import dev.ankitkumar.shortly.exception.LongUrlNotFoundException;
import dev.ankitkumar.shortly.service.UrlService;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("")
@CrossOrigin(origins = "http://localhost:5173")
@AllArgsConstructor
public class UrlController {

    private UrlService urlService;

    @PostMapping("/api/v1/shorten")
    public ResponseEntity<ResponseDto> shortenUrl(@RequestParam(name = "u") String url,
                                                  @RequestParam(name = "shortCode", required = false) String customShortCode,
                                                  @RequestParam(name = "expire", required = false) LocalDateTime expireAt

    ) {

        String shortenedUrl = urlService.shorten(url, customShortCode,expireAt);
        ResponseDto responseDto = ResponseDto.builder().shortUrl(shortenedUrl).longUrl(url).build();
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirectUrl(@PathVariable String shortCode) {

        System.out.println("ShortCode:" + shortCode);

        try {
            log.info("Find long url: short url = {}", shortCode);
            String longUrl = urlService.longUrl(shortCode);
            log.info("Long url found: short url = {}", shortCode);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(longUrl)).build();

        } catch (LongUrlNotFoundException | InvalidShortCodeException | ConstraintViolationException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_HTML).body(buildNotFoundHtml());
        }


    }

    private String buildNotFoundHtml() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>404 - Link Not Found | Shortly</title>
                     <link rel="icon" type="image/png" href="https://raw.githubusercontent.com/ankitkumar910/ankitkumar-resources/refs/heads/main/image.png" />
                    <style>
                        * {
                            box-sizing: border-box;
                        }
                
                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: grid;
                            place-items: center;
                            padding: 24px;
                            background: #050505;
                            color: #f4f4f5;
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                            text-align: center;
                        }
                
                        .container {
                            width: min(100%, 420px);
                        }
                
                        .brand {
                            margin-bottom: 40px;
                            color: #a1a1aa;
                            font-size: 14px;
                            font-weight: 500;
                            position: fixed;
                            bottom: -12px;
                           right: 20px;
                        }
                
                        .code {
                            margin-bottom: 12px;
                            color: #535bf2;
                            font-size: 80px;
                            font-weight: 700;
                            line-height: 1;
                        }
                
                        h1 {
                            margin: 0 0 12px;
                            font-size: 22px;
                            font-weight: 600;
                        }
                
                        p {
                            margin: 0 auto 28px;
                            color: #a1a1aa;
                            font-size: 14px;
                            line-height: 1.6;
                        }
                
                        a {
                            display: inline-block;
                            padding: 12px 18px;
                            border-radius: 2px;
                            background: #0713fc;
                            color: #fff;
                            font-size: 14px;
                            font-weight: 600;
                            text-decoration: none;
                        }
                
                        a:hover {
                            background: #535bf2;
                        }
                
                        a:focus-visible {
                            outline: 2px solid #a78bfa;
                            outline-offset: 3px;
                        }
                
                        @media (max-width: 380px) {
                            .code {
                                font-size: 68px;
                            }
                        }
                
                        .ubuntu-medium {
                            font-family: "Ubuntu", sans-serif;
                            font-weight: 500;
                            font-style: normal;
                        }
                        .kanit-bold {
                            font-family: "Kanit", sans-serif;
                            font-weight: 700;
                            font-style: normal;
                        }
                
                    </style>
                
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link href="https://fonts.googleapis.com/css2?family=Ubuntu:ital,wght@0,300;0,400;0,500;0,700;1,300;1,400;1,500;1,700&display=swap"
                          rel="stylesheet">
                    <link href="https://fonts.googleapis.com/css2?family=Kanit:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,700&display=swap" rel="stylesheet">
                </head>
                <body>
                <main class="container">
                
                    <div class="code kanit-bold" aria-hidden="true">404</div>
                    <h1>This short link doesn't exist</h1>
                    <p>The link you're looking for doesn't exist or may have expired. Double-check the URL, or head back and create a
                        new one.</p>
                    <a href="http://localhost:5173/" data-home-link>Go to Shortly</a>
                
                    <h1 id="page-title" class="ubuntu-medium brand">
                        Shortly
                    </h1>
                </main>
                
                
                </body>
                </html>
                """;
    }

}
