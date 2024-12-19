package ms.netty_old.trash;

public class Config {
    DBConfig dbConfig;
    int port;

    public Config(DBConfig dbConfig, int port){
        this.dbConfig = dbConfig;
        this.port = port;

//        try {
//            SessionFactory sessionFactory = dbConfig.getCfg().buildSessionFactory();
//            Session session = sessionFactory.openSession();
//            UsersDefault user = session.get(UsersDefault.class, 1);
//
//            System.out.println(user.getLogin());
//            System.out.println(user.getPassword());
//
//
//            session.close();
//            sessionFactory.close();
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    public DBConfig getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
