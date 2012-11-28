/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.platform;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Feng Hui
 */
public interface Tenant extends Serializable {

    long getId();

    String getName();

    String getDescription();

    String getIconName();

    String getIconPath();

    Date getCreationDate();

    String getState();

    boolean isDefaultTenant();

}
