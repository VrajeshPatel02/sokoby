package com.sokoby.service;

import com.sokoby.payload.AddressDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    AddressDto createAddress(UUID customerId, AddressDto dto);

    AddressDto getAddressById(UUID id);

    List<AddressDto> getAddressesByCustomerId(UUID customerId);

    Page<AddressDto> getAddressesByCustomerId(UUID customerId, Pageable pageable);

    AddressDto updateAddress(UUID id, AddressDto dto);

    void deleteAddress(UUID id);
}