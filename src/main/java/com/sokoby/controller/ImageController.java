package com.sokoby.controller;

import com.sokoby.payload.ImageDto;
import com.sokoby.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
@RestController
@RequestMapping("/api/images")
public class ImageController {
    @Autowired
    private ImageService imageService;
    @PostMapping("/upload/file/{bucketName}/product/{productId}")
    public ResponseEntity<ImageDto> uploadFile(@RequestParam MultipartFile file,
                                               @PathVariable String bucketName,
                                               @PathVariable UUID productId){
        ImageDto imageDto = imageService.uploadImageFile(file, bucketName, productId);
        return new ResponseEntity<>(imageDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete/file/{bucketName}/product/{productId}")
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
}
