//package com.sharkskin.store.service;
//
//import com.google.cloud.storage.BlobId;
//import com.google.cloud.storage.BlobInfo;
//import com.google.cloud.storage.HttpMethod;
//import com.google.cloud.storage.Storage;
//import com.google.cloud.storage.StorageOptions;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import jakarta.annotation.PostConstruct;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.Duration; // Import Duration
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class GcsImageUploadService {
//
//    @Value("${gcp.storage.bucket-name}")
//    private String bucketName;
//
//    @Value("${gcp.storage.service-account-key-path}")
//    private String serviceAccountKeyPath;
//
//    private Storage storage;
//
//    @PostConstruct
//    public void init() throws IOException {
//        // Load credentials from the service account key file
//        InputStream serviceAccountStream = getClass().getClassLoader().getResourceAsStream(serviceAccountKeyPath.replace("classpath:", ""));
//        if (serviceAccountStream == null) {
//            // Fallback for file system path if not found in classpath
//            serviceAccountStream = Files.newInputStream(Paths.get(serviceAccountKeyPath));
//        }
//
//        storage = StorageOptions.newBuilder()
//                .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccountStream))
//                .build()
//                .getService();
//    }
//
//    public String uploadFile(MultipartFile multipartFile) throws IOException {
//        String fileName = generateFileName(multipartFile);
//        BlobId blobId = BlobId.of(bucketName, fileName);
//        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
//                .setContentType(multipartFile.getContentType())
//                .build();
//
//        // Upload the file to GCS (objects are private by default)
//        storage.create(blobInfo, multipartFile.getBytes());
//
//        // Return the file name (object name) instead of the public URL
//        return fileName;
//    }
//
//    public String generateSignedUrl(String fileName, long expirationTimeMinutes) {
//        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
//        // Set signed URL options, e.g., expiration time
//        Storage.SignUrlOption options = Storage.SignUrlOption.withV4Signature();
//
//        URL url = storage.signUrl(blobInfo, expirationTimeMinutes, TimeUnit.MINUTES, options);
//        return url.toString();
//    }
//
//    private String generateFileName(MultipartFile multiPart) {
//        return UUID.randomUUID().toString() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
//    }
//}
package com.sharkskin.store.service;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class GcsImageUploadService {

    @Value("${gcs.enabled:false}")
    private boolean enabledProperty;

    @Value("${gcp.storage.bucket-name:}")
    private String bucketName;

    @Value("${gcp.storage.service-account-key-path:}")
    private String serviceAccountKeyPath;

    private Storage storage;
    private boolean enabled = false;

    @PostConstruct
    public void init() {
        if (!enabledProperty) {
            System.out.println("GCS service disabled by property.");
            enabled = false;
            return;
        }

        try {
            InputStream serviceAccountStream = getClass().getClassLoader()
                    .getResourceAsStream(serviceAccountKeyPath.replace("classpath:", ""));
            if (serviceAccountStream == null) {
                serviceAccountStream = Files.newInputStream(Paths.get(serviceAccountKeyPath));
            }
            storage = StorageOptions.newBuilder()
                    .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccountStream))
                    .build()
                    .getService();
            enabled = true;
            System.out.println("GCS service enabled.");
        } catch (IOException e) {
            System.out.println("GCS Service disabled due to error: " + e.getMessage());
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        if (!enabled) throw new IllegalStateException("GCS service is not enabled");
        String fileName = generateFileName(multipartFile);
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(multipartFile.getContentType())
                .build();
        storage.create(blobInfo, multipartFile.getBytes());
        return fileName;
    }

    public String generateSignedUrl(String fileName, long expirationTimeMinutes) {
        if (!enabled) throw new IllegalStateException("GCS service is not enabled");
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
        Storage.SignUrlOption options = Storage.SignUrlOption.withV4Signature();
        URL url = storage.signUrl(blobInfo, expirationTimeMinutes, TimeUnit.MINUTES, options);
        return url.toString();
    }

    private String generateFileName(MultipartFile multiPart) {
        return UUID.randomUUID().toString() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }
}