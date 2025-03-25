package com.sokoby.service;
import com.sokoby.payload.ImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImageService {
    ImageDto uploadImageFile(MultipartFile file, String bucketName, UUID productId);
    List<ImageDto> getImagesByProduct(UUID productId);
    boolean deleteImage(UUID imageId, String bucketName,UUID productId);
}
