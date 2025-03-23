package com.sokoby.service;

import com.sokoby.payload.StoreDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoreService {
    StoreDto createStore(UUID merchantId, StoreDto dto);

    StoreDto getStoreById(UUID id);

    StoreDto getStoreByMerchantId(UUID merchantId);

    Page<StoreDto> searchStores(String query, Pageable pageable);

    StoreDto updateStore(UUID id, StoreDto dto);

    void deleteStore(UUID id);
}