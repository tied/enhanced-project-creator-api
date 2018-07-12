package it.br.objective.jira;

import static it.br.objective.jira.JiraTestUtils.jsonResource;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import org.apache.wink.client.Resource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;

public class UsersResourceIT {

    @Before
    public void setup() {
        JiraTestUtils.createUserBase();
    }

    @Test
    public void simpleRestCall() throws JSONException {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/projectbuilder/1.0/users");
        JSONArray response = new JSONArray(resource.get(String.class));
        assertThat(response.length(), greaterThan(999));
        assertThat(response.getJSONObject(0).getString("displayName"), is("admin"));
    }

    @Test
    public void partialUsernameCall() throws JSONException {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/projectbuilder/1.0/users");
        resource.queryParam("q", "headbanger1");
        JSONArray response = new JSONArray(resource.get(String.class));
        assertThat(response.length(), equalTo(11));
        assertThat(response.getJSONObject(0).getString("displayName"), startsWith("Head Banger"));
    }

    @Test
    public void surnameCall() throws JSONException {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/projectbuilder/1.0/users");
        resource.queryParam("q", "Einstein");
        JSONArray response = new JSONArray(resource.get(String.class));
        assertThat(response.length(), greaterThan(0));
        for(int i = 0; i < response.length(); ++i) {
            assertThat(response.getJSONObject(i).getString("displayName"), endsWith("Einstein"));
        }
    }
}
