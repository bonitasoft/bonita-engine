package org.bonitasoft.engine.sequence;

import java.util.Collections;
import java.util.Set;

/**
 * @author Charles Souillard
 */
public class SequenceMapping {

    private final Set<String> classNames;
    private final long sequenceId;
    private final int rangeSize;

    public SequenceMapping(String className, long sequenceId, final int rangeSize) {
        this(Collections.singleton(className), sequenceId, rangeSize);
    }

    public SequenceMapping(Set<String> classNames, long sequenceId, final int rangeSize) {
        this.classNames = classNames;
        this.sequenceId = sequenceId;
        this.rangeSize = rangeSize;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public Set<String> getClassNames() {
        return classNames;
    }

    public int getRangeSize() {
        return rangeSize;
    }

    @Override
    public String toString() {
        return "SequenceMapping{" +
                "sequenceId=" + sequenceId +
                ", classNames='" + classNames + '\'' +
                ", rangeSize=" + rangeSize +
                '}';
    }
}
