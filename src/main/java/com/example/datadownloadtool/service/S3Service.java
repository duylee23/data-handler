//package com.example.datadownloadtool.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.core.ResponseInputStream;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.GetObjectRequest;
//import software.amazon.awssdk.services.s3.model.GetObjectResponse;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@Service
//@RequiredArgsConstructor
//public class S3Service {
//    private final S3Client s3Client;
//
//    @Value("${aws.bucketName}")
//    private String bucketName;
//
//    public void downloadFile(String key, String localPath) throws IOException {
//        Path path = Paths.get(localPath);
//        Files.createDirectories(path.getParent());
//        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
//            OutputStream outputStream = Files.newOutputStream(Paths.get(localPath))) {
//            s3Object.transferTo(outputStream);
//        }
//    }
//}
