package com.bonitasoft.engine.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Command implements Entity {

    private static final long serialVersionUID = 9171837127274880476L;

    @Id
    @GeneratedValue
    private final Long persistenceId;

    @Version
    private final Long persistenceVersion;

    @JsonIgnore
    private final List<CommandLine> lines;

    public Command(final Long persistenceId, final Long persistenceVersion) {
        super();
        lines = new ArrayList<CommandLine>();
        this.persistenceId = persistenceId;
        this.persistenceVersion = persistenceVersion;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    @LazyLoaded
    public List<CommandLine> getLines() {
        return lines;
    }

    public void addLine(final CommandLine line) {
        lines.add(line);
    }

}
