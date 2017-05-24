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
import org.bonitasoft.engine.page.Page;

/**
 * A <code>ProfileEntry</code> represents a menu entry (inside the main top menu bar) in the Bonita Portal. It can be a container (that contains other
 * profile entries) or a link to a page in the portal. <code>ProfileEntry</code>s are ordered inside a containing profile entry or at the root level.
 * 
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @author Julien Mege
 * @author Anthony Birembaut
 */
public interface ProfileEntry extends NamedElement, BaseElement {
	
	/**
	 * Constant for "folder" type of profile entries (used to create a group of menu link)
	 */
	static String FOLDER_TYPE = "folder";
	
	/**
	 * Constant for "link" type of profile entries (used to create a link to a page in the menu)
	 */
	static String LINK_TYPE = "link";

    /**
     * Retrieves the identifier of the parent {@code ProfileEntry}.
     * 
     * @return the identifier of the parent {@code ProfileEntry}.
     */
    long getParentId();

    /**
     * Retrieves the identifier of the related {@link Profile}
     * 
     * @return the identifier of the related {@code Profile}
     */
    long getProfileId();

    /**
     * Retrieves the order of the profile entry in its context (at root level or inside its parent profile entry).
     * 
     * @return the index of the profile entry.
     */
    long getIndex();

    /**
     * Retrieves the {@code ProfileEntry} description, that can be used to describe the link of the containing folder it represents, according to its type.
     * 
     * @return the {@code ProfileEntry} description
     */
    String getDescription();

    /**
     * Retrieves the {@code ProfileEntry} type. It can be container (folder), or link to a real page.
     * @return  the {@code ProfileEntry} type
     */
    String getType();

    /**
     * Retrieves the unique name of the page referenced by this {@code ProfileEntry}. It can be a portal page or a custom {@link org.bonitasoft.engine.page.Page}.
     * @return the unique name of the page referenced by this {@code ProfileEntry}
     * @see Page#getName()
     */
    String getPage();

    /**
     * Determines if the referenced page is a custom {@link Page}.
     * @return true if this {@code ProfileEntry} references a custom {@link Page}; false if it references a portal page.
     * @see Page
     */
    boolean isCustom();

}
