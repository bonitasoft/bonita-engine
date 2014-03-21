/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.connector.exception;


/**
 * @author Feng Hui
 * @author Celine Souchet
 */
public class SConnectorValidationException extends SConnectorException {

    private static final long serialVersionUID = -7025831546419799447L;

    public SConnectorValidationException(String message) {
        super(message);
    }

    public SConnectorValidationException(Throwable t) {
        super(t);
    }

    public SConnectorValidationException(String message, Exception e) {
        super(message, e);
    }

}
