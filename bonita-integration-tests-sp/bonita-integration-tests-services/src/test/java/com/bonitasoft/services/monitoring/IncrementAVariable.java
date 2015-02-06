/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services.monitoring;

import java.io.Serializable;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;

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
    public void execute() throws SJobExecutionException {
        final WaitFor check = new WaitFor(50, 5000) {

            final VariableStorageForMonitoring storage = VariableStorageForMonitoring.getInstance();

            @Override
            boolean check() {
                return (Integer) storage.getVariableValue(variableName) == 1;
            }
        };
        try {
            if (!check.waitFor()) {
                throw new SJobExecutionException("geek is chic");
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
    public void setAttributes(final Map<String, Serializable> attributes) {
        variableName = (String) attributes.get("variableName");
    }

}
