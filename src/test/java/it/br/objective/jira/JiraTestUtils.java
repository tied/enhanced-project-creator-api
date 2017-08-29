package it.br.objective.jira;

import org.apache.commons.lang3.StringUtils;
import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;

public class JiraTestUtils {

    public static void createUserBase(RestClient client) {
        Resource resource = client.resource("http://localhost:2990/jira/rest/api/2/user").contentType(MediaType.APPLICATION_JSON_TYPE);
        resource.queryParam("username", "headbanger99");
        if(resource.get().getStatusCode() == 404) {
            createHeadbangers(client);
        }
        resource.queryParam("username", "admiring_albattani");
        if(resource.get().getStatusCode() == 404) {
            createRandomUsers(client);
        }
    }

    private static void createHeadbangers(RestClient client) {
        Resource resource = client.resource("http://localhost:2990/jira/rest/api/2/user").contentType(MediaType.APPLICATION_JSON_TYPE);
        for (int i = 0; i < 100; ++i) {
            try {
                String username = "headbanger" + i;
                String password = "headbanger" + i;
                String email = "headbanger@metal.com";
                String displayName = "Head Banger " + i;
                JSONObject request = new JSONObject()
                        .put("name", username)
                        .put("password", password)
                        .put("emailAddress", email)
                        .put("displayName", displayName);
                String response = resource.post(String.class, request.toString());
//                System.out.println(response);
            } catch (ClientWebException | JSONException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Source: https://github.com/moby/moby/blob/master/pkg/namesgenerator/names-generator.go
     *
     * @throws JSONException
     * @param client
     */
    private static void createRandomUsers(RestClient client) {
        String[] left = new String[]{
                "admiring", "adoring", "affectionate", "agitated", "amazing", "angry", "awesome", "blissful", "boring",
                "dazzling", "determined", "distracted", "dreamy", "eager", "ecstatic", "elastic", "elated", "elegant",
                "gifted", "goofy", "gracious", "happy", "hardcore", "heuristic", "hopeful", "hungry", "infallible",
                "musing", "naughty", "nervous", "nifty", "nostalgic", "objective", "optimistic", "peaceful", "pedantic",
                "serene", "sharp", "silly", "sleepy", "stoic", "stupefied", "suspicious", "tender", "thirsty",
                "xenodochial", "youthful", "zealous", "zen"
        };

        String[] right = new String[]{
                "albattani", "allen", "almeida", "agnesi", "archimedes", "ardinghelli", "aryabhata", "austin",
                "blackwell", "bohr", "booth", "borg", "bose", "boyd", "brahmagupta", "brattain", "brown", "carson",
                "dijkstra", "dubinsky", "easley", "edison", "einstein", "elion", "engelbart", "euclid", "euler",
                "golick", "goodall", "haibt", "hamilton", "hawking", "heisenberg", "hermann", "heyrovsky", "hodgkin",
                "jones", "kalam", "kare", "keller", "kepler", "khorana", "kilby", "kirch", "knuth", "kowalevski",
                "lumiere", "mahavira", "mayer", "mccarthy", "mcclintock", "mclean", "mcnulty", "meitner", "meninsky",
                "noether", "northcutt", "noyce", "panini", "pare", "pasteur", "payne", "perlman", "pike", "poincare",
                "saha", "sammet", "shaw", "shirley", "shockley", "sinoussi", "snyder", "spence", "stallman",
                "varahamihira", "visvesvaraya", "volhard", "wescoff", "wiles", "williams", "wilson", "wing", "wozniak"
        };

        Resource resource = client.resource("http://localhost:2990/jira/rest/api/2/user").contentType(MediaType.APPLICATION_JSON_TYPE);
        for (String first : left) {
            for (String last : right) {
                try {
                    String username = first + "_" + last;
                    String password = first + "_" + last;
                    String email = username + "@docker.com";
                    String displayName = StringUtils.capitalize(first) + " " + StringUtils.capitalize(last);
                    JSONObject request = new JSONObject()
                            .put("name", username)
                            .put("password", password)
                            .put("emailAddress", email)
                            .put("displayName", displayName);
                    String response = resource.post(String.class, request.toString());
//                    System.out.println(response);
                } catch (ClientWebException | JSONException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        try {
            /* Steve Wozniak is not boring */
            client.resource("http://localhost:2990/jira/rest/api/2/user?username=boring_wozniak").delete();
        } catch (ClientWebException ex) {
            //
        }
    }
}
