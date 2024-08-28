//package com.example.demo.Controller;
//
//import com.example.demo.Mapper.FileMapper;
//import com.example.demo.Model.FileEntity;
//import com.example.demo.Service.ECDHService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.ByteArrayOutputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Base64;
//import java.util.Optional;
//import java.util.zip.GZIPOutputStream;
//
//@RestController
//@RequestMapping("/files")
//public class FileDownloadController {
//
//    private static final String ENCRYPTION_KEY = "your-secret-key"; // 你的加密密钥
//
//    @Autowired
//    private FileMapper fileMapper;
//
//    @Autowired
//    private ECDHService ecdhService;
//
//    @PostMapping("/download")
//    public ResponseEntity<ByteArrayResource> downloadFile(@RequestBody FileDownloadRequest request) throws Exception {
//        Long id = request.getFileId();
//        String username = request.getUsername();
//
//        // 从数据库中查询文件路径
//        Optional<FileEntity> fileEntityOptional = fileMapper.findById(id);
//        if (fileEntityOptional.isPresent()) {
//            FileEntity fileEntity = fileEntityOptional.get();
//            Path filePath = Paths.get(fileEntity.getFilePath());
//
//            // 读取文件内容
//            byte[] fileData = Files.readAllBytes(filePath);
//
//            // Step 1: 压缩文件
//            byte[] compressedData = compressData(fileData);
//
//            // Step 2: 加密文件（传入用户名）
//            String encryptedDataBase64 = ecdhService.encrypt(compressedData, username);
//
//            // 将 Base64 编码的加密数据转换回 byte[]
//            byte[] encryptedData = Base64.getDecoder().decode(encryptedDataBase64);
//
//            // 创建响应头部信息
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + ".gz.enc\"");
//            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
//
//            ByteArrayResource resource = new ByteArrayResource(encryptedData);
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .contentLength(encryptedData.length)
//                    .body(resource);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//
//    // 压缩数据
//    private byte[] compressData(byte[] data) throws Exception {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
//            gzipOutputStream.write(data);
//        }
//        return byteArrayOutputStream.toByteArray();
//    }
//}
//
