/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

/**
 * @author Charles Souillard
 */
public class StatementMapping {

    private final String sourceStatement;

    private final String destinationStatement;

    private final String parameterName;

    private final String parameterValue;

    public StatementMapping(final String sourceStatement, final String destinationStatement) {
        super();
        this.sourceStatement = sourceStatement;
        this.destinationStatement = destinationStatement;
        this.parameterName = null;
        this.parameterValue = null;
    }

    public StatementMapping(final String sourceStatement, final String destinationStatement, final String parameterName, final String parameterValue) {
        this.sourceStatement = sourceStatement;
        this.destinationStatement = destinationStatement;
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
    }

    public String getSourceStatement() {
        return this.sourceStatement;
    }

    public String getDestinationStatement() {
        return this.destinationStatement;
    }

    public boolean hasParameter() {
        return this.parameterName != null;
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public String getParameterValue() {
        return this.parameterValue;
    }

}
