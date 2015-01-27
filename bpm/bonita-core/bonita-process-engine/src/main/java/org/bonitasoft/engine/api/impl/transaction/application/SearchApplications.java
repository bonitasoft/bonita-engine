/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api.impl.transaction.application;

import java.util.List;

import org.bonitasoft.engine.api.impl.converter.ApplicationConvertor;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SearchApplications extends AbstractSearchEntity<Application, SApplication> {

    private final ApplicationService applicationService;
    private final ApplicationConvertor convertor;

    public SearchApplications(final ApplicationService applicationService, final SearchEntityDescriptor searchDescriptor, final SearchOptions options,
            final ApplicationConvertor convertor) {
        super(searchDescriptor, options);
        this.applicationService = applicationService;
        this.convertor = convertor;
    }

    @Override
    public long executeCount(final QueryOptions queryOptions) throws SBonitaReadException {
        return applicationService.getNumberOfApplications(queryOptions);
    }

    @Override
    public List<SApplication> executeSearch(final QueryOptions queryOptions) throws SBonitaReadException {
        return applicationService.searchApplications(queryOptions);
    }

    @Override
    public List<Application> convertToClientObjects(final List<SApplication> serverObjects) {
        return convertor.toApplication(serverObjects);
    }

}
