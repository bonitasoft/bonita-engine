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
package org.bonitasoft.engine.bpm;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class EventInstanceRepositoryIT extends CommonBPMServicesTest {

    private EventInstanceRepository eventInstanceRepository;
    private TransactionService transactionService;
    private final long oneMinuteAgo = Instant.now().minusSeconds(60).toEpochMilli();

    @Before
    public void before() throws Exception {
        eventInstanceRepository = getTenantAccessor().getEventInstanceRepository();
        transactionService = getTransactionService();

        transactionService.executeInTransaction(() -> {
            List<Long> messageInstanceIdOlderThanCreationDate = eventInstanceRepository
                    .getMessageInstanceIdOlderThanCreationDate(System.currentTimeMillis(), new QueryOptions(0, 1000));
            System.out.println("ids" + messageInstanceIdOlderThanCreationDate);
            eventInstanceRepository.deleteMessageInstanceByIds(messageInstanceIdOlderThanCreationDate);
            return null;
        });
    }

    @Test
    public void should_create_message_with_a_creation_date() throws Exception {
        SMessageInstance sMessageInstance = createMessageInstance("myMessage");

        SMessageInstance createdMessage = transactionService.executeInTransaction(() -> {
            eventInstanceRepository.createMessageInstance(sMessageInstance);
            return eventInstanceRepository.getMessageInstance(sMessageInstance.getId());
        });

        assertThat(createdMessage.getId()).isGreaterThan(0);
        assertThat(createdMessage.getCreationDate()).isGreaterThan(0);

    }

    private SMessageInstance createMessageInstance(String myMessage) {
        return new SMessageInstance(myMessage, "targetProcess", "targetFlowNode", 12345L, "fnName");
    }

    @Test
    public void should_get_older_message_with_creationDate_and_no_filters() throws Exception {
        SMessageInstance myMessageNow = createMessageInstance("myMessage");
        SMessageInstance myMessageOld2 = createMessageInstance("myMessage2");
        SMessageInstance myMessageOld = createMessageInstance("myMessage");
        myMessageOld2.setCreationDate(oneMinuteAgo);
        myMessageOld.setCreationDate(oneMinuteAgo);

        List<Long> ids = transactionService.executeInTransaction(() -> {
            eventInstanceRepository.createMessageInstance(myMessageNow);
            eventInstanceRepository.createMessageInstance(myMessageOld);
            eventInstanceRepository.createMessageInstance(myMessageOld2);
            return eventInstanceRepository.getMessageInstanceIdOlderThanCreationDate(oneMinuteAgo, null);

        });
        assertThat(ids).containsExactly(myMessageOld.getId(), myMessageOld2.getId());

    }

    @Test
    public void should_get_older_message_with_creationDate_messageName_filter() throws Exception {
        SMessageInstance myMessageNow = createMessageInstance("myMessage");
        SMessageInstance myMessageOld2 = createMessageInstance("myMessage2");
        SMessageInstance myMessageOld = createMessageInstance("myMessage");
        myMessageOld2.setCreationDate(oneMinuteAgo);
        myMessageOld.setCreationDate(oneMinuteAgo);

        List<Long> ids = transactionService.executeInTransaction(() -> {
            QueryOptions queryOptions = new QueryOptions(0, 100, emptyList(),
                    singletonList(new FilterOption(SMessageInstance.class, "messageName", "myMessage")), null);

            eventInstanceRepository.createMessageInstance(myMessageNow);
            eventInstanceRepository.createMessageInstance(myMessageOld);
            eventInstanceRepository.createMessageInstance(myMessageOld2);
            return eventInstanceRepository.getMessageInstanceIdOlderThanCreationDate(oneMinuteAgo, queryOptions);

        });
        assertThat(ids).containsExactly(myMessageOld.getId());

    }

    @Test
    public void should_delete_a_message_with_given_id() throws Exception {

        SMessageInstance sMessageInstance = createMessageInstance("myMessage");
        SMessageInstance sMessageInstance2 = createMessageInstance("myMessage2");
        transactionService.executeInTransaction(() -> {
            eventInstanceRepository.createMessageInstance(sMessageInstance);
            eventInstanceRepository.createMessageInstance(sMessageInstance2);
            return null;

        });

        transactionService.executeInTransaction(() -> {
            eventInstanceRepository.deleteMessageInstanceByIds(singletonList(sMessageInstance.getId()));
            return null;
        });

        assertThat(getMessageInstance(sMessageInstance.getId())).isNull();
        assertThat(getMessageInstance(sMessageInstance2.getId())).isNotNull();

    }

    private SMessageInstance getMessageInstance(long id) throws Exception {
        return transactionService.executeInTransaction(() -> eventInstanceRepository.getMessageInstance(id));
    }

}
