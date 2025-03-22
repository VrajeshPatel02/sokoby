package com.sokoby.mapper;

import com.sokoby.entity.Address;
import com.sokoby.payload.AddressDto;

public class AddressMapper {
    private AddressMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static AddressDto toDto(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address entity cannot be null");
        }

        AddressDto dto = new AddressDto();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        return dto;
    }

    public static Address toEntity(AddressDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("AddressDto cannot be null");
        }

        Address address = new Address();
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setPostalCode(dto.getPostalCode());
        return address;
    }
}