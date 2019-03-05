package crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class RC4 {
    private javax.crypto.Cipher rc4Ciper = null;
    private javax.crypto.spec.SecretKeySpec rc4Key = null;
    //public static final int ENCRYPT_MODE = javax.crypto.Cipher.ENCRYPT_MODE;
    //public static final int DECRYPT_MODE = javax.crypto.Cipher.DECRYPT_MODE;
    
    public RC4(byte[] key) throws Exception {
        this.rc4Ciper = javax.crypto.Cipher.getInstance("RC4");
        this.rc4Key = new javax.crypto.spec.SecretKeySpec(key, "RC4");
        this.rc4Ciper.init(Cipher.ENCRYPT_MODE, rc4Key);
    }
    public RC4(String key) throws Exception {
        this(javax.xml.bind.DatatypeConverter.parseHexBinary(key));
    }
    
    public byte[] cipher(byte[] rawData) {
        return this.rc4Ciper.update(rawData);
    }  
    
    public void reset() throws Exception {
        this.rc4Ciper.init(Cipher.ENCRYPT_MODE, rc4Key);
    }
}
