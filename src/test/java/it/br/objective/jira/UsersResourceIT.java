package it.br.objective.jira;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class UsersResourceIT {

    private RestClient client;

    @Before
    public void setup() {
        ClientConfig config = new ClientConfig();
        BasicAuthSecurityHandler basicAuthHandler = new BasicAuthSecurityHandler();
        basicAuthHandler.setUserName("admin");
        basicAuthHandler.setPassword("admin");
        config.handlers(basicAuthHandler);

        client = new RestClient(config);

        JiraTestUtils.createUserBase(client);
    }

    @Test
    public void simpleRestCall() throws JSONException {
        Resource resource = client.resource("http://localhost:2990/jira/rest/projectbuilder/1.0/users");
        JSONArray response = new JSONArray(resource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class));
        assertThat(response.length(), greaterThan(999));
        assertThat(response.getJSONObject(0).getString("displayName"), is("admin"));
    }

    @Test
    public void partialUsernameCall() throws JSONException {
        Resource resource = client.resource("http://localhost:2990/jira/rest/projectbuilder/1.0/users");
        resource.queryParam("q", "headbanger1");
        JSONArray response = new JSONArray(resource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class));
        assertThat(response.length(), equalTo(11));
        assertThat(response.getJSONObject(0).getString("displayName"), startsWith("Head Banger"));
    }

    @Test
    public void surnameCall() throws JSONException {
        Resource resource = client.resource("http://localhost:2990/jira/rest/projectbuilder/1.0/users");
        resource.queryParam("q", "Einstein");
        JSONArray response = new JSONArray(resource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class));
        assertThat(response.length(), greaterThan(0));
        for(int i = 0; i < response.length(); ++i) {
            assertThat(response.getJSONObject(i).getString("displayName"), endsWith("Einstein"));
        }
    }
}
