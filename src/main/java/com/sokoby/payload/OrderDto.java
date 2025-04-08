package com.sokoby.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private UUID id;

    @NotNull
    private UUID storeId;

    @NotNull
    private UUID customerId;

    @NotNull
    private AddressDto shippingAddress;

    @NotNull
    private UUID paymentId;

    @NotNull
    @Min(value = 0, message = "Total amount must be non-negative")
    private Double totalAmount;

    private DiscountDto discount;

    // New customer detail fields (optional)
    private String customerFirstName;
    private String customerLastName;
    private String customerPhoneNumber;
    private String customerEmail;

    @NotNull
    private String status;

    private Date createdAt;

    private List<OrderItemDto> orderItems;

    private String discountCode;

    private Double subtotal;

    private Double discountAmount;
}