package ms.netty_old.trash;

import ms.netty_old.server.Hibernate.UsersDefault;
import org.hibernate.cfg.Configuration;

public class DBConfig {

    public Configuration getCfg() {
        return cfg;
    }

    protected Configuration cfg = new Configuration();

    public DBConfig(){
        cfg.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        //cfg.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/ms_authorization");
        cfg.setProperty("hibernate.connection.username", "root");
        cfg.setProperty("hibernate.connection.password", "");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        cfg.addAnnotatedClass(UsersDefault.class);
    }


    public void setDriver(String driver) {
        this.cfg.setProperty("hibernate.connection.driver_class", driver);
    }

    public void setUrl(String url) {
        this.cfg.setProperty("hibernate.connection.url", url);
    }

    public void setUsername(String username) {
        this.cfg.setProperty("hibernate.connection.username", username);
    }

    public void setPassword(String password) {
        this.cfg.setProperty("hibernate.connection.password", password);
    }

    public void setDialect(String dialect) {
        this.cfg.setProperty("hibernate.dialect", dialect);
    }

    public void addAnnotatedClass(Class annotatedClass){
        this.cfg.addAnnotatedClass(annotatedClass);
    }
}
