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
package org.bonitasoft.engine.archive.model;

import org.bonitasoft.engine.persistence.PersistentObject;

public class Employee extends SPersistentObjectImpl {

    private static final long serialVersionUID = 1L;

    private String name;

    private int age;

    private long laptopId; // The relationship between Employee and Laptop is
                           // one-to-one.

    public Employee(final String name, final int age) {
        this.name = name;
        this.age = age;
    }

    public Employee(final Employee employee) {
        setName(employee.getName());
        setAge(employee.getAge());
        setLaptopId(employee.getLaptopId());
        setId(employee.getId());
        setTenantId(employee.getTenantId());
    }

    public Employee() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public long getLaptopId() {
        return laptopId;
    }

    public void setLaptopId(final long laptopId) {
        this.laptopId = laptopId;
    }

    @Override
    public String getDiscriminator() {
        return Employee.class.getName();
    }

    @Override
    public long getSourceObjectId() {
        return 0;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return Employee.class;
    }

}
