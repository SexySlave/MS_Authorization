package ms.netty_old.debug;

import ms.netty_old.trash.DBConfig;
import ms.netty_old.server.Hibernate.UsersDefault;

public class DefaultDBConfig extends DBConfig {

    public DefaultDBConfig(){
        cfg.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/ms_authorization");
        cfg.setProperty("hibernate.connection.username", "root");
        cfg.setProperty("hibernate.connection.password", "");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        cfg.addAnnotatedClass(UsersDefault.class);
    }

}
