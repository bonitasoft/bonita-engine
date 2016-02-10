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
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoBuilderFactory;
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
        final SProcessDefinitionDeployInfoBuilderFactory definitionDeployInfo = BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class);
        searchEntityKeys = new HashMap<String, FieldDescriptor>(12);
        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, new FieldDescriptor(SProcessDefinitionDeployInfo.class,
                definitionDeployInfo.getActivationStateKey()));
        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.CONFIGURATION_STATE, new FieldDescriptor(SProcessDefinitionDeployInfo.class,
                definitionDeployInfo.getConfigurationStateKey()));
        searchEntityKeys
                .put(ProcessDeploymentInfoSearchDescriptor.ID, new FieldDescriptor(SProcessDefinitionDeployInfo.class, definitionDeployInfo.getIdKey()));
        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.NAME,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, definitionDeployInfo.getNameKey()));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.VERSION,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, definitionDeployInfo.getVersionKey()));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.DEPLOYMENT_DATE, new FieldDescriptor(SProcessDefinitionDeployInfo.class,
                definitionDeployInfo.getDeploymentDateKey()));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.DEPLOYED_BY,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, definitionDeployInfo.getDeployedByKey()));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.LABEL, new FieldDescriptor(SProcessDefinitionDeployInfo.class,
                definitionDeployInfo.getLabelStateKey()));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.PROCESS_ID,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, definitionDeployInfo.getProcessIdKey()));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SProcessDefinitionDeployInfo.class, definitionDeployInfo.getDisplayNameKey()));

        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.LAST_UPDATE_DATE, new FieldDescriptor(SProcessDefinitionDeployInfo.class,
                definitionDeployInfo.getLastUpdateDateKey()));

        final SProcessCategoryMappingBuilderFactory processCategoryMappingBuilderFactory = BuilderFactory.get(SProcessCategoryMappingBuilderFactory.class);
        searchEntityKeys.put(ProcessDeploymentInfoSearchDescriptor.CATEGORY_ID, new FieldDescriptor(SProcessCategoryMapping.class,
                processCategoryMappingBuilderFactory.getCategoryIdKey()));

        processDefDeployInfos = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> processFields = new HashSet<String>(3);
        processFields.add(definitionDeployInfo.getNameKey());
        processFields.add(definitionDeployInfo.getDisplayNameKey());
        processFields.add(definitionDeployInfo.getVersionKey());
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
