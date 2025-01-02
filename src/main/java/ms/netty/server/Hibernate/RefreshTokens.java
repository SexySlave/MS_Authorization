package ms.netty.server.Hibernate;

import jakarta.persistence.*;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokens {

    public RefreshTokens() {
    }

    public RefreshTokens(int user_id, String token, int tokenVersion, String mac_address, UsersDefault usersDefault) {
        this.user_id = user_id;
        this.token = token;
        this.tokenVersion = tokenVersion;
        this.mac_address = mac_address;
        this.usersDefault = usersDefault;
    }

    @Id
    @Column(name = "tokenUUID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tokenUUID;

    @Column(name = "user_id")
    private int user_id;

    @Column(name = "token")
    private String token;

    @Column(name = "tokenVersion")
    private int tokenVersion;

    @Column(name = "mac_address")
    private String mac_address;


    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne
    UsersDefault usersDefault;

    public UsersDefault getUsersDefault() {
        return usersDefault;
    }

    public void setUsersDefault(UsersDefault usersDefault) {
        this.usersDefault = usersDefault;
    }

    public int getTokenUUID() {
        return tokenUUID;
    }

    public void setTokenUUID(int id) {
        this.tokenUUID = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }
}
