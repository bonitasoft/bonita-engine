package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.model.SToken;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Celine Souchet
 * @author Baptiste Mesta
 */
public class TokenServiceIntegrationTest extends CommonBPMServicesTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(CommonBPMServicesTest.class);

    private static BPMServicesBuilder bpmServicesBuilder;

    private static TokenService tokenService;

    private static TransactionService transactionService;

    static {
        bpmServicesBuilder = new BPMServicesBuilder();
        tokenService = getTokenService();
        transactionService = bpmServicesBuilder.getTransactionService();
    }

    static TokenService getTokenService() {
        return bpmServicesBuilder.getTokenService();
    }

    @After
    public void cleanUp() {
        try {
            transactionService.begin();
            tokenService.deleteAllTokens();
            transactionService.complete();
        } catch (final Exception e) {
            LOGGER.error("Error during clean-up. Ignoring...", e);
        }
    }

    @Test
    public void getNumberOfTokens() throws Exception {
        transactionService.begin();
        tokenService.createToken(123l, 124l, 125l);
        tokenService.createToken(123l, 126l, 127l);
        tokenService.createToken(111l, 126l, 127l);
        final long numberOfToken = tokenService.getNumberOfToken(123l);
        transactionService.complete();
        assertEquals(2, numberOfToken);
    }

    @Test
    public void getNumberOfTokensByRefId() throws Exception {
        transactionService.begin();
        tokenService.createToken(123l, 124l, 125l);
        tokenService.createToken(123l, 126l, 127l);
        final long numberOfToken = tokenService.getNumberOfToken(123l, 124l);
        transactionService.complete();
        assertEquals(1, numberOfToken);
    }

    @Test
    public void getTokenByRefId() throws Exception {
        transactionService.begin();
        tokenService.createToken(123l, 124l, 125l);
        tokenService.createToken(123l, 126l, 127l);
        final SToken token = tokenService.getToken(123l, 124l);
        transactionService.complete();
        assertEquals(123l, token.getProcessInstanceId());
        assertEquals(124l, token.getRefId().longValue());
        assertEquals(125l, token.getParentRefId().longValue());
    }

    @Test
    public void createMultipleTokens() throws Exception {
        transactionService.begin();
        tokenService.createTokens(123l, 124l, 125l, 3);
        final int numberOfToken = tokenService.getNumberOfToken(123l);
        transactionService.complete();
        assertEquals(3, numberOfToken);
    }

    @Test
    public void deleteMultipleTokens() throws Exception {
        transactionService.begin();
        tokenService.createTokens(123l, 124l, 125l, 3);
        tokenService.deleteTokens(123l, 124l, 2);
        final int numberOfToken = tokenService.getNumberOfToken(123l);
        transactionService.complete();
        assertEquals(1, numberOfToken);
    }

    @Test
    public void deleteToken() throws Exception {
        transactionService.begin();
        final SToken token = tokenService.createToken(123l, 124l, 125l);
        tokenService.deleteToken(token);
        final int numberOfToken = tokenService.getNumberOfToken(123l);
        transactionService.complete();
        assertEquals(0, numberOfToken);
    }

    @Test
    public void deleteTokens() throws Exception {
        transactionService.begin();
        tokenService.createToken(123l, 124l, 125l);
        tokenService.createToken(123l, 124l, 125l);
        tokenService.deleteTokens(123l);
        final int numberOfToken = tokenService.getNumberOfToken(123l);
        transactionService.complete();
        assertEquals(0, numberOfToken);
    }

    @Test
    public void getToken() throws Exception {
        // Get Token
        transactionService.begin();
        final SToken result = tokenService.createToken(111l, 222l, 333l);
        final SToken token = tokenService.getToken(111l, 222l);
        transactionService.complete();
        assertEquals(result, token);
    }

}
