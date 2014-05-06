/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageWithContent;

/**
 * @author Baptiste Mesta
 */
public class SPageWithContentImpl extends SPageImpl implements SPageWithContent {

    private static final long serialVersionUID = -5601507328546725517L;

    private byte[] content;

    public SPageWithContentImpl() {
    }

    public SPageWithContentImpl(final SPage sPage, final byte[] pagContent) {
        super(sPage);
        setContent(pagContent);
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    public void setContent(final byte[] content) {
        this.content = content;
    }

    @Override
    public String getDiscriminator() {
        return SPageWithContentImpl.class.getName();
    }

}
