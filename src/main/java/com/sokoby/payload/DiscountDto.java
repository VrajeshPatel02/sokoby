package com.sokoby.payload;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
public class DiscountDto {
    private UUID id;
    private UUID storeId;
    private String code;
    private String type; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    private BigDecimal value;
    private BigDecimal minimumPurchaseAmount;
    private Date startDate;
    private Date endDate;
    private Integer usageLimit;
    private String status; // ACTIVE, EXPIRED, DRAFT
} 