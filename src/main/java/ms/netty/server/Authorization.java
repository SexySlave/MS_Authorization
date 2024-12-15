package ms.netty.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import ms.netty.server.Hibernate.UsersDefault;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

public class Authorization {
    Configuration cfg;
    SessionFactory sessionFactory;

    KeyPair keyPair;
    RSAPublicKey publicKey;
    RSAPrivateKey privateKey;

    Algorithm algorithm;
    //Algorithm algorithm = Algorithm.RSA256()
    UsersDefault user;

    public Authorization(KeyPair keyPair) {
        cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/ms_authorization");
        cfg.setProperty("hibernate.connection.username", "root");
        cfg.setProperty("hibernate.connection.password", "");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        cfg.addAnnotatedClass(UsersDefault.class);
        sessionFactory = cfg.buildSessionFactory();
        this.keyPair = keyPair;
        this. publicKey = (RSAPublicKey) keyPair.getPublic();
        this. privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.algorithm = Algorithm.RSA256(publicKey, privateKey);
    }


    public Boolean checkUser(String logData) {
        try {


            Session session = sessionFactory.openSession();
            UsersDefault user = session.createQuery("from UsersDefault where login = :l and password =:p", UsersDefault.class)
                    .setParameter("l", logData.split(":")[0])
                    .setParameter("p", logData.split(":")[1]).getSingleResultOrNull();


            session.close();
            //sessionFactory.close();

            if (user != null) {
                this.user = user;
                System.out.println("ist null");
                System.out.println(user.getLogin());
                System.out.println(user.getPassword());
                return true;

            } else {
                System.out.println("is null");
                return false;
            }


        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }

    }
    public void registerUser(String logData){
        Session session = sessionFactory.openSession();
        session.getTransaction().begin();
        session.persist(new UsersDefault(logData.split(":")[0], logData.split(":")[1], null, 0));
        session.getTransaction().commit();
    }

//    public String generateJWT() throws Exception {
//
//        String jwtToken = JWT.create()
//                .withIssuer("Baeldung")
//                .withSubject("Baeldung Details")
//                .withClaim("userId", "15647839049гоалвд234")
//                .withIssuedAt(new Date())
//                .withExpiresAt(new Date(System.currentTimeMillis() + 5000L))
//                .withJWTId(UUID.randomUUID()
//                        .toString())
//                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
//                .sign(algorithm);
//        return jwtToken;
//    }
//
    public boolean validateJWT(String jwtToken) throws NoSuchAlgorithmException {

        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("MS_AUTHORIZATION")
                .build();
        try {
            DecodedJWT decodedJWT = verifier.verify(jwtToken);
              System.out.println(decodedJWT.getPayload());
//            Claim claim = decodedJWT.getClaim("userId");
//            String userId = claim.asString();
//            System.out.println(userId);
            return true;
        } catch (JWTVerificationException e) {
            System.out.println(e.getMessage());
            return false;
        }
//        Claim claim = JWT.decode(jwtToken).getClaim("userId");
//
//        String userId = claim.asString();
//        System.out.println(userId);

    }

    public String getJWTType(String jwtToken){
        try {
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("MS_AUTHORIZATION")
                .build();

            DecodedJWT decodedJWT = verifier.verify(jwtToken);
            return decodedJWT.getClaim("type").asString();
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            return " ";
        }
    }





    //public int getUserIdByLogData()

    public String generateJWTPair(int useId) throws Exception {

        String jwtAccessToken = JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "accesstoken")
                //.withClaim("version", "15647839049гоалвд234")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 5000L))
                .withJWTId(UUID.randomUUID()
                        .toString())
                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
                .sign(algorithm);



        String jwtRefreshToken = JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "refreshtoken")
                .withClaim("version", "15647839049гоалвд234")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000L*60*60*24*60)) // 60 days
                .withJWTId(UUID.randomUUID()
                        .toString())
                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
                .sign(algorithm);
        return jwtRefreshToken;
    }

    public String generateAccessJWT(){
        String jwtAccessToken = JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "accesstoken")
                //.withClaim("version", "15647839049гоалвд234")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60000L*15))
                .withJWTId(UUID.randomUUID()
                        .toString())
                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
                .sign(algorithm);
        return jwtAccessToken;
    }

    public String generateRefreshJWT(int userId, Boolean updateVersion){
        if (updateVersion){
            updateRefreshTokenVersion(userId);
        }
        String jwtRefreshToken = JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "refreshtoken")
                //.withClaim("redreshtokenUUID", )
                .withClaim("version", getRefreshTokenVersion(userId))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000L*60*60*24*60)) // 60 days
                .withJWTId(UUID.randomUUID()
                        .toString())
                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
                .sign(algorithm);
        return jwtRefreshToken;

    }

    public String generateRefreshJWT(Boolean updateVersion){

        if (updateVersion){
            updateRefreshTokenVersion(user.getId());
        }
        String jwtRefreshToken = JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "refreshtoken")
                .withClaim("version", user.getJWTversion())
                .withClaim("refreshtokenuuid", user.getRefreshtokenUUID())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000L*60*60*24*60)) // 60 days
                .withJWTId(UUID.randomUUID()
                        .toString())
                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
                .sign(algorithm);
        return jwtRefreshToken;
    }

    public String generateRefereshJWTFromJWT(String refreshJWT){  // using only when obtaining refreshtoken
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("MS_AUTHORIZATION")
                    .build();

            int refreshUUID = verifier.verify(refreshJWT).getClaim("refreshtokenuuid").asInt();


            updateRefreshTokenVersionByUUID(refreshUUID);
            updateInternalUserByRefreshUUID(refreshUUID);

            String jwtRefreshToken = JWT.create()
                    .withIssuer("MS_AUTHORIZATION")
                    .withSubject("MS_AUTHORIZATION_user")
                    .withClaim("type", "refreshtokenuuid")
                    .withClaim("version", user.getJWTversion())
                    .withClaim("refreshtokenuuid", user.getRefreshtokenUUID())
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 1000L*60*60*24*60)) // 60 days
                    .withJWTId(UUID.randomUUID()
                            .toString())
                    .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
                    .sign(algorithm);
            return jwtRefreshToken;

    }

    private int getRefreshTokenVersion(int userId){
        SessionFactory sessionFactory = cfg.buildSessionFactory();
        Session session = sessionFactory.openSession();
        int JWTversion = session.createQuery("SELECT JWTversion from UsersDefault where id =:i", int.class)
                .setParameter("i", userId).getSingleResultOrNull();


        session.close();
        sessionFactory.close();
        return JWTversion;
    }

    private  int getRefereshTokenUUID(int userId){
        SessionFactory sessionFactory = cfg.buildSessionFactory();
        Session session = sessionFactory.openSession();
        int JWTUUID = session.createQuery("SELECT refreshTokenUUID from UsersDefault where id =:i", int.class)
                .setParameter("i", userId).getSingleResultOrNull();


        session.close();
        sessionFactory.close();
        return JWTUUID;
    }

    private void updateRefreshTokenVersion(int userId){
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.createMutationQuery("update UsersDefault set JWTversion = :v where id = :i")
                    .setParameter("i", userId)
                    .setParameter("v",
                            (session.createQuery("SELECT JWTversion from UsersDefault where id =:i", int.class).setParameter("i", userId).getSingleResultOrNull())+1 )
                    .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
        } finally {
            session.close();
        }



    }

    private void updateRefreshTokenVersionByUUID(int refreshTokenUUID){
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.createMutationQuery("update UsersDefault set JWTversion = :v where refreshtokenUUID = :i")
                    .setParameter("i", refreshTokenUUID)
                    .setParameter("v",
                            (session.createQuery("SELECT JWTversion from UsersDefault where refreshtokenUUID =:i", int.class).setParameter("i", refreshTokenUUID).getSingleResultOrNull())+1 )
                    .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
        } finally {
            session.close();
        }



    }

    private void updateInternalUserByRefreshUUID(int refreshTokenUUID){
        try {


            Session session = sessionFactory.openSession();
            UsersDefault user = session.createQuery("from UsersDefault where refreshtokenUUID =:i", UsersDefault.class).setParameter("i", refreshTokenUUID).getSingleResultOrNull();


            session.close();
            //sessionFactory.close();

            if (user != null) {
                this.user = user;
                System.out.println("ist null");
                System.out.println(user.getLogin());
                System.out.println(user.getPassword());


            } else {
                System.out.println("is null");

            }


        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);


        }
    }

}


