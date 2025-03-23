package com.sokoby.service.impl;

import com.sokoby.entity.Customer;
import com.sokoby.entity.Store;
import com.sokoby.exception.CustomerException;
import com.sokoby.mapper.CustomerMapper;
import com.sokoby.payload.CustomerDto;
import com.sokoby.payload.JWTTokenDto;
import com.sokoby.payload.LoginDto;
import com.sokoby.repository.CustomerRepository;
import com.sokoby.repository.StoreRepository;
import com.sokoby.service.CustomerService;
import com.sokoby.service.JWTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final JWTService jwtService;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, StoreRepository storeRepository, JWTService jwtService) {
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public CustomerDto createCustomer(UUID storeId, CustomerDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new CustomerException("Customer name cannot be null or empty", "INVALID_NAME");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new CustomerException("Email cannot be null or empty", "INVALID_EMAIL");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new CustomerException("Password cannot be null or empty", "INVALID_PASSWORD");
        }
        if (customerRepository.existsByEmailAndStoreId(dto.getEmail(), storeId)) {
            throw new CustomerException("Email already exists in this store", "DUPLICATE_EMAIL");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomerException("Store not found", "STORE_NOT_FOUND"));

        Customer customer = CustomerMapper.toEntity(dto);
        customer.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt(10)));
        customer.setStore(store);

        try {
            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Created customer {} for store {}", dto.getEmail(), storeId);
            return CustomerMapper.toDto(savedCustomer);
        } catch (Exception e) {
            logger.error("Failed to create customer for store {}: {}", storeId, e.getMessage());
            throw new CustomerException("Failed to create customer", "CUSTOMER_CREATION_ERROR");
        }
    }

    @Override
    public CustomerDto getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerException("Customer not found", "CUSTOMER_NOT_FOUND"));
        logger.info("Retrieved customer with ID: {}", id);
        return CustomerMapper.toDto(customer);
    }

    @Override
    public List<CustomerDto> getCustomersByStoreId(UUID storeId) {
        List<Customer> customers = customerRepository.findByStoreId(storeId);
        if (customers.isEmpty()) {
            logger.warn("No customers found for store ID: {}", storeId);
        }
        return customers.stream().map(CustomerMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<CustomerDto> searchCustomers(String query, Pageable pageable) {
        logger.info("Searching customers with query: {}, page: {}, size: {}, sort: {}",
                query, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<Customer> customerPage = (query == null || query.trim().isEmpty())
                ? customerRepository.findAll(pageable)
                : customerRepository.searchCustomers(query, pageable);

        if (customerPage.isEmpty()) {
            logger.warn("No customers found for query: {}", query);
        }
        return mapToDtoPage(customerPage);
    }

    @Override
    public Page<CustomerDto> searchCustomersByStore(UUID storeId, String query, Pageable pageable) {
        logger.info("Searching customers in store {} with query: {}, page: {}, size: {}, sort: {}",
                storeId, query, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        if (!storeRepository.existsById(storeId)) {
            throw new CustomerException("Store not found", "STORE_NOT_FOUND");
        }

        Page<Customer> customerPage = (query == null || query.trim().isEmpty())
                ? customerRepository.findByStoreId(storeId, pageable)
                : customerRepository.searchCustomersByStore(storeId, query, pageable);

        if (customerPage.isEmpty()) {
            logger.warn("No customers found for store {} with query: {}", storeId, query);
        }
        return mapToDtoPage(customerPage);
    }

    private Page<CustomerDto> mapToDtoPage(Page<Customer> customerPage) {
        List<CustomerDto> dtos = customerPage.getContent()
                .stream()
                .map(CustomerMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, customerPage.getPageable(), customerPage.getTotalElements());
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(UUID id, CustomerDto dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerException("Customer not found", "CUSTOMER_NOT_FOUND"));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            customer.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty() && !dto.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmailAndStoreId(dto.getEmail(), customer.getStore().getId())) {
                throw new CustomerException("Email already exists in this store", "DUPLICATE_EMAIL");
            }
            customer.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            customer.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt(10)));
        }

        try {
            Customer updatedCustomer = customerRepository.save(customer);
            logger.info("Updated customer with ID: {}", id);
            return CustomerMapper.toDto(updatedCustomer);
        } catch (Exception e) {
            logger.error("Failed to update customer with ID: {}", id, e);
            throw new CustomerException("Failed to update customer", "CUSTOMER_UPDATE_ERROR");
        }
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerException("Customer not found", "CUSTOMER_NOT_FOUND");
        }
        try {
            customerRepository.deleteById(id);
            logger.info("Deleted customer with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete customer with ID: {}", id, e);
            throw new CustomerException("Failed to delete customer", "CUSTOMER_DELETION_ERROR");
        }
    }

    @Override
    public JWTTokenDto verifyUser(LoginDto loginDto) {
        try {
            Optional<Customer> opUser = customerRepository.findByEmail(loginDto.getEmail());
            if (opUser.isPresent()) {
                if (BCrypt. checkpw(loginDto.getPassword(), opUser.get().getPassword())) {
                    final Customer user = opUser.get();
                    String token = jwtService.generateToken(user.getEmail());
                    return new JWTTokenDto(token, "JWT Token");
                } else {
                    throw new CustomerException("Invalid password", "INVALID_PASSWORD");
                }
            } else {
                throw new CustomerException("User not found", "USER_NOT_FOUND");
            }
        } catch (Exception e) {
            logger.error("Failed to verify user with email: {}", loginDto.getEmail(), e);
            throw new CustomerException("Internal server error while verifying user", "SERVER_ERROR");
        }
    }
}