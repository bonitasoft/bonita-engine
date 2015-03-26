package org.bonitasoft.engine.sequence;

/**
 * @author Charles Souillard
 */
public class SequenceMapping {

    private final String className;
    private final long sequenceId;


    public SequenceMapping(String className, long sequenceId) {
        this.className = className;
        this.sequenceId = sequenceId;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "SequenceMapping{" +
                "className='" + className + '\'' +
                ", sequenceId=" + sequenceId +
                '}';
    }
}
