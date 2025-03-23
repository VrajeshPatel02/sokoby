package com.sokoby.service.impl;

import com.sokoby.entity.Merchant;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.MerchantMapper;
import com.sokoby.payload.JWTTokenDto;
import com.sokoby.payload.LoginDto;
import com.sokoby.payload.MerchantDto;
import com.sokoby.repository.MerchantRepository;
import com.sokoby.service.JWTService;
import com.sokoby.service.MerchantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MerchantServiceImpl implements MerchantService {

    private static final Logger logger = LoggerFactory.getLogger(MerchantServiceImpl.class);

    private final MerchantRepository merchantRepo;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MerchantServiceImpl(
        MerchantRepository merchantRepo, 
        JWTService jwtService,
        PasswordEncoder passwordEncoder
    ) {
        this.merchantRepo = merchantRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public MerchantDto createNewMerchant(MerchantDto dto) {

        if (merchantRepo.existsByEmail(dto.getEmail())) {
            throw new MerchantException("Email already exists", "DUPLICATE_EMAIL");
        }

        Merchant merchant = MerchantMapper.toEntity(dto);
        merchant.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt())); // Hash password

        try {
            Merchant savedMerchant = merchantRepo.save(merchant);
            logger.info("Created merchant with email: {}", savedMerchant.getEmail());
            MerchantDto responseDto = MerchantMapper.toDto(savedMerchant);
            responseDto.setPassword(null); // Do not return password
            return responseDto;
        } catch (Exception e) {
            logger.error("Failed to create merchant with email: {}", dto.getEmail(), e);
            throw new MerchantException("Failed to create merchant", "MERCHANT_CREATION_ERROR");
        }
    }

    @Override
    public List<MerchantDto> getAllMerchants() {
        try {
            List<Merchant> merchants = merchantRepo.findAll();
            if (merchants.isEmpty()) {
                logger.warn("No merchants found");
                throw new MerchantException("No merchants found", "NO_MERCHANTS_FOUND");
            }
            return merchants.stream()
                    .map(merchant -> {
                        MerchantDto dto = MerchantMapper.toDto(merchant);
                        dto.setPassword(null); // Do not return password
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to retrieve all merchants", e);
            throw new MerchantException("Failed to retrieve merchants", "SERVER_ERROR");
        }
    }

    @Override
    public MerchantDto getMerchantById(UUID id) {
        try {
            Merchant merchant = merchantRepo.findById(id)
                    .orElseThrow(() -> new MerchantException("Merchant not found", "MERCHANT_NOT_FOUND"));
            logger.info("Retrieved merchant with ID: {}", id);
            MerchantDto dto = MerchantMapper.toDto(merchant);
            dto.setPassword(null); // Do not return password
            return dto;
        } catch (Exception e) {
            logger.error("Failed to retrieve merchant with ID: {}", id, e);
            throw new MerchantException("Failed to retrieve merchant", "SERVER_ERROR");
        }
    }

    @Override
    @Transactional
    public MerchantDto updateMerchant(UUID id, MerchantDto dto) {
        Merchant merchant = merchantRepo.findById(id)
                .orElseThrow(() -> new MerchantException("Merchant not found", "MERCHANT_NOT_FOUND"));
        if (!dto.getEmail().equals(merchant.getEmail()) && merchantRepo.existsByEmail(dto.getEmail())) {
            throw new MerchantException("Email already exists", "DUPLICATE_EMAIL");
        }

        merchant.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            merchant.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt())); // Update password if provided
        }
        merchant.setFirstName(dto.getFirstName());
        merchant.setLastName(dto.getLastName());
        merchant.setUpdatedAt();
        try {
            Merchant updatedMerchant = merchantRepo.save(merchant);
            logger.info("Updated merchant with ID: {}", id);
            MerchantDto responseDto = MerchantMapper.toDto(updatedMerchant);
            responseDto.setPassword(null); // Do not return password
            return responseDto;
        } catch (Exception e) {
            logger.error("Failed to update merchant with ID: {}", id, e);
            throw new MerchantException("Failed to update merchant", "MERCHANT_UPDATE_ERROR");
        }
    }

    @Override
    public Page<MerchantDto> searchMerchants(String query, Pageable pageable) {
        try {
            Page<Merchant> merchantPage;
            
            if (query == null || query.trim().isEmpty()) {
                merchantPage = merchantRepo.findAll(pageable);
            } else {
                merchantPage = merchantRepo.searchMerchants(query, pageable);
            }

            return merchantPage.map(merchant -> {
                MerchantDto dto = MerchantMapper.toDto(merchant);
                return dto;
            });
        } catch (Exception e) {
            logger.error("Error searching merchants", e);
            throw new MerchantException("Failed to search merchants", "SEARCH_ERROR");
        }
    }

    @Override
    @Transactional
    public void deleteMerchant(UUID id) {
        if (!merchantRepo.existsById(id)) {
            throw new MerchantException("Merchant not found", "MERCHANT_NOT_FOUND");
        }
        try {
            merchantRepo.deleteById(id);
            logger.info("Deleted merchant with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete merchant with ID: {}", id, e);
            throw new MerchantException("Failed to delete merchant", "MERCHANT_DELETION_ERROR");
        }
    }

    @Override
    public boolean existByEmail(MerchantDto dto) {
        try {
            return merchantRepo.existsByEmail(dto.getEmail());
        } catch (Exception e) {
            logger.error("Failed to check existence of merchant by email: {}", dto.getEmail(), e);
            throw new MerchantException("Failed to check merchant existence", "SERVER_ERROR");
        }
    }

    @Override
    public JWTTokenDto verifyUser(LoginDto loginDto) {
        try {
            Merchant merchant = merchantRepo.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new MerchantException("User not found", "USER_NOT_FOUND"));

            if (!BCrypt.checkpw(loginDto.getPassword(), merchant.getPassword())) {
                throw new MerchantException("Invalid password", "INVALID_PASSWORD");
            }

            String token = jwtService.generateToken(merchant.getEmail()); // Using email as token subject
            logger.info("Generated JWT token for merchant: {}", merchant.getEmail());
            return new JWTTokenDto(token, "JWT Token");
        } catch (Exception e) {
            logger.error("Failed to verify user with email: {}", loginDto.getEmail(), e);
            throw new MerchantException("Failed to verify user", "SERVER_ERROR");
        }
    }

    @Override
    public Merchant loadMerchantByEmail(String email) throws UsernameNotFoundException {
        try {
            return merchantRepo.findByEmail(email)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Failed to load merchant by email: {}", email, e);
            throw new MerchantException("Failed to load merchant", "SERVER_ERROR");
        }
    }

    private void validateMerchantInput(MerchantDto dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new MerchantException("Email cannot be null or empty", "INVALID_EMAIL");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new MerchantException("Password must be at least 8 characters", "INVALID_PASSWORD");
        }
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            throw new MerchantException("First name cannot be null or empty", "INVALID_FIRST_NAME");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new MerchantException("Last name cannot be null or empty", "INVALID_LAST_NAME");
        }
    }
}