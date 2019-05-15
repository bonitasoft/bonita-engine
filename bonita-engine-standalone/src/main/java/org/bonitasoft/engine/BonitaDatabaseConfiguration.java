package org.bonitasoft.engine;

public class BonitaDatabaseConfiguration {

    private String dbVendor = "h2";
    private String server;
    private String port;
    private String databaseName;
    private String user;
    private String password;

    public String getDbVendor() {
        return dbVendor;
    }

    /**
     * Specify on which database vendor
     */
    public void setDbVendor(String dbVendor) {
        this.dbVendor = dbVendor;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUser() {
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
}
