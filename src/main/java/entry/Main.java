package entry;

import ms.netty.client.Http3ClientExample;
import ms.netty.server.APIProvider;
import ms.netty_old.server.Authorization;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
//

        Callable<Boolean> callable = new Callable<>() {
            @Override
            public Boolean call() throws Exception {
                TimeUnit.MILLISECONDS.sleep(1000*60*12);
                return true;
            }
        };


        while (callable.call()) {
            System.out.println("smth");
        }







        // Добавить отправку мак-адресса, добавить его в таблицу с жвт, настроить авторизацию без обращения к юзеру



//        for (int i = 0; i != 500; i ++){
//            TimeUnit.MILLISECONDS.sleep(10);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Http3ClientExample.main();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }).start();
//        }





















    }

    public static KeyPair generateRSAKeyPair()   {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(512); // Размер ключа: 2048 бит
        return keyPairGenerator.generateKeyPair();
    }

//        KeyPair keyPair = generateRSAKeyPair();
//        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//
//        Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey );
//        JWTVerifier verifier = JWT.require(algorithm)
//                .withIssuer("Baeldung")
//                .build();
//        String jwtToken = JWT.create()
//                .withIssuer("Baeldung")
//                .withSubject("Baeldung Details")
//                .withClaim("userId", "15647839049гоалвд234")
//                .withIssuedAt(new Date())
//                .withExpiresAt(new Date(System.currentTimeMillis() + 5000L))
//                .withJWTId(UUID.randomUUID()
//                        .toString())
////                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
//                .sign(algorithm);
//        System.out.println(jwtToken);
//        try {
//            DecodedJWT decodedJWT = verifier.verify(jwtToken);
//        } catch (JWTVerificationException e) {
//            System.out.println(e.getMessage());
//        }
//
//        Claim claim = JWT.decode(jwtToken).getClaim("userId");
//
//        String userId = claim.asString();
//        System.out.println(userId);
//
//    }
//
//    private static KeyPair generateRSAKeyPair() throws Exception {
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//        keyPairGenerator.initialize(512); // Размер ключа: 2048 бит
//        return keyPairGenerator.generateKeyPair();
//    }

}