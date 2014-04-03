package org.bonitasoft.engine.core.process.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;



public class SLoopActivityInstanceImplTest {
    
    private SLoopActivityInstanceImpl loop;
    
    @Before
    public void setUp() throws Exception {
        loop = new SLoopActivityInstanceImpl();
    }
    
    @Test
    public void mustExecuteOnAbortOrCancelProcess_return_false_when_is_stable() throws Exception {
        //given
        loop.setStable(true);
        
        //when
        boolean executeOnAbortOrCancelProcess = loop.mustExecuteOnAbortOrCancelProcess();

        //then
        assertThat(executeOnAbortOrCancelProcess).isFalse();
    }
    
    @Test
    public void mustExecuteOnAbortOrCancelProcess_return_false_when_is_not_stable() throws Exception {
        //given
        loop.setStable(false);
        
        //when
        boolean executeOnAbortOrCancelProcess = loop.mustExecuteOnAbortOrCancelProcess();
        
        //then
        assertThat(executeOnAbortOrCancelProcess).isFalse();
    }

}
