package org.bonitasoft.engine.bpm;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import java.time.Instant;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class EventInstanceRepositoryIT extends CommonBPMServicesTest {

    private EventInstanceRepository eventInstanceRepository;
    private TransactionService transactionService;
    private long oneMinuteAgo = Instant.now().minusSeconds(60).toEpochMilli();


    @Before
    public void before() throws Exception {
        eventInstanceRepository = getTenantAccessor().getEventInstanceRepository();
        transactionService = getTransactionService();

        transactionService.executeInTransaction(() -> {
            List<Long> messageInstanceIdOlderThanCreationDate = eventInstanceRepository.getMessageInstanceIdOlderThanCreationDate(System.currentTimeMillis(), new QueryOptions(0, 1000));
            System.out.println("ids" + messageInstanceIdOlderThanCreationDate);
            return eventInstanceRepository.deleteMessageInstanceByIds(messageInstanceIdOlderThanCreationDate);
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
    public void should_get_older_message_with_creationDate_messageName_filter() throws Exception {
        SMessageInstance myMessageNow = createMessageInstance("myMessage");
        SMessageInstance myMessageOld2 = createMessageInstance("myMessage2");
        SMessageInstance myMessageOld = createMessageInstance("myMessage");
        myMessageOld2.setCreationDate(oneMinuteAgo);
        myMessageOld.setCreationDate(oneMinuteAgo);


        List<Long> ids = transactionService.executeInTransaction(() -> {
            QueryOptions queryOptions = new QueryOptions(0, 100, emptyList(), singletonList(new FilterOption(SMessageInstance.class, "messageName", "myMessage")), null);

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


    @Test
    public void should_get_older_message_without_filters() throws Exception {

        SMessageInstance oldestMessage = createMessageInstance("myMessage");
        long oneMinuteAgo = Instant.now().minusSeconds(60).toEpochMilli();
        oldestMessage.setCreationDate(oneMinuteAgo);

        SMessageInstance recentMessage = createMessageInstance("myMessage2");

        List<Long> ids = transactionService.executeInTransaction(() -> {
            eventInstanceRepository.createMessageInstance(oldestMessage);
            eventInstanceRepository.createMessageInstance(recentMessage);
            return eventInstanceRepository.getMessageInstanceIdOlderThanCreationDate(oneMinuteAgo, new QueryOptions(0, 100, SMessageInstance.class, "id", OrderByType.ASC));

        });
        assertThat(ids).containsExactly(oldestMessage.getId());

    }


}
