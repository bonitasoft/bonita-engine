/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.page;

/**
 * @author Emmanuel Duchastenier
 */
public class SSavePageWithContentImpl extends SPageImpl implements SSavePageWithContent {

    private static final long serialVersionUID = -2359394359559272541L;

    private byte[] content;

    public SSavePageWithContentImpl() {
    }

    public SSavePageWithContentImpl(final SPage sPage, final byte[] pageContent) {
        super(sPage);
        setContent(pageContent);
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
        return SSavePageWithContent.class.getName();
    }

}
