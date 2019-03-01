package crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSA {
    javax.crypto.Cipher cipher = null;
    private static final String key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCKFctVrhfF3m2Kes0FBL/JFeOcmNg9eJz8k/hQy1kadD+XFUpluRqa//Uxp2s9W2qE0EoUCu59ugcf/p7lGuL99UoSGmQEynkBvZct+/M40L0E0rZ4BVgzLOJmIbXMp0J4PnPcb6VLZvxazGcmSfjauC7F3yWYqUbZd/HCBtawwIDAQAB";
    private java.security.PublicKey rsaPublicKey = null;
    
    public RSA() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException {
        this.cipher = javax.crypto.Cipher.getInstance("RSA");
        java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(javax.xml.bind.DatatypeConverter.parseBase64Binary(key));
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        this.rsaPublicKey = kf.generatePublic(keySpec);
        this.cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, rsaPublicKey);
    }
    
    public String encrypt(String data) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return javax.xml.bind.DatatypeConverter.printBase64Binary(this.cipher.doFinal(data.getBytes()));
    }
}
