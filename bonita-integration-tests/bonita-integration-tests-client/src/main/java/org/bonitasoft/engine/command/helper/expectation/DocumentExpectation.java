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
package org.bonitasoft.engine.command.helper.expectation;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class DocumentExpectation {

    private CommonAPIIT testCase;

    private ProcessInstance process;

    private String name;

    public DocumentExpectation(CommonAPIIT testCase, ProcessInstance process, String name) {
        this.testCase = testCase;
        this.process = process;
        this.name = name;
    }

    public void toBe(String variable) throws DocumentNotFoundException {
        Document document = testCase.getProcessAPI().getLastDocument(process.getId(), name);
        String storageId = document.getContentStorageId();
        assertEquals(variable, new String(testCase.getProcessAPI().getDocumentContent(storageId)));
    }
    
}
