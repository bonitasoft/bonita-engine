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
package org.bonitasoft.engine.bdm.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Employee {

    private List<Employee> employees = new ArrayList<Employee>(10);
    private List<Address> addresses = new ArrayList<Address>(10);

    private Employee manager;
    private Address address;

    public Employee() {
    }

    public void setEmployees(final List<Employee> employees) {
        this.employees = employees;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addToAddresses(final Employee addTo) {
        final List employees = getEmployees();
        employees.add(addTo);
    }

    @SuppressWarnings("rawtypes")
    public void removeFromAddresses(final Employee removeFrom) {
        final List employees = getEmployees();
        employees.remove(removeFrom);
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}