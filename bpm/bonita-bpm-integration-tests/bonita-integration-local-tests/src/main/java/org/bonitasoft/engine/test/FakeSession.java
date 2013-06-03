package org.bonitasoft.engine.test;

import java.util.Date;

import org.bonitasoft.engine.session.APISession;

/**
 * @author Matthieu Chaffotte
 */
public class FakeSession implements APISession {

    private static final long serialVersionUID = -957530454714716273L;

    private long id;

    private final Date creationDate;

    private long duration;

    private final String userName;

    private final long userId;

    private final String tenant;

    private final long tenantId;

    private boolean technicalUser = false;

    public FakeSession(final APISession session) {
        this.id = session.getId();
        this.creationDate = session.getCreationDate();
        this.duration = session.getDuration();
        this.userName = session.getUserName();
        this.tenant = session.getTenantName();
        this.tenantId = session.getTenantId();
        this.userId = session.getUserId();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Date getCreationDate() {
        return this.creationDate;
    }

    @Override
    public long getDuration() {
        return this.duration;
    }

    @Override
    public String getUserName() {
        return this.userName;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    public void setDuration(final long newDuration) {
        this.duration = newDuration;
    }

    public void setId(final long identifier) {
        this.id = identifier;
    }

    @Override
    public String getTenantName() {
        return this.tenant;
    }

    @Override
    public long getTenantId() {
        return this.tenantId;
    }

    @Override
    public boolean isTechnicalUser() {
        return technicalUser;
    }

    public void setTechnicalUser(final boolean technicalUser) {
        this.technicalUser = technicalUser;
    }

}
