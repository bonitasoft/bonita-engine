/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.company.pojo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

@Entity
public class ComplexInvoice implements com.bonitasoft.engine.bdm.Entity {

    private static final long serialVersionUID = -230L;

    @Id
    @GeneratedValue
    private Long persistenceId;

    @Version
    private Long persistenceVersion;

    private String string;

    @Lob
    private String aLongText;

    private byte aByte;

    private char aChar;

    private boolean aBoolean;

    private Date aDate;

    private double aDouble;

    private float aFloat;

    private int anInteger;

    private long aLong;

    private short aShort;

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }
}
