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
    public static String encrypt(String plainText, String password) {
        try {
            // 去掉第一行
            String[] lines = plainText.split("\n", 2);
            String firstLine = lines.length > 0 ? lines[0] : ""; // 获取第一行
            String textToEncrypt = lines.length > 1 ? lines[1] : ""; // 获取剩余部分

            byte[] salt = "salt".getBytes(); // 一个默认值作为盐值
            SecretKeySpec secretKey = generateAesKey(password, salt, 16); // 16 字节（128 位）

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(textToEncrypt.getBytes());
            String encryptedText = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);

            // 将第一行与加密结果拼接
            return firstLine + "\n" + encryptedText;
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return plainText; // 留给初始提示信息
        }
    }

    public static String decrypt(String cipherText, String password) {
        try {
            // 分离第一行和加密部分
            String[] lines = cipherText.split("\n", 2);
            String firstLine = lines.length > 0 ? lines[0] : ""; // 获取第一行
            String encryptedText = lines.length > 1 ? lines[1] : ""; // 获取加密部分

            byte[] salt = "salt".getBytes(); // 一个默认值作为盐值
            SecretKeySpec secretKey = generateAesKey(password, salt, 16); // 16 字节（128 位）

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP);
            String decryptedText = new String(cipher.doFinal(decodedBytes));

            // 将解密后的文本与第一行拼接
            return firstLine + "\n" + decryptedText;
        } catch (Exception e) {
            isFailed = 1;
            e.printStackTrace(); // 打印异常信息
            return cipherText + "\npassword error!\nYour changes will not be saved."; // 返回失败
        }
    }
}
