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
package org.bonitasoft.engine.core.connector;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.connector.Connector;

/**
 * Contains the instantiated connector and its result
 * It is used to give make the connector service return both the instantiated connector and its result
 * in order to be able to call disconnect on it in the execute operation method
 * 
 * @author Baptiste Mesta
 */
public class ConnectorResult {

    private Connector connector;

    private Map<String, Object> result;

    /**
     * @param connector
     * @param result
     */
    public ConnectorResult(final Connector connector, final Map<String, Object> result) {
        super();
        this.connector = connector;
        this.result = result;
    }

    /**
     * @return the connector
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * @param connector
     *            the connector to set
     */
    public void setConnector(final Connector connector) {
        this.connector = connector;
    }

    /**
     * @return the result
     */
    public Map<String, Object> getResult() {
        if (result == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * @param result
     *            the result to set
     */
    public void setResult(final Map<String, Object> result) {
        this.result = result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (connector == null ? 0 : connector.hashCode());
        result = prime * result + (this.result == null ? 0 : this.result.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConnectorResult other = (ConnectorResult) obj;
        if (connector == null) {
            if (other.connector != null) {
                return false;
            }
        } else if (!connector.equals(other.connector)) {
            return false;
        }
        if (result == null) {
            if (other.result != null) {
                return false;
            }
        } else if (!result.equals(other.result)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConnectorResult [connector=" + connector + ", result=" + result + "]";
    }

}
