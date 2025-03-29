package com.sokoby.service;

import com.sokoby.payload.StoreDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StoreService {
    StoreDto createStore(UUID merchantId, StoreDto dto, MultipartFile logo);

    StoreDto getStoreById(UUID id);

    StoreDto getStoreByMerchantId(UUID merchantId);

    Page<StoreDto> searchStores(String query, Pageable pageable);

    StoreDto updateStore(UUID id, StoreDto dto);

    void deleteStore(UUID id);
}