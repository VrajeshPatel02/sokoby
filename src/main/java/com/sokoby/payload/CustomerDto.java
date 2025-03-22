package com.sokoby.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private UUID id;

    @NotNull
    @Size(min = 2, message = "Name should be at least 2 characters")
    private String name;

    @NotNull
    @Email(message = "Email should be valid")
    private String email;

    @NotNull
    @Size(min = 8, message = "Password should be at least 8 characters")
    private String password;


    private String phoneNumber;

    private List<OrderDto> orders;

    private UUID cartId;
    private Date createdAt;
    private Date updatedAt;
    private UUID storeId;
}