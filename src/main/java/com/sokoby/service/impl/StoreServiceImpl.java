package com.sokoby.service.impl;

import com.sokoby.entity.Merchant;
import com.sokoby.entity.Store;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.StoreMapper;
import com.sokoby.payload.StoreDto;
import com.sokoby.repository.MerchantRepository;
import com.sokoby.repository.StoreRepository;
import com.sokoby.service.ImageService;
import com.sokoby.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoreServiceImpl implements StoreService {
    private static final Logger logger = LoggerFactory.getLogger(StoreServiceImpl.class);

    private final StoreRepository storeRepository;
    private final MerchantRepository merchantRepository;
    private final ImageService imageService;
    @Autowired
    public StoreServiceImpl(StoreRepository storeRepository, MerchantRepository merchantRepository, ImageService imageService) {
        this.storeRepository = storeRepository;
        this.merchantRepository = merchantRepository;
        this.imageService = imageService;
    }

    @Override
    public StoreDto createStore(UUID merchantId, StoreDto dto, MultipartFile logo) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new MerchantException("Store name cannot be null or empty", "INVALID_STORE_NAME");
        }
        if (dto.getDomain() == null || dto.getDomain().trim().isEmpty()) {
            throw new MerchantException("Store domain cannot be null or empty", "INVALID_STORE_DOMAIN");
        }

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantException("Merchant not found", "MERCHANT_NOT_FOUND"));

        Store store = StoreMapper.toEntity(dto);
        store.setMerchant(merchant);


        try {
            Store savedStore = storeRepository.save(store);
            logger.info("Created store for merchant {} with name: {}", merchantId, dto.getName());
            String storeLogo = imageService.uploadStoreLogoFile(logo, "sokoby", savedStore.getId());
            savedStore.setImageUrl(storeLogo);
            Store saved = storeRepository.save(savedStore);
            return StoreMapper.toDto(saved);
        } catch (Exception e) {
            logger.error("Failed to create store for merchant {}: {}", merchantId, e.getMessage());
            throw new MerchantException("Failed to create store", "STORE_CREATION_ERROR");
        }
    }

    @Override
    public StoreDto getStoreById(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));
        logger.info("Retrieved store with ID: {}", id);
        return StoreMapper.toDto(store);
    }

    @Override
    public StoreDto getStoreByMerchantId(UUID merchantId) {
        Store store = storeRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new MerchantException("Store not found for merchant", "STORE_NOT_FOUND"));
        logger.info("Retrieved store for merchant ID: {}", merchantId);
        return StoreMapper.toDto(store);
    }

    @Override
    public Page<StoreDto> searchStores(String query, Pageable pageable) {

        Page<Store> storePage = storeRepository.searchStores(query, pageable);
        if (storePage.isEmpty()) {
            logger.warn("No stores found for query: {}", query);
        }
        return mapToDtoPage(storePage);
    }

    private Page<StoreDto> mapToDtoPage(Page<Store> storePage) {
        List<StoreDto> dtos = storePage.getContent()
                .stream()
                .map(StoreMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, storePage.getPageable(), storePage.getTotalElements());
    }

    @Override
    public StoreDto updateStore(UUID id, StoreDto dto) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            store.setName(dto.getName());
        }
        if (dto.getDomain() != null && !dto.getDomain().trim().isEmpty()) {
            store.setDomain(dto.getDomain());
        }
        if (dto.getDescription() != null) {
            store.setDescription(dto.getDescription());
        }
        if (dto.getStripeAccountId() != null && !dto.getStripeAccountId().trim().isEmpty()){
            store.setStripeAccountId(dto.getStripeAccountId());
        }
        try {
            Store updatedStore = storeRepository.save(store);
            logger.info("Updated store with ID: {}", id);
            return StoreMapper.toDto(updatedStore);
        } catch (Exception e) {
            logger.error("Failed to update store with ID: {}", id, e);
            throw new MerchantException("Failed to update store", "STORE_UPDATE_ERROR");
        }
    }

    @Override
    public void deleteStore(UUID id) {
        if (!storeRepository.existsById(id)) {
            throw new MerchantException("Store not found", "STORE_NOT_FOUND");
        }
        try {
            storeRepository.deleteById(id);
            logger.info("Deleted store with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete store with ID: {}", id, e);
            throw new MerchantException("Failed to delete store", "STORE_DELETION_ERROR");
        }
    }
}