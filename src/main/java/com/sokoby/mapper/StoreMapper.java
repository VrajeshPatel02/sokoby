package com.sokoby.mapper;

import com.sokoby.entity.Store;
import com.sokoby.payload.StoreDto;

public class StoreMapper {
    // Private constructor to prevent instantiation
    private StoreMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static StoreDto toDto(Store store) {
        if (store == null) {
            throw new IllegalArgumentException("Store entity cannot be null");
        }

        StoreDto dto = new StoreDto();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setDomain(store.getDomain());
        dto.setDescription(store.getDescription());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setUpdatedAt(store.getUpdatedAt());
        if (store.getMerchant() != null) {
            dto.setMerchantId(store.getMerchant().getId());
        }
        return dto;
    }

    public static Store toEntity(StoreDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("StoreDto cannot be null");
        }

        Store store = new Store();
        store.setId(dto.getId());
        store.setName(dto.getName());
        store.setDomain(dto.getDomain());
        store.setDescription(dto.getDescription());
        // Merchant association handled in service layer (requires fetching Merchant)
        return store;
    }
}