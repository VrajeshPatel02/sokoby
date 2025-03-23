package com.sokoby.service;

import com.sokoby.entity.Merchant;
import com.sokoby.payload.JWTTokenDto;
import com.sokoby.payload.LoginDto;
import com.sokoby.payload.MerchantDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MerchantService {
    MerchantDto createNewMerchant(MerchantDto dto);

    List<MerchantDto> getAllMerchants();

    MerchantDto getMerchantById(UUID id);

    MerchantDto updateMerchant(UUID id, MerchantDto dto);

    Page<MerchantDto> searchMerchants(String query, Pageable pageable);

    void deleteMerchant(UUID id);

    boolean existByEmail(MerchantDto dto);

    JWTTokenDto verifyUser(LoginDto loginDto);

    Merchant loadMerchantByEmail(String email);
}