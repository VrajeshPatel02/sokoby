package com.sokoby.mapper;

import com.sokoby.entity.Customer;
import com.sokoby.payload.CustomerDto;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.stream.Collectors;

public class CustomerMapper {
    private CustomerMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static CustomerDto toDto(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer entity cannot be null");
        }

        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setOrders(customer.getOrders().stream().map(OrderMapper::toDto).collect(Collectors.toList()));
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }

    public static Customer toEntity(CustomerDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CustomerDto cannot be null");
        }

        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt(10))); // Will be hashed in service
        return customer;
    }
}
