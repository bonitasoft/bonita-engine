/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageWithContent;

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
