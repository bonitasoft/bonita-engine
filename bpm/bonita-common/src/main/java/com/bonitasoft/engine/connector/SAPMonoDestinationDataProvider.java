/**
 * Copyright (C) 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package com.bonitasoft.engine.connector;

import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

/**
 * Used by SAP connector in order to synchronize SAP {@code com.sap.conn.jco.ext.DestinationDataProvider} used
 * Warning: Internal use only!
 * This class is subject to change without notice
 *
 * @deprecated use {@link org.bonitasoft.engine.connector.sap.SAPMonoDestinationDataProvider} instead.
 */
public class SAPMonoDestinationDataProvider implements DestinationDataProvider {

    private static SAPMonoDestinationDataProvider destinationDataProvider = null;
    private org.bonitasoft.engine.connector.sap.SAPMonoDestinationDataProvider delegate;

    private SAPMonoDestinationDataProvider(
            final org.bonitasoft.engine.connector.sap.SAPMonoDestinationDataProvider delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * BE CAREFUL: only one destinationName is possible
     *
     * @param destinationName
     * @return
     */
    public static synchronized SAPMonoDestinationDataProvider getInstance(final String destinationName)
            throws IllegalStateException {
        if (destinationDataProvider == null) {
            destinationDataProvider = new SAPMonoDestinationDataProvider(
                    org.bonitasoft.engine.connector.sap.SAPMonoDestinationDataProvider.getInstance(destinationName));
        }
        return destinationDataProvider;
    }

    @Override
    public Properties getDestinationProperties(final String destinationName) {
        return delegate.getDestinationProperties(destinationName);
    }

    @Override
    public void setDestinationDataEventListener(final DestinationDataEventListener eventListener) {
        delegate.setDestinationDataEventListener(eventListener);
    }

    @Override
    public boolean supportsEvents() {
        return delegate.supportsEvents();
    }

    public void changeProperties(final Properties properties) {
        delegate.changeProperties(properties);
    }

    public String getDestinationName() {
        return delegate.getDestinationName();
    }

    /**
     * Use it carefully!!! If someone retrieved an Instance and you clear it it will break everything, currently usage
     * planned only for test purpose.
     */
    public static void clear() {
        org.bonitasoft.engine.connector.sap.SAPMonoDestinationDataProvider.clear();
    }

}
