package it.br.objective.jira;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import br.objective.jira.api.EnhancedProjectBuilder;
import com.atlassian.sal.api.ApplicationProperties;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class MyComponentWiredTest
{
    private final ApplicationProperties applicationProperties;
    private final EnhancedProjectBuilder myPluginComponent;

    public MyComponentWiredTest(ApplicationProperties applicationProperties,EnhancedProjectBuilder myPluginComponent)
    {
        this.applicationProperties = applicationProperties;
        this.myPluginComponent = myPluginComponent;
    }

    @Test
    public void testMyName()
    {
        assertEquals("names do not match!", "myComponent:" + applicationProperties.getDisplayName(),myPluginComponent.getName());
    }
}