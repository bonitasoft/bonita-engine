package com.company.pojo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(uniqueConstraints = @javax.persistence.UniqueConstraint(columnNames = { "bonito", "string" }))
public class ConstrainedItem implements org.bonitasoft.engine.bdm.Entity {

    private static final long serialVersionUID = -230L;

    @Id
    @GeneratedValue
    private Long persistenceId;

    @Version
    private Long persistenceVersion;

    private long bonito;

    private String string;

    private Date un_constrained;

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }
}
