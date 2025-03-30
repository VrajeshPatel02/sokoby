package com.sokoby.controller;

import com.sokoby.payload.ImageDto;
import com.sokoby.payload.StoreDto;
import com.sokoby.service.ImageService;
import com.sokoby.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
@RestController
@RequestMapping(consumes = {"multipart/form-data", "application/octet-stream"})
public class ImageController {
    @Autowired
    private ImageService imageService;

    @Autowired
    private StoreService storeService;
    @PostMapping("api/image/upload/file/{bucketName}/product/{productId}")
    public ResponseEntity<ImageDto> uploadFile(@RequestParam MultipartFile file,
                                               @PathVariable String bucketName,
                                               @PathVariable UUID productId){
        ImageDto imageDto = imageService.uploadImageFile(file, bucketName, productId);
        return new ResponseEntity<>(imageDto, HttpStatus.OK);
    }

    @DeleteMapping("api/image/delete/file/{bucketName}/product/{productId}")
    public ResponseEntity<?> deleteImage(@RequestParam UUID imageId,
                                         @PathVariable String bucketName,
                                         @PathVariable UUID productId) {
        boolean isDeleted = imageService.deleteImage(imageId, bucketName, productId);
        if (isDeleted) {
            return new ResponseEntity<>("Image deleted successfully.",HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to delete image.",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/store/create/{merchantId}")
    public ResponseEntity<StoreDto> createStore(
            @PathVariable UUID merchantId,
            @ModelAttribute StoreDto dto,
            @RequestParam("logo") MultipartFile logo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(merchantId, dto, logo));
    }
}
