package org.bonitasoft.engine.sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Charles Souillard
 */
public class SequenceMappingProvider {

    private List<SequenceMapping> sequenceMappings;

    public SequenceMappingProvider() {
    }


    public void setSequenceMappings(final List<SequenceMapping> sequenceMappings) {
        this.sequenceMappings = sequenceMappings;
    }

    public List<SequenceMapping> getSequenceMappings() {
        return sequenceMappings;
    }

    @Override
    public String toString() {
        return "SequenceMappingProvider{" +
                "sequenceMappings=" + sequenceMappings +
                '}';
    }
}
