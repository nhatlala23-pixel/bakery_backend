package com.nguyenthongnhat.backend_tttn.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
@Tag(name = "Upload", description = "Tải lên tệp tin")
@Slf4j
public class FileUploadController {

    @Value("${tttn.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Tải lên ảnh")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn một tệp tin!"));
            }

            // Get absolute path more safely
            Path root = Paths.get(System.getProperty("user.dir")).resolve(uploadDir).normalize();
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            // Generate unique file name and save
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = root.resolve(fileName);
            
            // Save file
            file.transferTo(filePath.toAbsolutePath().toFile());

            // Return the relative URL as JSON
            String fileUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "message", "Tải lên tệp tin thất bại! Vui lòng thử lại sau.",
                "errorType", e.getClass().getSimpleName()
            ));
        }
    }
}
