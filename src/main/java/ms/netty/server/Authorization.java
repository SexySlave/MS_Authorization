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

    private final SessionFactory sessionFactory;
    private final KeyPair keyPair;
    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final Algorithm algorithm;
    private UsersDefault user;

    public Authorization(KeyPair keyPair, SessionFactory sessionFactory) {
        this.keyPair = keyPair;
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.algorithm = Algorithm.RSA256(publicKey, privateKey);
        this.sessionFactory = sessionFactory;
    }

    public Boolean checkUser(String logData) {
        try (Session session = sessionFactory.openSession()) {
            String[] logDataParts = logData.split(":");
            UsersDefault user = session.createQuery("from UsersDefault where login = :l", UsersDefault.class)
                    .setParameter("l", logDataParts[0])
                    .getSingleResultOrNull();

            if (user != null && BCrypt.checkpw(logDataParts[1], user.getPassword())) {
                this.user = user;
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void registerUser(String logData) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                String[] logDataParts = logData.split(":");
                String hashedPassword = BCrypt.hashpw(logDataParts[1], BCrypt.gensalt());
                UsersDefault newUser = new UsersDefault(logDataParts[0], hashedPassword);
                session.persist(newUser);
                transaction.commit();

                transaction = session.beginTransaction();
                RefreshTokens newRefreshToken = new RefreshTokens(newUser.getId(), null, 0, logDataParts[2], newUser);
                session.persist(newRefreshToken);
                transaction.commit();

                user = newUser;
                user.getRefreshTokens().add(newRefreshToken);
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
            }
        }
    }

    public boolean validateJWT(String jwtToken) {
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("MS_AUTHORIZATION").build();
        try {
            verifier.verify(jwtToken);
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
        return JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "accesstoken")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60000L * 15)) // 15 min
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
                .sign(algorithm);
    }

    public String generateRefreshJWT(String MACAddress) {
        updateRefreshTokenVersion(user.getId(), MACAddress);
        updateInternalUserRefreshTokens();

        return JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "refreshtoken")
                .withClaim("version", user.getRefreshTokenByMacAddress(MACAddress).getTokenVersion())
                .withClaim("refreshtokenuuid", user.getRefreshTokenByMacAddress(MACAddress).getTokenUUID())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 15)) // 60 days
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
                .sign(algorithm);
    }

    public String generateRefreshJWTFromJWT(String refreshJWT) {
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("MS_AUTHORIZATION").build();
        int refreshUUID = verifier.verify(refreshJWT).getClaim("refreshtokenuuid").asInt();

        updateRefreshTokenVersionByUUID(refreshUUID);
        updateInternalUserByRefreshUUID(refreshUUID);

        return JWT.create()
                .withIssuer("MS_AUTHORIZATION")
                .withSubject("MS_AUTHORIZATION_user")
                .withClaim("type", "refreshtokenuuid")
                .withClaim("version", getRefreshTokenVersion(refreshUUID))
                .withClaim("refreshtokenuuid", refreshUUID)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 60)) // 60 days
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis() - 1000L))
                .sign(algorithm);
    }

    private int getRefreshTokenVersion(int tokenUUID) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("SELECT tokenVersion from RefreshTokens where tokenUUID = :i", int.class)
                    .setParameter("i", tokenUUID)
                    .getSingleResultOrNull();
        }
    }

    private void updateRefreshTokenVersion(int userId, String MacAddress) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                int newVersion = session.createQuery("SELECT tokenVersion from RefreshTokens where user_id = :i", int.class)
                        .setParameter("i", userId)
                        .getSingleResultOrNull() + 1;
                session.createMutationQuery("update RefreshTokens set tokenVersion = :v where user_id = :i and mac_address = :mac")
                        .setParameter("mac", MacAddress)
                        .setParameter("i", userId)
                        .setParameter("v", newVersion)
                        .executeUpdate();
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
            }
        }
    }

    private void updateRefreshTokenVersionByUUID(int refreshTokenUUID) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                int newVersion = session.createQuery("SELECT JWTversion from UsersDefault where refreshtokenUUID = :i", int.class)
                        .setParameter("i", refreshTokenUUID)
                        .getSingleResultOrNull() + 1;
                session.createMutationQuery("update RefreshTokens set tokenVersion = :v where tokenUUID = :i")
                        .setParameter("i", refreshTokenUUID)
                        .setParameter("v", newVersion)
                        .executeUpdate();
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
            }
        }
    }

    private void updateInternalUserByRefreshUUID(int refreshTokenUUID) {
        try (Session session = sessionFactory.openSession()) {
            UsersDefault user = session.createQuery("from UsersDefault where id = :i", UsersDefault.class)
                    .setParameter("i", session.createQuery("FROM RefreshTokens WHERE tokenUUID = :UUID", RefreshTokens.class)
                            .setParameter("UUID", refreshTokenUUID)
                            .getSingleResultOrNull()
                            .getUser_id())
                    .getSingleResultOrNull();

            if (user != null) {
                this.user = user;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private void updateInternalUserRefreshTokens() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<RefreshTokens> refreshTokens = session.createQuery("FROM RefreshTokens WHERE user_id = :id", RefreshTokens.class)
                        .setParameter("id", user.getId())
                        .getResultList();
                user.setRefreshTokens(refreshTokens);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
            }
        }
    }
}