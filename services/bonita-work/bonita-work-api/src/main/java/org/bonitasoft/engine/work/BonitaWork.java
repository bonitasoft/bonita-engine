package org.bonitasoft.engine.work;

import java.io.Serializable;

public interface BonitaWork extends Runnable, Serializable {

    String getDescription();

    void setTenantId(long tenantId);

}
