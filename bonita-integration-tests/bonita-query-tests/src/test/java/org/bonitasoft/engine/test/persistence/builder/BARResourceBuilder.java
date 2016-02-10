/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.SBARResource;

public class BARResourceBuilder extends PersistentObjectBuilder<SBARResource, BARResourceBuilder> {

    private String name;

    private Long processDefinitionId;

    private byte[] content;
    private BARResourceType type;

    public static BARResourceBuilder aBARResource() {
        return new BARResourceBuilder();
    }

    @Override
    public SBARResource _build() {
        return new SBARResource(name, type, processDefinitionId, content);
    }

    public BARResourceBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public BARResourceBuilder withType(final BARResourceType type) {
        this.type = type;
        return this;
    }

    public BARResourceBuilder withContent(final byte[] content) {
        this.content = content;
        return this;
    }

    public BARResourceBuilder withProcessDefinitionId(final Long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    @Override
    BARResourceBuilder getThisBuilder() {
        return this;
    }

}
