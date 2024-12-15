package ms.netty.server.Hibernate;

import jakarta.persistence.*;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokens {


    public RefreshTokens() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column
    private int user_id;

    @Column
    private int tokenUUID;

    @Column
    private int tokenVersion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getTokenUUID() {
        return tokenUUID;
    }

    public void setTokenUUID(int tokenUUID) {
        this.tokenUUID = tokenUUID;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }
}
