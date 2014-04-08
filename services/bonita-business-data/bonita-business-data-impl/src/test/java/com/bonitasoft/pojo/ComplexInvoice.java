package com.bonitasoft.pojo;

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
