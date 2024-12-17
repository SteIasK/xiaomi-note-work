package net.micode.notes.model;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Code {
    //用这个来指示密码错误 对...对吗？
    public static int isFailed = 0;
    //加解密方法
    public static SecretKeySpec generateAesKey(String password, byte[] salt, int keyLength) {
        //处理秘钥，把任意输入转换为AES可用秘钥
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, keyLength * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String encrypt(String plainText, String password){
        // 使用 AES 或其他加密算法对 plainText 进行加密
        // 返回加密后的密文
        try {
            byte[] salt = "salt".getBytes(); // 一个默认值作为盐值
            //传入密码
            SecretKeySpec secretKey = generateAesKey(password, salt, 16); // 16 字节（128 位）

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return "failed to encrypt"; // 返回失败
        }
    }

    public static String decrypt(String cipherText, String password){
        // 使用 AES 或其他加密算法对 cipherText 进行解密
        // 返回解密后的明文
        try {
            isFailed = 0;
            byte[] salt = "salt".getBytes(); // 一个默认值作为盐值
            //传入密码
            SecretKeySpec secretKey = generateAesKey(password, salt, 16); // 16 字节（128 位）

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP);
            return new String(cipher.doFinal(decodedBytes));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            //设置为密码错误状态
            isFailed = 1;
            return cipherText + "\npassword error!\nYour changes will not be saved."; // 返回失败
        }
    }
}
