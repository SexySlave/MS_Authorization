package ms.netty.server.Hibernate;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class UsersDefault {
//    public UsersDefault(String login, String password, int refreshJWTUUID, int JWTversion) {
//        this.login = login;
//        this.password = password;
//        this.JWTversion = JWTversion;
//    }

    public UsersDefault(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public UsersDefault(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column
    private String login;

    @Column
    private String password;

//    @Column
//    private int JWTversion;
//
//    @Column
//    private int refreshtokenUUID;

//    @JoinColumn(name="refreshtokenUUID")
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "users")
//    @JoinColumn(name="refreshtokenUUID")
//    @OneToMany
        @ElementCollection
        private List<RefreshTokens> refreshTokens = new ArrayList<>();

        public List<RefreshTokens> getRefreshTokens() {
            return refreshTokens;
        }

        public void setRefreshTokens(List<RefreshTokens> refreshTokens) {
            this.refreshTokens = refreshTokens;
        }

        public RefreshTokens getRefreshTokenByMacAddress(String MacAddress) {
            for(RefreshTokens r: refreshTokens){
                if (r.getMac_address().equals(MacAddress)){
                    return r;
                }
            }
            return null;
        }

//    public int getRefreshtokenUUID() {
//        return refreshtokenUUID;
//    }
//
//    public void setRefreshtokenUUID(int refreshtokenUUID) {
//        this.refreshtokenUUID = refreshtokenUUID;
//    }
//
//    @Column
//    public int getJWTversion() {
//        return JWTversion;
//    }
//
//    public void setJWTversion(int JWTversion) {
//        this.JWTversion = JWTversion;
//    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullData(){return id+"/"+login+"/"+password; }
}
