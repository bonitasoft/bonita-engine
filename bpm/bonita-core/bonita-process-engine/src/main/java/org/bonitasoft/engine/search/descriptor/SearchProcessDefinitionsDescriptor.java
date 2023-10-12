/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.category.model.SProcessCategoryMapping;
import org.bonitasoft.engine.core.category.model.builder.SProcessCategoryMappingBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchProcessDefinitionsDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> processDefDeployInfos;

    public SearchProcessDefinitionsDescriptor() {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(12);
        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class,
                        SProcessDefinitionDeployInfo.ACTIVATION_STATE_KEY));
        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.CONFIGURATION_STATE,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class,
                        SProcessDefinitionDeployInfo.CONFIGURATION_STATE_KEY));
        searchEntityKeys
                .put(ProcessDeploymentInfoSearchDescriptor.ID,
                        new FieldDescriptor(SProcessDefinitionDeployInfo.class, SProcessDefinitionDeployInfo.ID_KEY));
        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.NAME,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, SProcessDefinitionDeployInfo.NAME_KEY));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.VERSION,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, SProcessDefinitionDeployInfo.VERSION_KEY));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.DEPLOYMENT_DATE,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class,
                        SProcessDefinitionDeployInfo.DEPLOYMENT_DATE_KEY));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.DEPLOYED_BY,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, SProcessDefinitionDeployInfo.DEPLOYED_BY_KEY));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.PROCESS_ID,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, SProcessDefinitionDeployInfo.PROCESS_ID_KEY));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, SProcessDefinitionDeployInfo.DISPLAY_NAME_KEY));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.LAST_UPDATE_DATE,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class,
                        SProcessDefinitionDeployInfo.LAST_UPDATE_DATE_KEY));

        final SProcessCategoryMappingBuilderFactory processCategoryMappingBuilderFactory = BuilderFactory
                .get(SProcessCategoryMappingBuilderFactory.class);
        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.CATEGORY_ID,
                new FieldDescriptor(SProcessCategoryMapping.class,
                        processCategoryMappingBuilderFactory.getCategoryIdKey()));

        processDefDeployInfos = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> processFields = new HashSet<String>(3);
        processFields.add(SProcessDefinitionDeployInfo.NAME_KEY);
        processFields.add(SProcessDefinitionDeployInfo.DISPLAY_NAME_KEY);
        processFields.add(SProcessDefinitionDeployInfo.VERSION_KEY);
        processDefDeployInfos.put(SProcessDefinitionDeployInfo.class, processFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return processDefDeployInfos;
    }

}
