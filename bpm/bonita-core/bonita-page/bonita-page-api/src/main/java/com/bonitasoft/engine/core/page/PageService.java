/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.page;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Laurent Leseigneur
 */
public interface PageService {

    String PAGE = "PAGE";


    SPage addPage(SPage page, byte[] content) throws SPageCreationException, SPageAlreadyExistsException;

    SPage getPage(long pageId) throws SBonitaReadException, SPageNotFoundException;

    /**
     * Get a report from its name.
     * 
     * @param pageId
     *            the id of the report to retrieve.
     * @return the page if found, NULL if not found.
     * @throws SBonitaReadException
     *             if an read error occurs.
     */
    SPage getPageByName(String pageId) throws SBonitaReadException;

    long getNumberOfPages(QueryOptions options) throws SBonitaSearchException;

    List<SPage> searchPages(QueryOptions options) throws SBonitaSearchException;

    void deletePage(long reportId) throws SPageDeletionException, SPageNotFoundException;

    byte[] getPageContent(long reportId) throws SBonitaReadException, SPageNotFoundException;

}
