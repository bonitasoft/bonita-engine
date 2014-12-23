package org.bonitasoft.engine.data.instance.api;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;

public interface ParentContainerResolver {

    public Pair<Long, String> getParentContainer(final Pair<Long, String> container) throws SObjectNotFoundException, SObjectReadException;

}
