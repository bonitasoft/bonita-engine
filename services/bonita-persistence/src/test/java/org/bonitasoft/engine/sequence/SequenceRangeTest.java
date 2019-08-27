package org.bonitasoft.engine.sequence;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;

public class SequenceRangeTest {

    @Test
    public void should_give_next_id_in_the_sequence() {
        SequenceRange sequenceRange = new SequenceRange(4);
        sequenceRange.updateToNextRange(10);

        Optional<Long> nextAvailableId = sequenceRange.getNextAvailableId();

        assertThat(nextAvailableId).isPresent().contains(10L);
    }
    @Test
    public void should_not_give_id_if_sequence_is_not_initialized() {
        SequenceRange sequenceRange = new SequenceRange(4);

        Optional<Long> nextAvailableId = sequenceRange.getNextAvailableId();

        assertThat(nextAvailableId).isNotPresent();
    }

    @Test
    public void should_give_all_if_from_range() {
        SequenceRange sequenceRange = new SequenceRange(3);
        sequenceRange.updateToNextRange(1);

        assertThat(sequenceRange.getNextAvailableId()).isPresent().contains(1L);
        assertThat(sequenceRange.getNextAvailableId()).isPresent().contains(2L);
        assertThat(sequenceRange.getNextAvailableId()).isPresent().contains(3L);
        assertThat(sequenceRange.getNextAvailableId()).isNotPresent();
    }


    @Test
    public void should_not_give_id_when_sequence_is_completed() {
        SequenceRange sequenceRange = new SequenceRange(2);
        sequenceRange.updateToNextRange(1);

        assertThat(sequenceRange.getNextAvailableId()).isPresent();
        assertThat(sequenceRange.getNextAvailableId()).isPresent();

        assertThat(sequenceRange.getNextAvailableId()).isNotPresent();
        assertThat(sequenceRange.getNextAvailableId()).isNotPresent();
        assertThat(sequenceRange.getNextAvailableId()).isNotPresent();
    }
}