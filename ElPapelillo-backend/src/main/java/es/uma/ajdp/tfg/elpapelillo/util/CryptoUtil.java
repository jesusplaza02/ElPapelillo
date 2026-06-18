package es.uma.ajdp.tfg.elpapelillo.util;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "ClAvEsEcReTa_TFG_Carnaval_2026__"; 

    public static String encrypt(String strToEncrypt) {
        if (strToEncrypt == null || strToEncrypt.trim().isEmpty()) return strToEncrypt;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.trim().getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar el campo: " + e.getMessage(), e);
        }
    }

    public static String decrypt(String strToDecrypt) {
        if (strToDecrypt == null || strToDecrypt.trim().isEmpty()) return strToDecrypt;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return strToDecrypt;
        }
    }
}