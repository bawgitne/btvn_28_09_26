package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn file hình ảnh");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ cho phép tải lên file hình ảnh");
        }

        // Check if Cloudinary is configured with valid credentials
        if (cloudName != null && !cloudName.isBlank() && !cloudName.equals("your_cloudinary_name")) {
            try {
                Map<?, ?> result = cloudinary.uploader().upload(
                        file.getBytes(),
                        Map.of("folder", "shop/products")
                );
                String url = String.valueOf(result.get("secure_url"));
                String publicId = String.valueOf(result.get("public_id"));
                return new CloudinaryUploadResult(url, publicId);
            } catch (Exception e) {
                log.error("Cloudinary upload failed, falling back to local storage: {}", e.getMessage());
            }
        }

        // Local storage fallback so the application runs even without Cloudinary API keys
        try {
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;
            Path targetPath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            String url = "/uploads/" + fileName;
            return new CloudinaryUploadResult(url, "local_" + fileName);
        } catch (IOException e) {
            throw new IllegalStateException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        if (publicId.startsWith("local_")) {
            try {
                String fileName = publicId.substring("local_".length());
                Path filePath = Paths.get("uploads").resolve(fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Không thể xóa file local: {}", e.getMessage());
            }
            return;
        }

        if (cloudName != null && !cloudName.isBlank() && !cloudName.equals("your_cloudinary_name")) {
            try {
                cloudinary.uploader().destroy(publicId, Map.of("resource_type", "image"));
            } catch (Exception e) {
                log.error("Lỗi xóa ảnh Cloudinary: {}", e.getMessage());
            }
        }
    }
}
