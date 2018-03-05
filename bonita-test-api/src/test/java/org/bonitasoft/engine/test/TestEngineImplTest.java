package org.bonitasoft.engine.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.test.internal.EngineCommander;
import org.bonitasoft.engine.test.internal.EngineStarter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEngineImplTest {

    @Mock
    private EngineStarter engineStarter;
    @Mock
    private EngineCommander engineCommander;
    @InjectMocks
    private TestEngineImpl testEngine;

    @Test
    public void should_start_do_nothing_second_time() throws Exception {
        //when
        testEngine.start();
        testEngine.start();

        //then
        verify(engineStarter,times(1)).start();
    }


}
