package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.StatelessJob;

/**
 * @author Elias Ricken de Medeiros
 */
public class DoNothingJob implements StatelessJob {

    private static final long serialVersionUID = 5253574298401130601L;

    @Override
    public String getName() {
        return "doNothing";
    }

    @Override
    public String getDescription() {
        return "DoNothing";
    }

    @Override
    public void execute() {
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
    }

}
