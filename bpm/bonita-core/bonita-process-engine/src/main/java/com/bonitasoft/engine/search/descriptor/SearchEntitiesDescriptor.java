/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search.descriptor;


/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class SearchEntitiesDescriptor extends org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor {

    private final SearchLogDescriptor logDescriptor;

    protected final SearchProcessInstanceDescriptorExt processInstanceDescriptorExt;

    protected final SearchArchivedProcessInstancesDescriptorExt archivedProcessInstanceDescriptorExt;

    public SearchEntitiesDescriptor() {
        super();
        logDescriptor = new SearchLogDescriptor();
        processInstanceDescriptorExt = new SearchProcessInstanceDescriptorExt();
        archivedProcessInstanceDescriptorExt = new SearchArchivedProcessInstancesDescriptorExt();
    }

    public SearchLogDescriptor getLogDescriptor() {
        return logDescriptor;
    }

    @Override
    public SearchArchivedProcessInstancesDescriptorExt getArchivedProcessInstancesDescriptor() {
        return archivedProcessInstanceDescriptorExt;
    }

    @Override
    public SearchProcessInstanceDescriptorExt getProcessInstanceDescriptor() {
        return processInstanceDescriptorExt;
    }

    public SearchReportDescriptor getReportDescriptor() {
        return new SearchReportDescriptor();
    }

}
