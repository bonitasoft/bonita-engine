package com.bonitasoft.engine.log.api.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;



public class BatchLogBufferTest {
    
    @Mock
    private SQueriableLog log1;

    @Mock
    private SQueriableLog log2;
    
    @Before
    public void setUp() throws Exception {
        BatchLogBuffer.getInstance().clearLogs();
    }
    
    @Test
    public void clearLogs_without_adding_logs_should_return_empty_list() throws Exception {
        //given
        BatchLogBuffer logBuffer = BatchLogBuffer.getInstance();

        //when
        List<SQueriableLog> logs = logBuffer.clearLogs();

        //then
        assertThat(logs).isEmpty();
    }

    @Test
    public void clearLogs_should_return_previous_added_items() throws Exception {
        //given
        BatchLogBuffer logBuffer = BatchLogBuffer.getInstance();
        List<SQueriableLog> addedLogs = Arrays.asList(log1, log2);
        logBuffer.addLogs(addedLogs);
        
        //when
        List<SQueriableLog> logs = logBuffer.clearLogs();
        
        //then
        assertThat(logs).isEqualTo(addedLogs);
    }

    @Test
    public void second_call_to_clearLogs_without_adding_logs_should_return_return_empty_list() throws Exception {
        //given
        BatchLogBuffer logBuffer = BatchLogBuffer.getInstance();
        List<SQueriableLog> addedLogs = Arrays.asList(log1, log2);
        logBuffer.addLogs(addedLogs);
        
        //when
        logBuffer.clearLogs();
        List<SQueriableLog> twice = logBuffer.clearLogs();
        
        //then
        assertThat(twice).isEmpty();
    }

}
