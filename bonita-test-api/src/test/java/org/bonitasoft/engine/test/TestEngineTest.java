package org.bonitasoft.engine.test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEngineTest {



    @Spy
    private TestEngine testEngine;

    @Test
    public void should_start_do_nothing_second_time() throws Exception {
        //given
        doNothing().when(testEngine).doStart();
        //when
        testEngine.start();
        testEngine.start();

        //then
        verify(testEngine,times(1)).doStart();
    }


}