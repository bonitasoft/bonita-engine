package org.bonitasoft.engine.sequence;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceRange {

    private AtomicLong nextAvailableId;
    private long lastIdInRange;
    private int rangeSize;

    public SequenceRange(int rangeSize) {
        this.rangeSize = rangeSize;
    }

    public Optional<Long> getNextAvailableId() {
        if (nextAvailableId == null) {
            return Optional.empty();
        }
        long nextId = nextAvailableId.getAndUpdate(current -> {
            if (current == -1 || current >= lastIdInRange) {
                return -1;
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
