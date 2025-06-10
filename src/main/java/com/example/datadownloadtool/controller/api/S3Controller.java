//package com.example.datadownloadtool.controller.api;
//
//
//import com.example.datadownloadtool.service.S3Service;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/s3")
//public class S3Controller {
//    private final S3Service s3Service;
//
//    @PostMapping("/download")
//    public ResponseEntity<String> downloadFile(@RequestParam String key, @RequestParam String targetPath) {
//        try{
//            // kiem tra va tao thu muc
//            Path targetDir = Paths.get(targetPath).toAbsolutePath();
//            if(Files.exists(targetDir)){
//                Files.createDirectories(targetDir);
//            }
//            // tao duong dan voi ten file
//            Path savePath = targetDir.resolve(Paths.get(key).getFileName().toString());
//
//            s3Service.downloadFile(key, savePath.toString());
//            return ResponseEntity.ok("Downloaded successfully");
//        } catch (IOException e) {
//            return ResponseEntity.status(500).body(e.getMessage());
//        }
//    }
//}
