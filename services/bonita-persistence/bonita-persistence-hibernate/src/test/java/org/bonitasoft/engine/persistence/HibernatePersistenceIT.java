package org.bonitasoft.engine.persistence;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.hamcrest.CoreMatchers;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.junit.Test;

public class HibernatePersistenceIT {

    protected QueryOptions buildQueryOptions(final String... searchTerms) {
        Map<Class<? extends PersistentObject>, Set<String>> allFields = new HashMap<Class<? extends PersistentObject>, Set<String>>();
        final Set<String> fields = new HashSet<String>(2);
        fields.add("title");
        fields.add("author");
        allFields.put(Book.class, fields);
        QueryOptions queryOptions = new QueryOptions(0,
                10,
                Collections.<OrderByOption>emptyList(),
                new ArrayList<FilterOption>(0),
                new SearchFields(Arrays.asList(searchTerms), allFields));
        return queryOptions;
    }

    @Test
    public void should_return_single_result_when_wordsearch_not_enabled() throws Exception {
        boolean enableWordSearch = false;
        int expectedResults = 1;

        executeSearch(enableWordSearch, expectedResults);

    }


    @Test
    public void should_return_more_results_when_wordsearch_enabled() throws Exception {
        boolean enableWordSearch = true;
        int expectedResults = 2;

        executeSearch(enableWordSearch, expectedResults);

    }




    /**
     * @param enableWordSearch
     * @param expectedResults
     * @throws ClassNotFoundException
     * @throws SPersistenceException
     * @throws SBonitaReadException
     */
    protected void executeSearch(final boolean enableWordSearch, final int expectedResults) throws ClassNotFoundException, SPersistenceException, SBonitaReadException {
        // Setup Hibernate and extract SessionFactory
        Configuration configuration = new Configuration().configure();
        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        SessionFactory sessionFactory;
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);

        //
        final List<Class<? extends PersistentObject>> classMapping = Arrays.<Class<? extends PersistentObject>>asList(Book.class);
        final Map<String, String> classAliasMappings = Collections.singletonMap(Book.class.getName(), "book");
        PlatformHibernatePersistenceService persistenceService = new PlatformHibernatePersistenceService(sessionFactory, classMapping, classAliasMappings, enableWordSearch, Collections.<String>emptySet(), mock(TechnicalLoggerService.class));

        Session session;
        session = persistenceService.getSession(true);
        session.beginTransaction();
        try {
            Book book;
            book = new Book();
            book.setId(1);
            book.setTitle("lieues");
            book.setAuthor("Laurent");
            persistenceService.insert(book);

            book = new Book();
            book.setId(2);
            book.setTitle("Vingt mille lieues");
            book.setAuthor("Nicolas");
            persistenceService.insert(book);

        } finally {
            session.getTransaction().commit();
        }

        QueryOptions queryOptions = buildQueryOptions("lieues", "ipsum");

        session = persistenceService.getSession(true);
        session.beginTransaction();
        try {
            final List<Book> allBooks = persistenceService.selectList(new SelectListDescriptor<Book>("getAllBooks", null, Book.class, queryOptions));
            assertThat(allBooks.size(), CoreMatchers.equalTo(expectedResults));
        } finally {
            session.getTransaction().commit();
        }
    }

}
