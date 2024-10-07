/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.engine.gradle.docker

import org.gradle.api.Action

class DatabasePluginExtension {

    /**
     * Include test class patterns applied to database vendors
     */
    List<String> includes
    /**
     * Exclude test class patterns applied to all database vendors
     */
    List<String> excludes
    /**
     * Extra configuration for the postgres database
     */
    DatabaseExtraConfiguration postgres = new DatabaseExtraConfiguration(enabled: true)
    /**
     * Extra configuration for the mysql database
     */
    DatabaseExtraConfiguration mysql = new DatabaseExtraConfiguration()
    /**
     * Extra configuration for the oracle database
     */
    DatabaseExtraConfiguration oracle = new DatabaseExtraConfiguration()
    /**
     * Extra configuration for the sqlserver database
     */
    DatabaseExtraConfiguration sqlserver = new DatabaseExtraConfiguration()

    def includes(String... includes) {
        this.includes = []
        this.includes.addAll(includes)
    }

    def include(String include) {
        if (this.includes == null) {
            this.includes = []
        }
        this.includes.add(include)
    }

    def excludes(String... excludes) {
        this.excludes = []
        this.excludes.addAll(excludes)
    }

    def exclude(String exclude) {
        if (this.excludes == null) {
            this.excludes = []
        }
        this.excludes.add(exclude)
    }

    def postgres(Action<DatabaseExtraConfiguration> action) {
        action.execute(postgres)
    }

    def mysql(Action<DatabaseExtraConfiguration> action) {
        action.execute(mysql)
    }

    def oracle(Action<DatabaseExtraConfiguration> action) {
        action.execute(oracle)
    }

    def sqlserver(Action<DatabaseExtraConfiguration> action) {
        action.execute(sqlserver)
    }

}
