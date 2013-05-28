/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

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
