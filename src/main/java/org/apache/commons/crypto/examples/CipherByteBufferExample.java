package org.apache.commons.crypto.examples;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.crypto.cipher.CipherTransformation;
import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;

public class CipherByteBufferExample {

    private static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    private static String asString(ByteBuffer buffer) {
        final ByteBuffer copy = buffer.duplicate();
        final byte[] bytes = new byte[Math.min(copy.remaining(),50)];
        copy.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        final SecretKeySpec key = new SecretKeySpec(getUTF8Bytes("1234567890123456"), "AES");
        final IvParameterSpec iv = new IvParameterSpec(getUTF8Bytes("1234567890123456"));
        Properties properties = new Properties();
        //Creates a CryptoCipher instance with the transformation and properties.
        final CipherTransformation transform = CipherTransformation.AES_CBC_PKCS5PADDING;
        CryptoCipher encipher = Utils.getCipherInstance(transform, properties);

        final int bufferSize = 1024;

        ByteBuffer inBuffer = ByteBuffer.allocateDirect(bufferSize);
        ByteBuffer outBuffer = ByteBuffer.allocateDirect(bufferSize);
        inBuffer.put(getUTF8Bytes("hello world!"));

        inBuffer.flip(); // ready for the cipher to read it
        // Show the data is there
        System.out.println("inBuffer="+asString(inBuffer));

        // Initializes the cipher with ENCRYPT_MODE,key and iv.
        encipher.init(Cipher.ENCRYPT_MODE, key, iv);
        // Continues a multiple-part encryption/decryption operation for byte buffer.
        final int updateBytes = encipher.update(inBuffer, outBuffer);
        System.out.println(updateBytes);

        // We should call do final at the end of encryption/decryption.
        final int finalBytes = encipher.doFinal(inBuffer, outBuffer);
        System.out.println(finalBytes);
        encipher.close();

        outBuffer.flip(); // ready for use as decrypt
        byte [] encoded = new byte[updateBytes + finalBytes];
        outBuffer.duplicate().get(encoded);
        System.out.println(Arrays.toString(encoded));

        // Now reverse the process
        CryptoCipher decipher = Utils.getCipherInstance(transform, properties);
        decipher.init(Cipher.DECRYPT_MODE, key, iv);
        ByteBuffer decoded = ByteBuffer.allocateDirect(bufferSize);
        decipher.update(outBuffer, decoded);
        decipher.doFinal(outBuffer, decoded);
        decipher.close();
        decoded.flip(); // ready for use
        System.out.println("decoded="+asString(decoded));
    }

}