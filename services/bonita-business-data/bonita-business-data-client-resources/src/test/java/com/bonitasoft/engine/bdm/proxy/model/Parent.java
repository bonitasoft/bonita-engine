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
