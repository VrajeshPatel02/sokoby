package com.sokoby.payload;

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
public class AddressDto {
    @NotNull
    @Size(min = 1, message = "Street should not be empty")
    private String street;

    @NotNull
    @Size(min = 1, message = "City should not be empty")
    private String city;

    private String state;

    @NotNull
    @Size(min = 1, message = "Postal Code should not be empty")
    private String postalCode;

    @NotNull
    @Size(min = 1, message = "Country should not be empty")
    private String country;
}