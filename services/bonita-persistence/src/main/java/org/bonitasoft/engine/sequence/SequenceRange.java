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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceRange {

    private AtomicLong nextAvailableId;
    private long lastIdInRange;
    private final int rangeSize;

    public SequenceRange(int rangeSize) {
        this.rangeSize = rangeSize;
    }

    public Optional<Long> getNextAvailableId() {
        if (nextAvailableId == null) {
            // Range is not initialized yet:
            return Optional.empty();
        }
        long nextId = nextAvailableId.getAndUpdate(current -> {
            if (current == -1 || current >= lastIdInRange) {
                return -1; // -1 means no more Id available
            } else {
                return current + 1;
            }
        });
        if (nextId < 0) {
            return Optional.empty();
        }
        return Optional.of(nextId);
    }

    public void updateToNextRange(long nextAvailableIdFromDatabase) {
        nextAvailableId = new AtomicLong(nextAvailableIdFromDatabase);
        lastIdInRange = nextAvailableIdFromDatabase + rangeSize - 1;
    }
}
