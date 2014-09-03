package org.bonitasoft.engine.archive.model;

import org.bonitasoft.engine.persistence.PersistentObject;

public class Laptop extends SPersistentObjectImpl {

    private static final long serialVersionUID = 1L;

    private String brand;

    private String model;

    public Laptop(final String brand, final String model) {
        this.brand = brand;
        this.model = model;
    }

    public Laptop() {
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    @Override
    public String getDiscriminator() {
        return Laptop.class.getName();
    }

    @Override
    public long getSourceObjectId() {
        return 0;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return Laptop.class;
    }

}
