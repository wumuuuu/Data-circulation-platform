package com.example.demo.Service;

import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@Service
public class ECDHService {

    @Autowired
    private UserMapper userMapper;


    /**
     * 生成一个椭圆曲线 Diffie-Hellman (ECDH) 密钥对，用于密钥交换。
     * 采用 P-256 曲线，提供高安全性。
     *
     * @return 生成的 ECDH 密钥对
     * @throws NoSuchAlgorithmException 如果无法找到 "EC" 算法时抛出
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));  // 使用 P-256 椭圆曲线
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 根据私钥和对方的公钥生成共享密钥。
     * 共享密钥是通过 ECDH 算法计算得出的。
     *
     * @param privateKey 本地的私钥
     * @param publicKey  对方的公钥
     * @return 生成的共享密钥（字节数组形式）
     * @throws Exception 如果密钥初始化或密钥阶段出现问题时抛出
     */
    public byte[] generateSharedSecret(PrivateKey privateKey, PublicKey publicKey) throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(privateKey);  // 初始化 KeyAgreement，使用本地的私钥
        keyAgreement.doPhase(publicKey, true);  // 执行密钥交换，使用对方的公钥
        return keyAgreement.generateSecret();  // 生成共享密钥
    }

    /**
     * 使用共享密钥加密数据。
     * 采用 AES/GCM/NoPadding 算法，GCM 模式提供了加密和认证功能。
     *
     * @param data         要加密的原始数据
     * @return 加密后的数据，Base64 编码，包含加密后的数据和 IV
     * @throws Exception 如果加密过程出现问题时抛出
     */
    public String encrypt(byte[] data, String username) throws Exception {
        // 从数据库获取该用户的共享密钥
        User userSecretOptional = userMapper.findByUsername(username);
        if (userSecretOptional == null) {
            throw new IllegalArgumentException("User not found or shared secret is missing");
        }

        String sharedSecretString = userSecretOptional.getShared_secret();

        // 将共享密钥从 Base64 编码的字符串转换为 byte[]
        byte[] sharedSecret = Base64.getDecoder().decode(sharedSecretString);

        // 创建用于加密的 AES 密钥
        SecretKey secretKey = new SecretKeySpec(sharedSecret, "AES");

        // 初始化 Cipher，使用 AES/GCM/NoPadding 模式
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];  // GCM 推荐使用 12 字节的 IV（初始化向量）
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);  // 生成随机的 IV

        // 设置 GCM 参数，包含 IV 和认证标签长度（128 位）
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);  // 使用密钥和 GCM 参数初始化 Cipher

        // 加密数据
        byte[] encryptedData = cipher.doFinal(data);

        // 将 IV 和加密后的数据组合在一起
        byte[] encryptedDataWithIv = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, encryptedDataWithIv, 0, iv.length);
        System.arraycopy(encryptedData, 0, encryptedDataWithIv, iv.length, encryptedData.length);

        // 将组合后的数据编码为 Base64 字符串返回
        return Base64.getEncoder().encodeToString(encryptedDataWithIv);
    }


    // 将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 使用共享密钥解密数据。
     * 采用 AES/GCM/NoPadding 算法，GCM 模式提供了解密和认证功能。
     *
     * @param encryptedData 加密后的数据，Base64 编码，包含加密的数据和 IV
     * @param sharedSecret  用于解密的共享密钥
     * @return 解密后的原始数据
     * @throws Exception 如果解密过程出现问题时抛出
     */
    public String decrypt(String encryptedData, byte[] sharedSecret) throws Exception {
        try {

            // 解码 Base64 编码的数据
            byte[] encryptedDataWithIv = Base64.getDecoder().decode(encryptedData);

            // 从解码的数据中提取 IV（前 12 个字节）
            byte[] iv = new byte[12];
            System.arraycopy(encryptedDataWithIv, 0, iv, 0, iv.length);

            // 提取剩余的加密数据
            byte[] encryptedBytes = new byte[encryptedDataWithIv.length - iv.length];
            System.arraycopy(encryptedDataWithIv, iv.length, encryptedBytes, 0, encryptedBytes.length);

            // 创建用于解密的 AES 密钥，使用整个共享密钥
            SecretKey secretKey = new SecretKeySpec(sharedSecret, "AES");

            // 初始化 Cipher，使用 AES/GCM/NoPadding 模式
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // 解密数据
            byte[] decryptedData = cipher.doFinal(encryptedBytes);

            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to decode base64 encrypted data: " + e.getMessage());
            throw new Exception("Failed to decode base64 encrypted data", e);
        } catch (Exception e) {
            System.err.println("Decryption failed: " + e.getMessage());
            throw new Exception("Decryption failed", e);
        }
    }

    /**
     * 将 Base64 编码的公钥字符串解码为 PublicKey 对象。
     *
     * @param publicKeyStr Base64 编码的公钥字符串
     * @return 解码后的 PublicKey 对象
     * @throws Exception 如果解码或密钥生成过程中出现问题时抛出
     */
    public PublicKey decodePublicKey(String publicKeyStr) throws Exception {
        // 将 Base64 编码的公钥字符串解码为字节数组
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);

        // 使用 X509 编码规范生成公钥
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(keySpec);  // 生成 PublicKey 对象并返回
    }
}
