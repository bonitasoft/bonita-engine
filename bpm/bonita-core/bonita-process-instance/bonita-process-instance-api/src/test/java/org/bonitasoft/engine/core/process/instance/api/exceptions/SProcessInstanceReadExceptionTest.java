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
package org.bonitasoft.engine.core.process.instance.api.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.junit.Test;

/**
 * 
 * @author Celine Souchet
 * 
 */
public class SProcessInstanceReadExceptionTest {

    @Test
    public void constructTheExceptionWithoutADescriptor() {
        final SBonitaReadException cause = new SBonitaReadException("problem");
        final SProcessInstanceReadException exception = new SProcessInstanceReadException(cause);
        assertTrue(exception.getMessage().isEmpty());
    }

    @Test
    public void constructTheExceptionWithADescriptor() {
        final SelectOneDescriptor<PersistentObject> descriptor = new SelectOneDescriptor<PersistentObject>("getPersistentObject", null, PersistentObject.class);
        final SBonitaReadException cause = new SBonitaReadException("problem", null, descriptor);
        final SProcessInstanceReadException exception = new SProcessInstanceReadException(cause);
        assertEquals(descriptor.toString(), exception.getMessage());
    }

}
