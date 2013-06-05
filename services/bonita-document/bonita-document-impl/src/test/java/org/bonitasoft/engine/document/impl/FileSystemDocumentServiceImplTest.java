/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.document.impl;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 * 
 */
public class FileSystemDocumentServiceImplTest {

    private FileSystemDocumentServiceImpl fileSystemDocumentServiceImpl;

    @Before
    public void setUp() throws Exception {
        fileSystemDocumentServiceImpl = new FileSystemDocumentServiceImpl("storePath");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.document.impl.FileSystemDocumentServiceImpl#getContent(java.lang.String)}.
     */
    @Test
    public final void getContent() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.document.impl.FileSystemDocumentServiceImpl#storeDocumentContent(org.bonitasoft.engine.document.model.SDocument, byte[])}.
     */
    @Test
    public final void storeDocumentContent() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.document.impl.FileSystemDocumentServiceImpl#deleteDocumentContent(java.lang.String)}.
     */
    @Test
    public final void deleteDocumentContent() {
        // TODO : Not yet implemented
    }

}
