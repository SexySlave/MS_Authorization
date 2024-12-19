package ms.netty_old.server.Hibernate;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UsersDefault {
    public UsersDefault(String login, String password,String JWTrefreshtoken, int JWTversion) {
        this.login = login;
        this.password = password;
        this.JWTversion = JWTversion;
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

    @Column
    private int JWTversion;

    @Column
    private int refreshtokenUUID;

    public int getRefreshtokenUUID() {
        return refreshtokenUUID;
    }

    public void setRefreshtokenUUID(int refreshtokenUUID) {
        this.refreshtokenUUID = refreshtokenUUID;
    }

    @Column
    public int getJWTversion() {
        return JWTversion;
    }

    public void setJWTversion(int JWTversion) {
        this.JWTversion = JWTversion;
    }


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
