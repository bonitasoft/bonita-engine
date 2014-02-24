package com.bonitasoft.engine.bdm;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class VoidAnswer implements Answer<Void> {

    @Override
    public Void answer(final InvocationOnMock invocation) throws Throwable {
        return null;
    }

}
