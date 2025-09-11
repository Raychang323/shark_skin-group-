package com.sharkskin.store.action;

import com.sharkskin.store.service.GcsImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/images")
public class ImageController {

    private final GcsImageUploadService gcsImageUploadService;

    @Autowired
    public ImageController(GcsImageUploadService gcsImageUploadService) {
        this.gcsImageUploadService = gcsImageUploadService;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Void> getSignedUrl(@PathVariable String fileName) {
        try {
            // Generate a signed URL that expires in 15 minutes
            String signedUrl = gcsImageUploadService.generateSignedUrl(fileName, 15);
            return ResponseEntity.status(HttpStatus.FOUND).location(java.net.URI.create(signedUrl)).build();
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}