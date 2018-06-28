/*
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, US.
 */
package org.bonitasoft.engine.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * author Emmanuel Duchastenier
 */
public class AbstractActivityInstanceSearchEntityTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getEntityClassShouldHandleAutoTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.AUTOMATIC_TASK);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SAutomaticTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleManualTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.MANUAL_TASK);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SManualTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleUserTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.USER_TASK);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SUserTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleHumanTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.HUMAN_TASK);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SHumanTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleReceiveTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.RECEIVE_TASK);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SReceiveTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleSendTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.SEND_TASK);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SSendTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleCallActivityType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.CALL_ACTIVITY);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SCallActivityInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleLoopTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.LOOP_ACTIVITY);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SLoopActivityInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleMultiInstanceTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.MULTI_INSTANCE_ACTIVITY);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SMultiInstanceActivityInstance.class);
    }

    @Test
    public void getEntityClassShouldThrowExceptionOnUnknownType() throws Exception {
        //given
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.START_EVENT);
        expectedException.expect(SBonitaReadException.class);
        //when
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);
        //then exception
    }

    @Test
    public void getEntityClassShouldHandleSuProcessTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.SUB_PROCESS);
        final AbstractActivityInstanceSearchEntity searcher = new MyAbstractActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SSubProcessActivityInstance.class);
    }

    private static class MyAbstractActivityInstanceSearchEntity extends AbstractActivityInstanceSearchEntity {
        public MyAbstractActivityInstanceSearchEntity(SearchOptionsImpl searchOptions) throws SBonitaReadException {
            super(null, searchOptions, null);
        }

        @Override
        public long executeCount(QueryOptions queryOptions) throws SBonitaReadException {
            return 0;
        }

        @Override
        public List<SActivityInstance> executeSearch(QueryOptions queryOptions) throws SBonitaReadException {
            return null;
        }
    }
}
