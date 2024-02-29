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
