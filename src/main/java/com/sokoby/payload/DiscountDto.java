package com.sokoby.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class DiscountDto {
    private UUID id;
    private String code;
    private String discountType;
    private Double value;
    private Double minimumOrderAmount;
    private Date validFrom;
    private Date validUntil;
    private Boolean isActive;
    private Date createdAt;
}