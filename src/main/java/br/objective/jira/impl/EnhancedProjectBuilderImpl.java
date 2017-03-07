package br.objective.jira.impl;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import br.objective.jira.api.EnhancedProjectBuilder;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService ({EnhancedProjectBuilder.class})
@Named ("enhancedProjectBuilder")
public class EnhancedProjectBuilderImpl implements EnhancedProjectBuilder
{
    @ComponentImport
    private final ApplicationProperties applicationProperties;

    @Inject
    public EnhancedProjectBuilderImpl(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public String getName()
    {
        if(null != applicationProperties)
        {
            return "enhancedProjectBuilder:" + applicationProperties.getDisplayName();
        }
        
        return "enhancedProjectBuilder";
    }
}