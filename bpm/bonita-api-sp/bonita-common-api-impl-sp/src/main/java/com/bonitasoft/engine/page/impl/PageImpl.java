/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import com.bonitasoft.engine.page.Page;

/**
 * @author laurent Leseigneur
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.impl.PageImpl} instead.
 * @see org.bonitasoft.engine.impl.PageImpl
 */
@Deprecated
public class PageImpl extends org.bonitasoft.engine.impl.PageImpl implements Page {

    private static final long serialVersionUID = 5785414687043871169L;

    public PageImpl(org.bonitasoft.engine.page.Page page) {
        super(page.getId(), page.getName(), page.getDisplayName(), page.isProvided(), page.getDescription(), page.getInstallationDate().getTime(), page
                .getInstalledBy(), page.getLastModificationDate().getTime(), page.getLastUpdatedBy(), page.getContentName());
    }

}
