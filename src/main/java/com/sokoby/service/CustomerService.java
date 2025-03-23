package com.sokoby.service;

import com.sokoby.payload.CustomerDto;
import com.sokoby.payload.JWTTokenDto;
import com.sokoby.payload.LoginDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    CustomerDto createCustomer(UUID storeId, CustomerDto dto);

    CustomerDto getCustomerById(UUID id);

    List<CustomerDto> getCustomersByStoreId(UUID storeId);

    Page<CustomerDto> searchCustomers(String query, Pageable pageable);

    Page<CustomerDto> searchCustomersByStore(UUID storeId, String query, Pageable pageable);

    CustomerDto updateCustomer(UUID id, CustomerDto dto);

    void deleteCustomer(UUID id);

    JWTTokenDto verifyUser(LoginDto loginDto);
}