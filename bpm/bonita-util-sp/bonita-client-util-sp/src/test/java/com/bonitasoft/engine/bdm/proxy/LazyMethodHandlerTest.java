package com.bonitasoft.engine.bdm.proxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LazyMethodHandlerTest {

    @Mock
    private LazyLoader lazyLoader;

    @InjectMocks
    private LazyMethodHandler methodHandler;

    @Test
    public void should_lazy_load_object_when_method_is_lazy_and_object_is_not_loaded() throws Exception {
        //        methodHandler.invoke(self, thisMethod, proceed, args)
    }
}
