package org.bonitasoft.engine.page;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bonitasoft.engine.api.impl.converter.PageModelConverter;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Laurent Leseigneur
 */
public class ProcessPageDeployer {

    private final PageService pageService;
    private static final String regex = "^resources/customPages/(custompage_.*)\\.(zip)$";

    public ProcessPageDeployer(PageService pageService) {
        this.pageService = pageService;
    }

    public void deployProcessPages(BusinessArchive businessArchive, Long processDefinitionId, long userId) throws SBonitaException {
        final Map<String, byte[]> pageResources = getPageResources(businessArchive);
        for (final Map.Entry<String, byte[]> resource : pageResources.entrySet()) {
            deployPage(resource.getKey(), resource.getValue(), processDefinitionId, userId);
        }
    }

    protected Map<String, byte[]> getPageResources(BusinessArchive businessArchive) {
        return businessArchive.getResources(regex);
    }

    private void deployPage(String resourcePath, byte[] pageContent, Long processDefinitionId, long userId) throws SBonitaException {
        final Matcher pathMatcher = getPathMatcher(resourcePath);
        if (pathMatcher.matches()) {
            final String pageName = pathMatcher.group(1);
            final String extension = pathMatcher.group(2);
            String contentName = new StringBuilder().append(pageName).append(".").append(extension).toString();
            final SPage sPage = pageService.getPageByNameAndProcessDefinitionId(pageName, processDefinitionId);
            if (sPage != null) {
                pageService.updatePageContent(sPage.getId(), pageContent, contentName);
            } else {
                final PageCreator pageCreator = new PageCreator(pageName, contentName, ContentType.FORM, processDefinitionId);
                final SPage newPage = new PageModelConverter().constructSPage(pageCreator, userId);
                pageService.addPage(newPage, pageContent);
            }
        }
    }

    private Matcher getPathMatcher(String resourcePath) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(resourcePath);
        return matcher;
    }

}
