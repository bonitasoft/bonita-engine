/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data;

import java.lang.reflect.Method;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;

public class BusinessDataUpdateConnector extends AbstractConnector {

    @Override
    public void validateInputParameters() {
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final Object bizData = getInputParameter("bizData");
        try {
            Method method = bizData.getClass().getMethod("addToPhoneNumbers", String.class);
            method.invoke(bizData, "48665421");

            method = bizData.getClass().getMethod("setLastName", String.class);
            method.invoke(bizData, "Hakkinen");
            setOutputParameter("output1", bizData);
        } catch (final Exception e) {
            throw new ConnectorException(e);
        }
    }

}
