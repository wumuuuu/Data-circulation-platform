package com.example.demo.Controller;

import com.example.demo.Model.APIResponse;
import com.example.demo.Service.ECDHService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class FileController {

    private static final String DIRECTORY_PATH = "C:\\Users\\zzy\\Desktop\\1";
    // 上传块计数器，记录成功上传的块数
    private final AtomicInteger uploadedChunksCounter = new AtomicInteger(0);


    @Autowired
    private ECDHService ecdhService;

    @GetMapping("/start-upload")
    public APIResponse<String> startUpload(HttpSession session) {
        // 生成唯一的文件标识符
        String fileId = UUID.randomUUID().toString();
        // 返回给前端，前端每次上传块时都携带该 fileId
        return APIResponse.success(fileId);
    }

    @RequestMapping("/upload-chunk")
    public APIResponse<String> uploadChunk(
            @RequestParam("chunk") MultipartFile chunk,  // 直接接收 Base64 字符串
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("fileId") String fileId,
            HttpSession session) {
        try {
            System.out.println("File ID: " + fileId);

            // **从会话中获取共享密钥**
            byte[] sharedSecret = (byte[]) session.getAttribute("sharedSecret");

            // **将共享密钥转换为十六进制字符串输出**
            String sharedSecretHex = bytesToHex(sharedSecret);
            System.out.println("Shared Secret (Hex): " + sharedSecretHex);

            if (sharedSecret == null) {
                return APIResponse.error(500, "共享密钥不存在于会话中");
            }

            // **获取上传的二进制数据**
            byte[] encryptedChunkBytes = chunk.getBytes();

            // 打印二进制数据长度，检查是否为二进制数据
            System.out.println("Received chunk size (bytes): " + encryptedChunkBytes.length);

            // 打印前几个字节，看看是不是符合预期的二进制数据
            System.out.println("First 10 bytes of the chunk: ");
            for (int i = 0; i < Math.min(encryptedChunkBytes.length, 10); i++) {
                System.out.print(String.format("0x%02X ", encryptedChunkBytes[i]));
            }
            System.out.println(); // 换行

            // **解密数据**
            byte[] decryptedDataBytes = ecdhService.decryptFile(encryptedChunkBytes, sharedSecret);

            // **保存解密后的块**
            saveChunk(decryptedDataBytes, chunkIndex, fileId);

            // 更新计数器，每次上传成功一个块时计数器加1
            int uploadedChunks = uploadedChunksCounter.incrementAndGet();

            // 检查是否所有块都上传完毕
            if (uploadedChunks == totalChunks) {
                // 所有块都上传完毕，执行合并
                if (areAllChunksPresent(totalChunks, fileId)) {
                    mergeChunks(totalChunks, fileId);
                    return APIResponse.success("所有块都上传并合并成功");
                } else {
                    return APIResponse.error(500, "数据块缺失");
                }
            }

            // 如果不是最后一个块，返回成功消息
            return APIResponse.success("Chunk " + (chunkIndex + 1) + " uploaded and decrypted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误响应
            return APIResponse.error(500, "Error uploading chunk " + (chunkIndex + 1) + ": " + e.getMessage());
        }
    }


    // **将字节数组转换为十六进制字符串的方法**
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0'); // 确保每个字节都用两位表示
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // 保存单个文件块到以 fileId 命名的指定目录
    private void saveChunk(byte[] data, int chunkIndex, String fileId) throws IOException {
        // 指定保存目录为 C:\Users\zzy\Desktop\1\fileId
        Path chunkDir = Paths.get(DIRECTORY_PATH, fileId);

        // 创建保存目录（如果不存在）
        if (!Files.exists(chunkDir)) {
            Files.createDirectories(chunkDir);
        }

        // 保存块到以 fileId 命名的子目录
        Path chunkPath = chunkDir.resolve("chunk_" + chunkIndex);  // 保存为 chunk_0, chunk_1, etc.
        Files.write(chunkPath, data);
    }

    // 检查所有块是否都存在
    private boolean areAllChunksPresent(int totalChunks, String fileId) {
        Path chunkDir = Paths.get(DIRECTORY_PATH, fileId);
        for (int i = 0; i < totalChunks; i++) {
            Path chunkPath = chunkDir.resolve("chunk_" + i);
            if (!Files.exists(chunkPath)) {
                System.err.println("Missing chunk: " + chunkPath.toString());
                return false;  // 如果某个块不存在，返回 false
            }
        }
        return true;  // 如果所有块都存在，返回 true
    }

    // 合并所有块
    private void mergeChunks(int totalChunks, String fileId) throws IOException {
        // 创建合并后的输出文件，保存在 C:\Users\zzy\Desktop\1\fileId_complete.dat
        Path chunkDir = Paths.get(DIRECTORY_PATH, fileId); // 确保使用 fileId 作为文件夹
        Path outputFile = chunkDir.resolve("complete.csv"); // 合并后的文件名

        try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
            // 遍历所有块，按顺序合并它们
            for (int i = 0; i < totalChunks; i++) {
                Path chunkPath = chunkDir.resolve("chunk_" + i);
                Files.copy(chunkPath, outputStream);
            }
        }

        // 合并完成后可以选择删除块文件
        deleteChunks(totalChunks, fileId);
        System.out.println("Chunks merged successfully into: " + outputFile.toString());

        // 合并完成后，重置计数器
        uploadedChunksCounter.set(0);
    }

    // 删除所有块文件
    private void deleteChunks(int totalChunks, String fileId) throws IOException {
        Path chunkDir = Paths.get(DIRECTORY_PATH, fileId);
        for (int i = 0; i < totalChunks; i++) {
            Path chunkPath = chunkDir.resolve("chunk_" + i);
            Files.deleteIfExists(chunkPath);  // 删除块文件
        }
    }
}
