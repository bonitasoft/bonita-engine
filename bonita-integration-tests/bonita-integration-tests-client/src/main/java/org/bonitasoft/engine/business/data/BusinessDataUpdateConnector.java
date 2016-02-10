/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.business.data;

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
