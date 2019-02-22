package it.br.objective.jira;

import static it.br.objective.jira.JiraTestUtils.jsonResource;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.Resource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

public class IssueResourceIT {

    @Test
    public void nonExistentIssue() {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/projectbuilder/1.0/issue/NONEXIST-1/fields-required-in-transitions");
        try {
            resource.post(String.class, "[]");
        } catch (ClientWebException e) {
            assertThat(e.getResponse().getStatusCode(), is(404));
            assertThat(e.getResponse().getMessage(), is("Not Found"));
        }
    }

    @Test
    public void existentIssue() throws JSONException {
        JiraTestUtils.createProjectBase();
        String issueKey = JiraTestUtils.createIssue();

        Resource resource = jsonResource("http://localhost:2990/jira/rest/projectbuilder/1.0/issue/" + issueKey + "/fields-required-in-transitions");
        JSONArray response = new JSONArray(resource.post(String.class, "[11, 21, 22]"));

        assertThat(response.length(), equalTo(2));
        assertThat(response.getJSONObject(0).getInt("id"), equalTo(11));
        assertThat(response.getJSONObject(0).getJSONArray("requiredFields").length(), equalTo(0));
        assertThat(response.getJSONObject(1).getInt("id"), equalTo(21));
        assertThat(response.getJSONObject(1).getJSONArray("requiredFields").length(), equalTo(0));
    }

}
