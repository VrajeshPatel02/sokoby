package com.sokoby.payload;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class JWTTokenDto {
    private String type;
    private String token;
    private MerchantDto merchant;

    public JWTTokenDto(String token, String type) {
        this.token = token;
        this.type = type;
    }
}
