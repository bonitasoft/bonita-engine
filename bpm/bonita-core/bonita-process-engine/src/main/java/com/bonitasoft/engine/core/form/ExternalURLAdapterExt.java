/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.form;

import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.form.ExternalURLAdapter;
import org.bonitasoft.engine.page.URLAdapterConstants;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;


/**
 * @author Baptiste Mesta, Anthony Birembaut
 */
public class ExternalURLAdapterExt extends ExternalURLAdapter {
    
    SessionAccessor sessionAccessor;
	
    public ExternalURLAdapterExt(SessionAccessor sessionAccessor) {
		this.sessionAccessor = sessionAccessor;
	}
	
	@Override
    protected void appendParameters(StringBuffer newURL, final Map<String, String[]> parameters) throws SExecutionException {
    	super.appendParameters(newURL, parameters);
        try {
        	appendParameter(newURL, URLAdapterConstants.TENANT_QUERY_PARAM, new String[] {String.valueOf(sessionAccessor.getTenantId())});
        } catch (STenantIdNotSetException e) {
        	throw new SExecutionException("Unable to retrieve the tenant Id", e);
        }
    }
}
