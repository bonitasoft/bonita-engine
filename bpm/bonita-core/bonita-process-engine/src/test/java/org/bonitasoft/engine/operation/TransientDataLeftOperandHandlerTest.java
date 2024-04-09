/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.data.instance.model.SShortTextDataInstance;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransientDataLeftOperandHandlerTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    @Mock
    private TransientDataService transientDataService;

    @InjectMocks
    private TransientDataLeftOperandHandler transientDataLeftOperandHandler;

    @Test
    public void should_update_call_transient_data_service() throws Exception {
        // given
        final SShortTextDataInstance data = createData();
        when(transientDataService.getDataInstance("myData", 42, "ctype")).thenReturn(data);
        systemOutRule.clearLog();
        // when
        transientDataLeftOperandHandler.update(createLeftOperand("myData"), Collections.<String, Object> emptyMap(),
                "new Value", 42, "ctype");

        // then
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField("value", "new Value");
        verify(transientDataService, times(1)).updateDataInstance(eq(data), eq(entityUpdateDescriptor));
        assertThat(systemOutRule.getLog()).containsPattern("WARN.*");
    }

    private SLeftOperandImpl createLeftOperand(final String name) {
        final SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(name);
        return sLeftOperand;
    }

    @Test
    public void should_retrieve_get_data_from_transient_service() throws Exception {
        // given
        final SShortTextDataInstance data = createData();
        when(transientDataService.getDataInstance("myData", 42, "ctype")).thenReturn(data);
        SExpressionContext sExpressionContext = new SExpressionContext(42l, "ctype", 12l);
        // when
        transientDataLeftOperandHandler.loadLeftOperandInContext(createLeftOperand("myData"),
                sExpressionContext.getContainerId(), sExpressionContext.getContainerType(), sExpressionContext);

        // then
        assertThat(sExpressionContext.getInputValues()).containsOnly(entry("myData", data.getValue()),
                entry("%TRANSIENT_DATA%_myData", data));
    }

    private SShortTextDataInstance createData() {
        final SShortTextDataInstance data = new SShortTextDataInstance();
        data.setName("myData");
        data.setId(56);
        data.setValue("The data value");
        return data;
    }

    @Test(expected = SOperationExecutionException.class)
    public void deleteThrowsAnExceptionNotYetSupported() throws Exception {
        transientDataLeftOperandHandler.delete(createLeftOperand("myData"), 45l, "container");
    }

}
