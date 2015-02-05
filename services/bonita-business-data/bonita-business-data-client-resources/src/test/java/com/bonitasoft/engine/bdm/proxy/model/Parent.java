/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.proxy.model;

import java.util.List;

import com.bonitasoft.engine.bdm.Entity;

@SuppressWarnings("serial")
public class Parent implements Entity {

    private Child child;
    private List<Child> children;
    
    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    @Override
    public Long getPersistenceId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getPersistenceVersion() {
        // TODO Auto-generated method stub
        return null;
    }

}
