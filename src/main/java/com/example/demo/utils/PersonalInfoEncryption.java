package com.example.demo.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.SecureRandom;

public class PersonalInfoEncryption {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "12345678901234567890123456789012"; // 정확히 32바이트
    
    /**
     * 개인정보 암호화
     */
    public static String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // IV 생성 (16바이트)
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            
            // IV + 암호화된 데이터를 Base64로 인코딩
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            System.err.println("암호화 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 개인정보 복호화
     */
    public static String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            
            // IV 추출 (처음 16바이트)
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);
            
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            System.err.println("복호화 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 