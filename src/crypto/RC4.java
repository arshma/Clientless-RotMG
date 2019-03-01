package crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.NoSuchPaddingException;

public class RC4 {
    private javax.crypto.Cipher rc4Ciper = null;
    private javax.crypto.spec.SecretKeySpec rc4Key = null;
    private int currentMode;
    public static final int ENCRYPT_MODE = javax.crypto.Cipher.ENCRYPT_MODE;
    public static final int DECRYPT_MODE = javax.crypto.Cipher.DECRYPT_MODE;
    
    public RC4(byte[] key, int mode) {
        try {
            this.rc4Ciper = javax.crypto.Cipher.getInstance("RC4");
            this.rc4Key = new javax.crypto.spec.SecretKeySpec(key, "RC4");
            this.currentMode = mode;
            this.rc4Ciper.init(mode, rc4Key);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    public RC4(String key, int mode) {
        this(key.getBytes(), mode);
    }
    
    public byte[] encrypt(byte[] rawData) {
        //this.rc4Ciper.init(javax.crypto.Cipher.ENCRYPT_MODE, rc4Key);
        return this.rc4Ciper.update(rawData);
    }
    public String encrypt(String rawData) {
        try {
            return new String(this.encrypt(rawData.getBytes("ASCII")), "ASCII");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(RC4.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }
    
    public byte[] decrypt(byte[] encryptedData) {
        //this.rc4Ciper.init(javax.crypto.Cipher.DECRYPT_MODE, rc4Key);
        return this.rc4Ciper.update(encryptedData);        
    }
    public String decrypt(String encryptedData) {
        try {
            return new String(this.decrypt(encryptedData.getBytes()), "ASCII");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(RC4.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void reset() {
        try {
            this.rc4Ciper.init(this.currentMode, rc4Key);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(RC4.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
