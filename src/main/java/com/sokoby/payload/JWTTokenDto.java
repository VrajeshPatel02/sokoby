package com.sokoby.payload;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class JWTTokenDto {
    private String type;
    private String token;

    public JWTTokenDto(String token, String type) {
        this.token = token;
        this.type = type;
    }
}
