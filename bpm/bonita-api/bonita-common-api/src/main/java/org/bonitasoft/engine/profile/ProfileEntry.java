/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * A <code>ProfileEntry</code> represents a menu entry (inside the main top menu bar) in the Bonita BPM Portal. It can be a container (that contains other
 * profile entries) or a link to a page in the portal. <code>ProfileEntry</code>s are ordered inside a containing profile entry or at the root level.
 * 
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @author Julien Mege
 */
public interface ProfileEntry extends NamedElement, BaseElement {

    /**
     * @return the ID of the parent ProfileEntry.
     */
    long getParentId();

    /**
     * @return the ID of the profile that this profile entry belongs to.
     */
    long getProfileId();

    /**
     * The order of the profile entry in its context (at root level or inside its parent profile entry).
     * 
     * @return the index of the profile entry.
     */
    long getIndex();

    /**
     * @return the description of the <code>ProfileEntry</code>, that can be used to describe the link of containing folder it represents, according to its
     *         type.
     */
    String getDescription();

    /**
     * @return the type of the profile entry. It can be container (folder), or link to a real page.
     */
    String getType();

    /**
     * @return a unique reference to a page in the portal.
     */
    String getPage();

    /**
     * @return true if this entry reference a custom page
     */
    boolean isCustom();

}
