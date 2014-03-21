package com.bonitasoft.engine.operation.pojo;

import java.io.Serializable;

public class InvalidTravel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int nbDays;

    public int getNbDays() {
        return nbDays;
    }

    public void setNbDays(final int nbDays) {
        this.nbDays = nbDays;
    }
}