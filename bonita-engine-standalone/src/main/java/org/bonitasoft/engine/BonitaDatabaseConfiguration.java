package org.bonitasoft.engine;

public class BonitaDatabaseConfiguration {

    private String driver = "org.h2.Driver";
    private String url = "jdbc:h2:file:" +
            System.getProperty("org.bonitasoft.h2.database.dir", "./h2databasedir") +
            "/bonita;LOCK_MODE=0;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;";
    private String dbVendor = "h2";

    private String user = "sa";
    private String password = "";
    public String getDbVendor() {
        return dbVendor;
    }

    /**
     * Specify on which database vendor
     */
    public void setDbVendor(String dbVendor) {
        this.dbVendor = dbVendor;
    }

    public String getUserName() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
