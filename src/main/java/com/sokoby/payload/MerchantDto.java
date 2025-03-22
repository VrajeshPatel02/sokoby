package com.sokoby.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDto {
    private UUID id;

    @NotNull
    @Email(message = "Email should be valid")
    private String email;

    @NotNull
    @Size(min = 8, message = "Password should be at least 8 characters")
    private String password;

    @NotNull
    @Size(min = 1, message = "First name should not be empty")
    private String firstName;

    @NotNull
    @Size(min = 1, message = "Last name should not be empty")
    private String lastName;

    private Date createdAt;
    private Date updatedAt;
}