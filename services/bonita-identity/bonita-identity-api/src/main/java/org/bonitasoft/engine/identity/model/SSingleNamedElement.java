package org.bonitasoft.engine.identity.model;

import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Vincent Elcrin
 */
public interface SSingleNamedElement extends PersistentObject {

    /**
     * Gets the name of the element.
     *
     * @return the element name
     */
    String getName();

    /**
     * Obtains the description of the element
     *
     * @return the element description
     */
    String getDescription();
}
