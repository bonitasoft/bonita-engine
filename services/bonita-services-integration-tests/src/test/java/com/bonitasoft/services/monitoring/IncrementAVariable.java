package com.bonitasoft.services.monitoring;

import java.io.Serializable;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.bonitasoft.engine.scheduler.JobExecutionException;
import org.bonitasoft.engine.scheduler.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.StatelessJob;

public class IncrementAVariable implements StatelessJob {

    private static final long serialVersionUID = 5253574298401130601L;

    private String variableName;

    @Override
    public String getName() {
        return "increment";
    }

    @Override
    public String getDescription() {
        return "increment a variable";
    }

    @Override
    public void execute() throws JobExecutionException {
        final WaitFor check = new WaitFor(50, 5000) {

            final VariableStorageForMonitoring storage = VariableStorageForMonitoring.getInstance();

            @Override
            boolean check() {
                return (Integer) storage.getVariableValue(variableName) == 1;
            }
        };
        try {
            if (!check.waitFor()) {
                throw new JobExecutionException("geek is chic");
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (final InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (final MBeanException e) {
            e.printStackTrace();
        } catch (final ReflectionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        variableName = (String) attributes.get("variableName");
    }

    @Override
    public boolean isWrappedInTransaction() {
        return true;
    }

}
