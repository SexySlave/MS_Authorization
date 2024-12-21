package ms.netty.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import ms.netty.server.Hibernate.RefreshTokens;
import ms.netty.server.Hibernate.UsersDefault;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.mindrot.jbcrypt.BCrypt;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Authorization {

    SessionFactory sessionFactory;
    KeyPair keyPair;
    RSAPublicKey publicKey;
    RSAPrivateKey privateKey;
    Algorithm algorithm;
    UsersDefault user;

    public Authorization(KeyPair keyPair, SessionFactory sessionFactory) {
        this.keyPair = keyPair;
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.algorithm = Algorithm.RSA256(publicKey, privateKey);
        this.sessionFactory = sessionFactory;
    }


    public Boolean checkUser(String logData) {
        try {
            Session session = sessionFactory.openSession();
            String[] logDataParts = logData.split(":");
            UsersDefault user = session.createQuery("from UsersDefault where login = :l", UsersDefault.class)
                    .setParameter("l", logDataParts[0])
                    .getSingleResultOrNull();

            session.close();
            System.out.println("session is open" + session.isOpen());

            if (user != null && BCrypt.checkpw(logDataParts[1], user.getPassword())) {
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

    public void registerUser(String logData) {
        Session session = sessionFactory.openSession();
        try {
            session.getTransaction().begin();
            String[] logDataParts = logData.split(":");
            String hashedPassword = BCrypt.hashpw(logDataParts[1], BCrypt.gensalt());
            UsersDefault newUser = new UsersDefault(logDataParts[0], hashedPassword);
            session.persist(newUser);
            session.getTransaction().commit();
            session.getTransaction().begin();
            RefreshTokens newRefreshToken = new RefreshTokens(newUser.getId(), null, 0, logDataParts[2], newUser);
            session.persist(newRefreshToken);
            session.getTransaction().commit();
            user = newUser;
            user.getRefreshTokens().add(newRefreshToken);
        } catch (Exception e) {
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
    }

    public boolean validateJWT(String jwtToken) throws NoSuchAlgorithmException {
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("MS_AUTHORIZATION").build();
        try {
            DecodedJWT decodedJWT = verifier.verify(jwtToken);
            System.out.println(decodedJWT.getPayload());
            return true;
        } catch (JWTVerificationException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public String getJWTType(String jwtToken) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("MS_AUTHORIZATION").build();
            DecodedJWT decodedJWT = verifier.verify(jwtToken);
            return decodedJWT.getClaim("type").asString();
        } catch (Exception e) {
            return " ";
        }
    }


    public String generateAccessJWT() {
        String jwtAccessToken = JWT.create().withIssuer("MS_AUTHORIZATION").withSubject("MS_AUTHORIZATION_user").withClaim("type", "accesstoken")
                //.withClaim("version", "15647839049гоалвд234")
                .withIssuedAt(new Date()).withExpiresAt(new Date(System.currentTimeMillis() + 60000L * 15)).withJWTId(UUID.randomUUID().toString()).withNotBefore(new Date(System.currentTimeMillis() - 1000L)).sign(algorithm);
        return jwtAccessToken;
    }

    public String generateRefreshJWT(String MACAddress) {

        updateRefreshTokenVersion(user.getId(), MACAddress);
        updateInternalUserRefreshTokens();

        String jwtRefreshToken = JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "refreshtoken")
                .withClaim("version", user.getRefreshTokenByMacAddress(MACAddress).getTokenVersion())
                .withClaim("refreshtokenuuid", user.getRefreshTokenByMacAddress(MACAddress).getTokenUUID())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 60)) // 60 days
                .withJWTId(UUID.randomUUID().toString()).withNotBefore(new Date(System.currentTimeMillis() - 1000L)).sign(algorithm);
        return jwtRefreshToken;
    }

    public String generateRefereshJWTFromJWT(String refreshJWT) {  // using only when obtaining refreshtoken
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("MS_AUTHORIZATION").build();

        int refreshUUID = verifier.verify(refreshJWT).getClaim("refreshtokenuuid").asInt();


        updateRefreshTokenVersionByUUID(refreshUUID);
        updateInternalUserByRefreshUUID(refreshUUID);

        String jwtRefreshToken = JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "refreshtokenuuid")
                .withClaim("version", getRefreshTokenVersion(refreshUUID))
                .withClaim("refreshtokenuuid", refreshUUID)
                .withIssuedAt(new Date()).withExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 60)) // 60 days
                .withJWTId(UUID.randomUUID().toString()).withNotBefore(new Date(System.currentTimeMillis() - 1000L)).sign(algorithm);
        return jwtRefreshToken;

    }

    private int getRefreshTokenVersion(int tokenUUID) {
        Session session = sessionFactory.openSession();
        int JWTversion = session.createQuery("SELECT tokenVersion from RefreshTokens where tokenUUID =:i", int.class).setParameter("i", tokenUUID).getSingleResultOrNull();


        session.close();
        sessionFactory.close();
        return JWTversion;
    }

//    private int getRefereshTokenUUID(int userId) {
//        SessionFactory sessionFactory = cfg.buildSessionFactory();
//        Session session = sessionFactory.openSession();
//        int JWTUUID = session.createQuery("SELECT refreshTokenUUID from UsersDefault where id =:i", int.class).setParameter("i", userId).getSingleResultOrNull();
//
//
//        session.close();
//        sessionFactory.close();
//        return JWTUUID;
//    }

    private void updateRefreshTokenVersion(int userId, String MacAddress) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.createMutationQuery("update RefreshTokens set tokenVersion = :v where user_id = :i and mac_address = : mac").setParameter("mac", MacAddress).setParameter("i", userId).setParameter("v", (session.createQuery("SELECT tokenVersion from RefreshTokens where user_id =:i", int.class).setParameter("i", userId).getSingleResultOrNull()) + 1).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    private void updateRefreshTokenVersionByUUID(int refreshTokenUUID) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.createMutationQuery("update RefreshTokens set tokenVersion = :v where tokenUUID = :i").setParameter("i", refreshTokenUUID).setParameter("v", (session.createQuery("SELECT JWTversion from UsersDefault where refreshtokenUUID =:i", int.class).setParameter("i", refreshTokenUUID).getSingleResultOrNull()) + 1).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
        } finally {
            session.close();
        }


    }

    private void updateInternalUserByRefreshUUID(int refreshTokenUUID) {
        Session session = sessionFactory.openSession();
        try {



            UsersDefault user = session.createQuery("from UsersDefault where id =:i", UsersDefault.class).setParameter("i",
                    session.createQuery("FROM RefreshTokens WHERE tokenUUID =: UUID", RefreshTokens.class).setParameter("UUID", refreshTokenUUID).getSingleResultOrNull().getUser_id()
            ).getSingleResultOrNull();



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


        } finally {
            session.close();
        }
    }

    private void updateInternalUserRefreshTokens(){
       Session session = sessionFactory.openSession();
        try {
            session.getTransaction().begin();
            List<RefreshTokens> refreshTokens = session.createQuery("FROM RefreshTokens WHERE user_id =: id", RefreshTokens.class).setParameter("id", user.getId()).getResultList();
            session.getTransaction().commit();
            user.setRefreshTokens(refreshTokens);
        } catch (Exception e){
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
    }





}


