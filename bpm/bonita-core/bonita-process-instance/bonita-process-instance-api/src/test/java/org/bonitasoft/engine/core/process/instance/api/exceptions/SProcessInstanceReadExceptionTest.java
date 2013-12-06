package org.bonitasoft.engine.core.process.instance.api.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.junit.Test;

public class SProcessInstanceReadExceptionTest {

    @Test
    public void constructTheExceptionWithoutADescriptor() {
        final SBonitaReadException cause = new SBonitaReadException("problem");
        final SProcessInstanceReadException exception = new SProcessInstanceReadException(cause);
        assertNull(exception.getMessage());
    }

    @Test
    public void constructTheExceptionWithADescriptor() {
        final SelectOneDescriptor<PersistentObject> descriptor = new SelectOneDescriptor<PersistentObject>("getPersistentObject", null, PersistentObject.class);
        final SBonitaReadException cause = new SBonitaReadException("problem", null, descriptor);
        final SProcessInstanceReadException exception = new SProcessInstanceReadException(cause);
        assertEquals(descriptor.toString(), exception.getMessage());
    }

}
