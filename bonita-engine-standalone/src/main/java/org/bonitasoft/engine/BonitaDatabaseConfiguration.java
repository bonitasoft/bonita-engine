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
@Builder
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

}
