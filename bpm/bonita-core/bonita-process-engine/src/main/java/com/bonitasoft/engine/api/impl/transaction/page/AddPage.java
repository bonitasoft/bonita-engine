/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.page;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

import com.bonitasoft.engine.core.page.PageService;
import com.bonitasoft.engine.core.page.SPage;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class AddPage implements TransactionContentWithResult<SPage> {

    private final byte[] content;

    private SPage page;

    private final PageService pageService;

    public AddPage(final PageService reportingService, final SPage page, final byte[] content) {
        this.pageService = reportingService;
        this.page = page;
        this.content = content;
    }

    @Override
    public void execute() throws SBonitaException {
        page = pageService.addPage(page, content);
    }

    @Override
    public SPage getResult() {
        return page;
    }

}
