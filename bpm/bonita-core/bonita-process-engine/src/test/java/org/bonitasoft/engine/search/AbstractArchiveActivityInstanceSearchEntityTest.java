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
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.engine.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SACallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SALoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAUserTaskInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.junit.Test;

/**
 * author Emmanuel Duchastenier
 */
public class AbstractArchiveActivityInstanceSearchEntityTest {

    @Test
    public void getEntityClassShouldHandleAutoTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.AUTOMATIC_TASK);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SAAutomaticTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleManualTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.MANUAL_TASK);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SAManualTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleUserTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.USER_TASK);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SAUserTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleHumanTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.HUMAN_TASK);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SAHumanTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleReceiveTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.RECEIVE_TASK);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SAReceiveTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleSendTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.SEND_TASK);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SASendTaskInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleCallActivityType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.CALL_ACTIVITY);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SACallActivityInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleLoopTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.LOOP_ACTIVITY);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SALoopActivityInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleMultiInstanceTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.MULTI_INSTANCE_ACTIVITY);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SAMultiInstanceActivityInstance.class);
    }

    @Test
    public void getEntityClassShouldHandleSuProcessTaskType() throws Exception {
        final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
        searchOptions.addFilter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.SUB_PROCESS);
        final AbstractArchiveActivityInstanceSearchEntity searcher = new MyAbstractArchiveActivityInstanceSearchEntity(searchOptions);

        assertThat(searcher.getEntityClass()).isEqualTo(SASubProcessActivityInstance.class);
    }

    private static class MyAbstractArchiveActivityInstanceSearchEntity extends AbstractArchiveActivityInstanceSearchEntity {
        public MyAbstractArchiveActivityInstanceSearchEntity(SearchOptionsImpl searchOptions) {
            super(null, searchOptions, null);
        }

        @Override
        public long executeCount(QueryOptions queryOptions) throws SBonitaReadException {
            return 0;
        }

        @Override
        public List<SAActivityInstance> executeSearch(QueryOptions queryOptions) throws SBonitaReadException {
            return null;
        }
    }
}