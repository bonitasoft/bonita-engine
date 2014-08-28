package org.bonitasoft.engine.archive.model;

import org.bonitasoft.engine.persistence.PersistentObject;

public class EmployeeProjectMapping extends SPersistentObjectImpl {

    private static final long serialVersionUID = 1L;

    private long employeeId;

    private long projectId;// The relationship between Employee and project is
                           // many-to-many.

    public EmployeeProjectMapping() {
    }

    public EmployeeProjectMapping(final Employee employee, final Project project) {
        employeeId = employee.getId();
        projectId = project.getId();
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(final long employeeId) {
        this.employeeId = employeeId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    @Override
    public String getDiscriminator() {
        return EmployeeProjectMapping.class.getName();
    }

    @Override
    public long getSourceObjectId() {
        return 0;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return EmployeeProjectMapping.class;
    }
}
