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
package org.bonitasoft.engine.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class DBConfiguration {

    private final String createTablesFile;

    private final String initTablesFile;

    private final String cleanTablesFile;

    private final String dropTablesFile;

    private final List<String> filter;

    private final String deleteTenantObjectsFile;

    private final String postCreateStructureFile;

    private final String preDropStructureFile;

    private Map<String, SQLTransformer> sqlTransformers;

    private int deleteTenantObjectsPriority;

    /**
     * @param createTablesFile
     * @param dropTablesFile
     * @param initTablesFile
     * @param filter
     *            name of persistence service that the configuration is relevant to
     */
    public DBConfiguration(final String createTablesFile, final String dropTablesFile, final String initTablesFile, final String cleanTablesFile,
            final String filter) {
        this.createTablesFile = createTablesFile;
        this.dropTablesFile = dropTablesFile;
        this.initTablesFile = initTablesFile;
        this.cleanTablesFile = cleanTablesFile;
        this.filter = Arrays.asList(filter);
        deleteTenantObjectsFile = null;
        postCreateStructureFile = null;
        preDropStructureFile = null;
    }

    /**
     * @param createTablesFile
     * @param dropTablesFile
     * @param initTablesFile
     * @param filter
     *            name of persistence service that the configuration is relevant to
     * @param postCreateStructureFile
     *            SQL script that creates foreign keys
     * @param preDropStructureFile
     *            SQL script that drops foreign keys
     */
    public DBConfiguration(final String createTablesFile, final String dropTablesFile, final String initTablesFile, final String cleanTablesFile,
            final String filter, final String postCreateStructureFile, final String preDropStructureFile) {
        this.createTablesFile = createTablesFile;
        this.dropTablesFile = dropTablesFile;
        this.initTablesFile = initTablesFile;
        this.cleanTablesFile = cleanTablesFile;
        this.filter = Arrays.asList(filter);
        deleteTenantObjectsFile = null;
        this.postCreateStructureFile = postCreateStructureFile;
        this.preDropStructureFile = preDropStructureFile;
    }

    /**
     * @param createTablesFile
     * @param dropTablesFile
     * @param initTablesFile
     * @param filter
     *            name of persistence service that the configuration is relevant to
     * @param deleteTenantObjectsFile
     *            SQL script that clean objects of the tenant
     * @param deleteTenantObjectsPriority
     *            Priority order to execute deleteTenantObjectsFile
     * @param postCreateStructureFile
     *            SQL script that creates foreign keys
     * @param preDropStructureFile
     *            SQL script that drops foreign keys
     */
    public DBConfiguration(final String createTablesFile, final String dropTablesFile, final String initTablesFile, final String cleanTablesFile,
            final String filter, final String deleteTenantObjectsFile, final int deleteTenantObjectsPriority, final String postCreateStructureFile,
            final String preDropStructureFile) {
        this.createTablesFile = createTablesFile;
        this.dropTablesFile = dropTablesFile;
        this.initTablesFile = initTablesFile;
        this.cleanTablesFile = cleanTablesFile;
        this.filter = Arrays.asList(filter);
        this.deleteTenantObjectsFile = deleteTenantObjectsFile;
        this.postCreateStructureFile = postCreateStructureFile;
        this.preDropStructureFile = preDropStructureFile;
        this.deleteTenantObjectsPriority = deleteTenantObjectsPriority;
    }

    /**
     * @param createTablesFile
     * @param dropTablesFile
     * @param initTablesFile
     * @param filter
     *            name of persistence service that the configuration is relevant to
     * @param deleteTenantObjectsFile
     *            SQL script that clean objects of the tenant
     * @param deleteTenantObjectsPriority
     *            Priority order to execute deleteTenantObjectsFile
     */
    public DBConfiguration(final String createTablesFile, final String dropTablesFile, final String initTablesFile, final String cleanTablesFile,
            final String filter, final String deleteTenantObjectsFile, final int deleteTenantObjectsPriority) {
        this.createTablesFile = createTablesFile;
        this.dropTablesFile = dropTablesFile;
        this.initTablesFile = initTablesFile;
        this.cleanTablesFile = cleanTablesFile;
        this.filter = Arrays.asList(filter);
        this.deleteTenantObjectsFile = deleteTenantObjectsFile;
        postCreateStructureFile = null;
        preDropStructureFile = null;
        this.deleteTenantObjectsPriority = deleteTenantObjectsPriority;
    }

    /**
     * @param createTablesFile
     * @param dropTablesFile
     * @param initTablesFile
     * @param filter
     *            name of persistence service that the configuration is relevant to
     * @param deleteTenantObjectsFile
     *            SQL script that clean objects of the tenant
     */
    public DBConfiguration(final String postCreateStructureFile, final String preDropStructureFile, final String filter) {
        createTablesFile = null;
        dropTablesFile = null;
        initTablesFile = null;
        cleanTablesFile = null;
        this.filter = Arrays.asList(filter);
        deleteTenantObjectsFile = null;
        this.postCreateStructureFile = postCreateStructureFile;
        this.preDropStructureFile = preDropStructureFile;
    }

    public String getCreateTablesFile() {
        return createTablesFile;
    }

    public String getDropTablesFile() {
        return dropTablesFile;
    }

    public String getInitTablesFile() {
        return initTablesFile;
    }

    public String getCleanTablesFile() {
        return cleanTablesFile;
    }

    public String getDeleteTenantObjectsFile() {
        return deleteTenantObjectsFile;
    }

    public int getDeleteTenantObjectsPriority() {
        return deleteTenantObjectsPriority;
    }

    public void setDeleteTenantObjectsPriority(final int deleteTenantObjectsPriority) {
        this.deleteTenantObjectsPriority = deleteTenantObjectsPriority;
    }

    protected List<String> getFilter() {
        return filter;
    }

    public boolean hasCreateTablesFile() {
        return createTablesFile != null && !createTablesFile.isEmpty();
    }

    public boolean hasDropTablesFile() {
        return dropTablesFile != null && !dropTablesFile.isEmpty();
    }

    public boolean hasInitTablesFile() {
        return initTablesFile != null && !initTablesFile.isEmpty();
    }

    public boolean hasCleanTablesFile() {
        return !(cleanTablesFile == null || cleanTablesFile.isEmpty());
    }

    public boolean matchesFilter(final String persistenceServiceName) {
        return filter == null ? persistenceServiceName == null : filter.contains(persistenceServiceName);
    }

    public boolean hasDeleteTenantObjectsFile() {
        return deleteTenantObjectsFile != null && !deleteTenantObjectsFile.isEmpty();
    }

    public String getPostCreateStructureFile() {
        return postCreateStructureFile;
    }

    public boolean hasPostCreateStructureFile() {
        return postCreateStructureFile != null && !postCreateStructureFile.isEmpty();
    }

    public String getPreDropStructureFile() {
        return preDropStructureFile;
    }

    public boolean hasPreDropStructureFile() {
        return preDropStructureFile != null && !preDropStructureFile.isEmpty();
    }

    public Map<String, SQLTransformer> getSqlTransformers() {
        return sqlTransformers;
    }

    public void setSqlTransformers(final Map<String, SQLTransformer> sqlTransformers) {
        this.sqlTransformers = sqlTransformers;
    }

    public boolean hasSqlTransformers() {
        return sqlTransformers != null && !sqlTransformers.isEmpty();
    }

}
