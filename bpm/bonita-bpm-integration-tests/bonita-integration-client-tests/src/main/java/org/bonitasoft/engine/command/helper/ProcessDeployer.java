package org.bonitasoft.engine.command.helper;

import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.command.helper.designer.SimpleProcessDesigner;
import org.bonitasoft.engine.exception.BonitaException;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 11:40
 */
public abstract class ProcessDeployer {

    ProcessDefinition processDefinition;

    public ProcessDefinition deploy(SimpleProcessDesigner design) throws BonitaException {
        return processDefinition = deploy(design.done());
    }

    public abstract ProcessDefinition deploy(DesignProcessDefinition design) throws BonitaException;

    public void clean() throws BonitaException {
        clean(processDefinition);
    }

    public abstract void clean(ProcessDefinition processDefinition) throws BonitaException;
}
