package entry;

import ms.netty.server.Authorization;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
//        DBConfig mydbconfug = new DBConfig();
//        mydbconfug.setUrl("jdbc:mysql://localhost:3306/ms_authorization");
//        mydbconfug.setUsername("root");
//        mydbconfug.setPassword("");


        Authorization a = new Authorization(generateRSAKeyPair());
//        String jwt = a.generateJWT();
//        System.out.println(a.validateJWT(jwt));
        a.checkUser("lolipop:qwerty123");


        System.out.println(a.validateJWT(a.generateAccessJWT()));





















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