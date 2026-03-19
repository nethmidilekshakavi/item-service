package com.spms.item_service.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class GcsService {

    @Value("${gcs.bucket-name}")
    private String bucketName;

    @Value("${gcs.project-id}")
    private String projectId;

    /**
     * Uploads a file to GCS and returns the public URL.
     * The bucket must have "allUsers" -> "Storage Object Viewer" IAM binding (public access).
     */
    public String uploadFile(MultipartFile file) throws IOException {
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
                .getService();

        String objectName = "items/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        // Return the public URL
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
    }

    /**
     * Deletes a file from GCS given its public URL.
     */
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) return;

        // Extract object name from URL
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";
        if (!fileUrl.startsWith(prefix)) return;
        String objectName = fileUrl.substring(prefix.length());

        Storage storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
                .getService();

        storage.delete(BlobId.of(bucketName, objectName));
    }
}