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
package com.company.pojo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

@Entity
public class ComplexInvoice implements org.bonitasoft.engine.bdm.Entity {

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
