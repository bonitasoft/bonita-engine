/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.instance.model.builder.impl;

import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceLogBuilder;
import org.bonitasoft.engine.data.instance.model.archive.builder.impl.SADataInstanceBuilderImpl;
import org.bonitasoft.engine.data.instance.model.archive.builder.impl.SADataInstanceLogBuilderImpl;
import org.bonitasoft.engine.data.instance.model.archive.builder.impl.SADataInstanceVisibilityMappingBuilderImpl;
import org.bonitasoft.engine.data.instance.model.builder.SADataInstanceVisibilityMappingBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceLogBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceVisibilityMappingBuilder;
import org.bonitasoft.engine.data.instance.model.impl.SDataInstanceVisibilityMappingBuilderImpl;

/**
 * @author Matthieu Chaffotte
 */
public class SDataInstanceBuildersImpl implements SDataInstanceBuilders {

    public SDataInstanceBuildersImpl() {
        super();
    }

    @Override
    public SDataInstanceBuilder getDataInstanceBuilder() {
        return new SDataInstanceBuilderImpl();
    }

    @Override
    public SDataInstanceLogBuilder getDataInstanceLogBuilder() {
        return new SDataInstanceLogBuilderImpl();
    }

    @Override
    public SADataInstanceBuilder getSADataInstanceBuilder() {
        return new SADataInstanceBuilderImpl();
    }

    @Override
    public SDataInstanceVisibilityMappingBuilder getDataInstanceVisibilityMappingBuilder() {
        return new SDataInstanceVisibilityMappingBuilderImpl();
    }

    @Override
    public SADataInstanceLogBuilder getSADataInstanceLogBuilder() {
        return new SADataInstanceLogBuilderImpl();
    }

    @Override
    public SADataInstanceVisibilityMappingBuilder getArchivedDataInstanceVisibilityMappingBuilder() {
        return new SADataInstanceVisibilityMappingBuilderImpl();
    }

}
