/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.page;

import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Laurent Leseigneur
 */
public class PageServiceImpl implements PageService {

    private static final String GET_PAGE_CONTENT = "getPageContent";

    private static final String DELETE_PAGE = "deletePage";

    private static final String SEARCH_PAGES = "searchPages";

    private static final String GET_NUMBER_OF_PAGES = "getNumberOfPages";

    // private static final CharSequence COMMA = ",";
    //
    // private static final CharSequence SEMICOLON = ";";
    //
    // private static final String NEW_LINE = "\n";

    // private final DataSource dataSource;

    private static final String PAGE_NAME = "pageName";

    private static final String GET_PAGE_BY_NAME = "getPageByName";

    private static final String GET_PAGE_BY_ID = "getPageById";

    private static final String GET_PAGE = "getPage";

    private static final String ADD_PAGE = "addPage";

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    public PageServiceImpl(/* FIXME remove dataxsource */final DataSource dataSource, final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {
        // this.dataSource = dataSource;
        this.persistenceService = persistenceService;
        this.eventService = eventService;
        this.recorder = recorder;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    // @Override
    // public String selectList(final String selectQuery) throws SQLException {
    // final String lowerSQ = selectQuery.toLowerCase();
    // if (!lowerSQ.startsWith("select")) {
    // throw new SQLException("The statement is not a SELECT query");
    // }
    // final Connection connection = dataSource.getConnection();
    // try {
    // final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    // try {
    // return executeQuery(selectQuery, statement);
    // } finally {
    // if (statement != null) {
    // statement.close();
    // }
    // }
    //
    // } finally {
    // connection.close();
    // }
    // }

    // private String executeQuery(final String selectQuery, final Statement statement) throws SQLException {
    // final ResultSet resultSet = statement.executeQuery(selectQuery);
    // try {
    // return parseResultSet(resultSet);
    // } finally {
    // if (resultSet != null) {
    // resultSet.close();
    // }
    // }
    // }

    // private String parseResultSet(final ResultSet resultSet) throws SQLException {
    // final StringBuilder builder = new StringBuilder();
    // if (resultSet != null) {
    // final int columns = appendColumnNames(resultSet, builder);
    // appendValues(resultSet, builder, columns);
    // }
    // return builder.toString();
    // }

    // private void appendValues(final ResultSet resultSet, final StringBuilder builder, final int columns) throws SQLException {
    // while (resultSet.next()) {
    // for (int j = 1; j < columns; j++) {
    // final Object value = resultSet.getObject(j);
    // builder.append(protect(String.valueOf(value))).append(",");
    // }
    // final Object value = resultSet.getObject(columns);
    // // Special treatment of last record (to avoid having extra comma at the end):
    // builder.append(value).append(NEW_LINE);
    // }
    // }

    // private int appendColumnNames(final ResultSet resultSet, final StringBuilder builder) throws SQLException {
    // final ResultSetMetaData metaData = resultSet.getMetaData();
    // final int columns = metaData.getColumnCount();
    // for (int i = 1; i < columns; i++) {
    // final String columnName = metaData.getColumnLabel(i);
    // // in order to use the same case for all database
    // builder.append(columnName.toUpperCase()).append(",");
    // }
    // // Special treatment of last record (to avoid having extra comma at the end):
    // final String columnName = metaData.getColumnLabel(columns);
    // builder.append(columnName.toUpperCase()).append(NEW_LINE);
    // return columns;
    // }
    //
    // private String protect(final String value) {
    // if (isDangerous(value)) {
    // return "\"" + escape(value) + "\"";
    // }
    // return value;
    // }

    // private boolean isDangerous(final String value) {
    // return value.contains(COMMA) || value.contains(SEMICOLON) || value.contains("\"");
    // }
    //
    // private String escape(final String value) {
    // if (value.contains("\"")) {
    // return value.replaceAll("\"", "\"\"");
    // }
    // return value;
    // }

    @Override
    public SPage addPage(final SPage page, final byte[] content) throws SPageCreationException, SPageAlreadyExistsException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), ADD_PAGE));
        }
        final String message = "Adding a new page with name " + page.getName();
        final SPageLogBuilder logBuilder = getPageLog(ActionType.CREATED, message);
        try {
            SSavePageWithContent pageContent = new SSavePageWithContentImpl(page, content);
            final InsertRecord insertContentRecord = new InsertRecord(pageContent);
            final SInsertEvent insertContentEvent = getInsertEvent(insertContentRecord, PAGE);
            recorder.recordInsert(insertContentRecord, insertContentEvent);
            page.setId(pageContent.getId());

            initiateLogBuilder(pageContent.getId(), SQueriableLog.STATUS_OK, logBuilder, ADD_PAGE);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), ADD_PAGE));
            }
            return page;
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), ADD_PAGE, re));
            }
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, ADD_PAGE);
            throw new SPageCreationException(re);
        }
    }

    private SPageLogBuilder getPageLog(final ActionType actionType, final String message) {
        final SPageLogBuilder logBuilder = new SPageLogBuilderImpl();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public SPage getPage(final long pageId) throws SBonitaReadException, SPageNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), GET_PAGE));
        }
        try {
            final SPage page = persistenceService.selectById(new SelectByIdDescriptor<SPage>(GET_PAGE_BY_ID, SPage.class, pageId));
            if (page == null) {
                throw new SPageNotFoundException(pageId);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), GET_PAGE));
            }
            return page;
        } catch (final SBonitaReadException sbe) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), GET_PAGE, sbe));
            }
            throw sbe;
        }
    }

    @Override
    public SPage getPageByName(final String pageName) throws SBonitaReadException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), GET_PAGE_BY_NAME));
        }
        try {
            final SPage page = persistenceService.selectOne(new SelectOneDescriptor<SPage>(GET_PAGE_BY_NAME, Collections.singletonMap(PAGE_NAME,
                    (Object) pageName), SPage.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), GET_PAGE_BY_NAME));
            }
            return page;
        } catch (final SBonitaReadException sbe) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), GET_PAGE_BY_NAME, sbe));
            }
            throw sbe;
        }
    }

    @Override
    public long getNumberOfPages(final QueryOptions options) throws SBonitaSearchException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), GET_NUMBER_OF_PAGES));
        }
        try {
            final long number = persistenceService.getNumberOfEntities(SPage.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), GET_NUMBER_OF_PAGES));
            }
            return number;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), GET_NUMBER_OF_PAGES, bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SPage> searchPages(final QueryOptions options) throws SBonitaSearchException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), SEARCH_PAGES));
        }
        try {
            final List<SPage> pages = persistenceService.searchEntity(SPage.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), SEARCH_PAGES));
            }
            return pages;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), SEARCH_PAGES, bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public void deletePage(final long pageId) throws SPageDeletionException, SPageNotFoundException {
        try {
            final SPage page = getPage(pageId);
            deletePage(page);
        } catch (final SBonitaReadException sbe) {
            new SPageDeletionException(sbe);
        }
    }

    private void deletePage(final SPage page) throws SPageDeletionException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), DELETE_PAGE));
        }
        final SPageLogBuilder logBuilder = getPageLog(ActionType.DELETED, "Deleting report named: " + page.getName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(page);
            final SDeleteEvent deleteEvent = getDeleteEvent(page, PAGE);
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_OK, logBuilder, DELETE_PAGE);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), DELETE_PAGE));
            }
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), DELETE_PAGE, re));
            }
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, DELETE_PAGE);
            throw new SPageDeletionException(re);
        }
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SInsertEvent getInsertEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.CREATED)) {
            return (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(type).setObject(object).done();
        } else {
            return null;
        }
    }

    private SDeleteEvent getDeleteEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.DELETED)) {
            return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(type).setObject(object).done();
        } else {
            return null;
        }
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String methodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), methodName, log);
        }
    }

    @Override
    public byte[] getPageContent(final long pageId) throws SBonitaReadException, SPageNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), GET_PAGE_CONTENT));
        }
        try {
            final SPageContent getPageContent = persistenceService.selectById(new SelectByIdDescriptor<SPageContent>(GET_PAGE_CONTENT,
                    SPageContent.class, pageId));
            if (getPageContent == null) {
                throw new SPageNotFoundException(pageId);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), GET_PAGE_CONTENT));
            }
            return getPageContent.getContent();
        } catch (final SBonitaReadException sbe) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), GET_PAGE_CONTENT, sbe));
            }
            throw sbe;
        }
    }

}
