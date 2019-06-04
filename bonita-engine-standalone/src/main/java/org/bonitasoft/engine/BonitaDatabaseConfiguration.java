package org.bonitasoft.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(exclude = "password")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BonitaDatabaseConfiguration {

    private String driverClassName;
    private String url;
    private String dbVendor;
    private String user;
    private String password;


    public boolean isEmpty() {
        return isNullOrEmpty(driverClassName) &&
                isNullOrEmpty(url) &&
                isNullOrEmpty(dbVendor) &&
                isNullOrEmpty(user) &&
                isNullOrEmpty(password);
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * The name of the driver class, if the default one does not suit your needs
     *
     * @param driverClassName the name of the driver class to use
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * The URL to connect to the database
     *
     * @param url the URL to connect to the database
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The database vendor to use with Bonita Engine.
     * Supported values are h2, mysql, postgres, sqlserver, oracle. Default value, if not specified, is h2.
     *
     * @param dbVendor the database vendor to use with Bonita Engine
     */
    public void setDbVendor(String dbVendor) {
        this.dbVendor = dbVendor;
    }

    /**
     * The connection user name to use to access the Bonita database
     *
     * @param user the connection user name to use to access the Bonita database
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * The connection password corresponding to the specified user name
     *
     * @param password the connection password corresponding to the specified user name
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
