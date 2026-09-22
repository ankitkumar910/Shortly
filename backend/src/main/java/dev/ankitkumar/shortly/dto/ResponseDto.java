package dev.ankitkumar.shortly.dto;

import lombok.*;

@Builder
@Getter
public class ResponseDto {
    private String shortUrl;
    private String longUrl;
}
