package org.bonitasoft.engine.data.instance.api;

import java.util.List;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;

public interface ParentContainerResolver {

    public List<Pair<Long, String>> getContainerHierarchy(final Pair<Long, String> currentContainer) throws SObjectNotFoundException, SObjectReadException;

}
