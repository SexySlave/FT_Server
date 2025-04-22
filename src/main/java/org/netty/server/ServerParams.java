package org.netty.server;

import org.hibernate.SessionFactory;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class ServerParams {
    public static final int PORT = 9999;
    public static final String FILESTORAGEDIRECTORY = "C:\\FileStorage";
    public static SessionFactory SESSIONFACTORY;
    public static KeyPair KEYPAIR = generateRSAKeyPair();

    public static KeyPair generateRSAKeyPair() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(512); // Размер ключа: 2048 бит
        return keyPairGenerator.generateKeyPair();
    }
}
