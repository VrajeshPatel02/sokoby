package com.sokoby.mapper;

import com.sokoby.entity.Merchant;
import com.sokoby.payload.MerchantDto;

public class MerchantMapper {
    private MerchantMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static MerchantDto toDto(Merchant merchant) {
        if (merchant == null) {
            throw new IllegalArgumentException("Merchant entity cannot be null");
        }

        MerchantDto dto = new MerchantDto();
        dto.setId(merchant.getId());
        dto.setEmail(merchant.getEmail());
        dto.setPassword(merchant.getPassword()); // Note: In practice, password should not be returned
        dto.setFirstName(merchant.getFirstName());
        dto.setLastName(merchant.getLastName());
        dto.setCreatedAt(merchant.getCreatedAt());
        dto.setUpdatedAt(merchant.getUpdatedAt());
        return dto;
    }

    public static Merchant toEntity(MerchantDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("MerchantDto cannot be null");
        }

        Merchant merchant = new Merchant();
        merchant.setId(dto.getId());
        merchant.setEmail(dto.getEmail());
        merchant.setPassword(dto.getPassword()); // Should be hashed in service
        merchant.setFirstName(dto.getFirstName());
        merchant.setLastName(dto.getLastName());
        return merchant;
    }
}