/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector;

import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;

/**
 * Used by SAP connector in order to synchronize SAP {@code com.sap.conn.jco.ext.DestinationDataProvider} used
 * 
 * 
 * Warning: Internal use only!
 * This class is subject to change without notice
 *
 * @author Aurelien Pupier
 */
public class SAPMonoDestinationDataProvider implements DestinationDataProvider {

    private static SAPMonoDestinationDataProvider destinationDataProvider = null;

    private DestinationDataEventListener listener;

    private Properties properties;

    private final String destinationName;

    private SAPMonoDestinationDataProvider(final String destinationName) {
        super();
        this.destinationName = destinationName;
    }

    /**
     * BE CAREFUL: only one destinationName is possible
     *
     * @param destinationName
     * @return
     */
    public static synchronized SAPMonoDestinationDataProvider getInstance(final String destinationName) throws IllegalStateException {
        // TODO: handle several destinationName correctly
        if (destinationDataProvider == null) {
            destinationDataProvider = new SAPMonoDestinationDataProvider(destinationName);
            Environment.registerDestinationDataProvider(destinationDataProvider);
        } else if (!destinationName.equals(destinationDataProvider.getDestinationName())) {
            throw new IllegalStateException("You can use only one SAP destination (and they should use the configuration). The current one is named "
                    + destinationDataProvider.getDestinationName());
        }
        return destinationDataProvider;
    }

    @Override
    public Properties getDestinationProperties(final String destinationName) {
        if (destinationName.equals(this.destinationName) && properties != null) {
            return properties;
        }
        throw new RuntimeException("Destination " + destinationName + " is not available");
    }

    @Override
    public void setDestinationDataEventListener(final DestinationDataEventListener eventListener) {
        listener = eventListener;
    }

    @Override
    public boolean supportsEvents() {
        return true;
    }

    public void changeProperties(final Properties properties) {
        if (properties == null) {
            listener.deleted(destinationName);
            this.properties = null;
        } else {
            if (listener != null && !properties.equals(this.properties)) {
                listener.updated(destinationName);
            }
            this.properties = properties;
        }
    }

    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Use it carefully!!! If someone retrieved an Instance and you clear it it will break everything, currently usage planned only for test purpose.
     */
    public static void clear() {
        if (destinationDataProvider != null) {
            Environment.unregisterDestinationDataProvider(destinationDataProvider);
            destinationDataProvider = null;
        }
    }

}
