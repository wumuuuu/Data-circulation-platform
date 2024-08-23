package com.example.demo2.controller;

import com.example.demo2.Model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("/DH")
public class DHController {

    private KeyPair serverKeyPair;
    private byte[] sharedSecret;

    public DHController() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // 使用 ECDH 算法生成服务器密钥对
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC");
        keyPairGen.initialize(new ECGenParameterSpec("secp256r1")); // 使用 P-256 曲线
        serverKeyPair = keyPairGen.generateKeyPair();
    }

    @PostMapping("/sendPublicKey")
    public ResponseEntity<ApiResponse> receiveClientPublicKey(@RequestBody PublicKeyRequest publicKeyRequest) {
        // 打印接收到的请求体
        System.out.println("Received request: " + publicKeyRequest);

        // 检查并打印 publicKey 值
        if (publicKeyRequest.getPublicKey() == null || publicKeyRequest.getPublicKey().isEmpty()) {
            System.err.println("Received Public Key is null or empty!");
            return ResponseEntity.badRequest().body(new ApiResponse(400, "Received public key is null or empty", null));
        }

        try {
            // 接收并解码客户端公钥
            byte[] clientPublicKeyBytes = Base64.getDecoder().decode(publicKeyRequest.getPublicKey());
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey clientPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(clientPublicKeyBytes));

            // 生成共享密钥
            KeyAgreement keyAgree = KeyAgreement.getInstance("ECDH");
            keyAgree.init(serverKeyPair.getPrivate());
            keyAgree.doPhase(clientPublicKey, true);
            sharedSecret = keyAgree.generateSecret();

            System.out.println("Shared secret generated successfully.");

            // 返回成功的响应
            return ResponseEntity.ok(new ApiResponse(200, "Public key received and shared secret generated", null));

        } catch (GeneralSecurityException e) {
            // 打印更详细的错误信息
            System.err.println("Error during key agreement: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse(500, "Key agreement failed", null));
        }
    }


    @GetMapping("/getServerPublicKey")
    public PublicKeyResponse sendServerPublicKey() {
        // 导出并编码服务器公钥
        byte[] serverPublicKeyBytes = serverKeyPair.getPublic().getEncoded();
        String serverPublicKeyBase64 = Base64.getEncoder().encodeToString(serverPublicKeyBytes);

        return new PublicKeyResponse(serverPublicKeyBase64);
    }

    // 用于接收和发送 JSON 数据的类
    static class PublicKeyRequest {
        private String publicKey;

        // 无参构造函数
        public PublicKeyRequest() {
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }
    }

    static class PublicKeyResponse {
        private String publicKey;

        public PublicKeyResponse(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPublicKey() {
            return publicKey;
        }
    }
}
