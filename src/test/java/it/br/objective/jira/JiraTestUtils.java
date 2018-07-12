package it.br.objective.jira;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import it.br.objective.jira.util.Guava;
import it.br.objective.jira.util.Json;

public class JiraTestUtils {

    private static final String ADMIN_AUTH = basicAuthorizationToken("admin");

    private static final RestClient client;

    static {
        ClientConfig config = new ClientConfig();
        client = new RestClient(config);
    }

    public static Resource jsonResource(String url) {
        return client.resource(url)
                .header("Authorization", ADMIN_AUTH)
                .contentType(MediaType.APPLICATION_JSON_TYPE);
    }

    public static Resource jsonResource(String url, String user) {
        return client.resource(url)
                .header("Authorization", basicAuthorizationToken(user))
                .contentType(MediaType.APPLICATION_JSON_TYPE);
    }

    public static void createUserBase() {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/user");
        resource.queryParam("username", "headbanger99");
        if(resource.get().getStatusCode() == 404) {
            createHeadbangers();
        }
        resource.queryParam("username", UserNameGenerator.username(0));
        if(resource.get().getStatusCode() == 404) {
            createRandomUsers();
        }
    }

    public static void createProjectBase() {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/project/" + ProjectNameGenerator.key(0));
        if (resource.get().getStatusCode() == 404) {
            createRandomProjects();
        }
    }

    public static void createRoles() {
        List<String> roleNames = allRoleNames();

        Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/role");
        for (int i = 0; i < RoleNameGenerator.length; ++i) {
            try {
                String role = RoleNameGenerator.name(i);
                if (!roleNames.contains(role)) {
                    JSONObject request = new JSONObject()
                            .put("name", role)
                            .put("description", RoleNameGenerator.description(i));
                    String response = resource.post(String.class, request.toString());
//                    System.out.println(response);
                }
            } catch (ClientWebException ex) {
                throw new RuntimeException(ex.getResponse().getEntity(String.class), ex);
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Apply the following to default permission scheme:
     * - Revoke permission BROWSE_PROJECTS to any logged user
     * - Grant permission BROWSE_PROJECTS to users that have any role associated to a project
     */
    public static void configurePermissionScheme() {
        try {
            final String schemeId = "0";

            JSONObject permissionScheme = new JSONObject(jsonResource("http://localhost:2990/jira/rest/api/2/permissionscheme/" + schemeId + "?expand=permissions").get().getEntity(String.class));
            JSONArray permissions = permissionScheme.getJSONArray("permissions");
            Table<String, String, String> browseProjectsPermissions = Json.stream(permissions)
                    .filter(item -> "BROWSE_PROJECTS".equals(item.opt("permission")))
                    .collect(Guava.toTable(
                            item -> (String) ((JSONObject) item.opt("holder")).opt("type")
                            , item -> ObjectUtils.defaultIfNull((String) ((JSONObject) item.opt("holder")).opt("parameter"), "")
                            , item -> String.valueOf(item.opt("id"))
                            , HashBasedTable::create
                    ));

            if(browseProjectsPermissions.containsRow("applicationRole")) {
                for(String permissionId : browseProjectsPermissions.row("applicationRole").values()) {
                    jsonResource("http://localhost:2990/jira/rest/api/2/permissionscheme/" + schemeId + "/permission/" + permissionId).delete();
                }
            }
            List<String> allRoles = allRoleIds();
            Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/permissionscheme/" + schemeId + "/permission");
            for(String role : allRoles) {
                if(!browseProjectsPermissions.containsRow("projectRole") || !browseProjectsPermissions.row("projectRole").containsKey(role)) {
                    JSONObject request = new JSONObject()
                            .put("permission", "BROWSE_PROJECTS")
                            .put("holder", new JSONObject()
                                    .put("type", "projectRole")
                                    .put("parameter", role));
                    String result = resource.post(String.class, request.toString());
//                    System.out.println(result);
                }
            }
        } catch (ClientWebException ex) {
            throw new RuntimeException(ex.getMessage() + ": " + ex.getResponse().getEntity(String.class), ex);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Project bad-rabbit:
     * - Project Manager   -> admiring_archimedes
     * - Customer          -> stupefied_shirley
     * Project eager-machine:
     * - Project Manager   -> determined_dijkstra
     * - Developers        -> admiring_archimedes
     * Project jaded-cabbage:
     * - Project Manager   -> ecstatic_einstein
     * - Quality Assurance -> determined_dijkstra
     * Project yellow-kettle:
     * - Project Manager   -> stupefied_shirley
     * - Reviewer          -> ecstatic_einstein
     */
    public static void addSomeUsersToSomeProjectRoles() {
        String rabbitProject = "bad-rabbit";
        String machineProject = "eager-machine";
        String cabbageProject = "jaded-cabbage";
        String kettleProject = "yellow-kettle";

        String archimedes = "admiring_archimedes";
        String dijkstra = "determined_dijkstra";
        String einstein = "ecstatic_einstein";
        String shirley = "stupefied_shirley";

        String customerRole = "Customer";
        String devRole = "Developers";
        String pmRole = "Project Manager";
        String qaRole = "Quality Assurance";
        String reviewerRole = "Reviewer";

        Table<String, String, List<String>> projectRoleActors = HashBasedTable.create();
        BiFunction<String, String, List<String>> actorsFor = (project, role) -> projectRoleActors.row(project).computeIfAbsent(role, key -> new ArrayList<>());

        actorsFor.apply(rabbitProject, pmRole).add(archimedes);
        actorsFor.apply(machineProject, pmRole).add(dijkstra);
        actorsFor.apply(cabbageProject, pmRole).add(einstein);
        actorsFor.apply(kettleProject, pmRole).add(shirley);

        actorsFor.apply(rabbitProject, customerRole).add(shirley);
        actorsFor.apply(machineProject, devRole).add(archimedes);
        actorsFor.apply(cabbageProject, qaRole).add(dijkstra);
        actorsFor.apply(kettleProject, reviewerRole).add(einstein);

        try {
            Map<String, String> projectIdMap = projectNameIdMap();
            Map<String, String> roleIdMap = roleNameIdMap();

            for(Table.Cell<String, String, List<String>> cell : projectRoleActors.cellSet()) {
                String project = projectIdMap.get(cell.getRowKey());
                String role = roleIdMap.get(cell.getColumnKey());
                JSONArray actors = cell.getValue().stream()
                        .collect(JSONArray::new, JSONArray::put, (a, b) -> {
                            throw new RuntimeException("no combiner");
                        });
                JSONObject request = new JSONObject()
                        .put("id", role)
                        .put("categorisedActors", new JSONObject()
                                .put("atlassian-user-role-actor", actors)
                                .put("atlassian-group-role-actor", new JSONArray())
                        );

                Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/project/" + project + "/role/" + role);
                String response = resource.put(String.class, request.toString());
//                System.out.println(response);
            }
        } catch (ClientWebException ex) {
            throw new RuntimeException(ex.getMessage() + ": " + ex.getResponse().getEntity(String.class), ex);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void createHeadbangers() {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/user");
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

    private static void createRandomUsers() {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/user");
        for(int i = 0; i < UserNameGenerator.length; ++i) {
            try {
                String username = UserNameGenerator.username(i);
                String password = UserNameGenerator.password(i);
                String email = UserNameGenerator.email(i);
                String displayName = UserNameGenerator.displayName(i);
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
        try {
            /* Steve Wozniak is not boring */
            jsonResource("http://localhost:2990/jira/rest/api/2/user?username=boring_wozniak").delete();
        } catch (ClientWebException ex) {
            //
        }
    }

    private static void createRandomProjects() {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/project");
        for (int i = 0; i < ProjectNameGenerator.length; ++i) {
            try {
                JSONObject request = new JSONObject()
                        .put("key", ProjectNameGenerator.key(i))
                        .put("name", ProjectNameGenerator.name(i))
                        .put("projectTypeKey", "business")
                        .put("lead", "admin");
                String response = resource.post(String.class, request.toString());
//                System.out.println(response);
            } catch (ClientWebException ex) {
                throw new RuntimeException(ex.getResponse().getEntity(String.class), ex);
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static Stream<JSONObject> allProjects() {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/project");
        JSONArray allProjects;
        try {
            allProjects = new JSONArray(resource.get().getEntity(String.class));
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
        return Json.stream(allProjects);
    }

    private static Map<String, String> projectNameIdMap() {
        return allProjects()
                .collect(toMap(obj -> (String) obj.opt("name"), obj -> String.valueOf(obj.opt("id"))));
    }

    private static Stream<JSONObject> allRoles() {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/api/2/role");
        JSONArray allRoles;
        try {
            allRoles = new JSONArray(resource.get().getEntity(String.class));
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
        return Json.stream(allRoles);
    }

    private static List<String> allRoleIds() {
        return allRoles()
                .map(obj -> String.valueOf(obj.opt("id")))
                .collect(toList());
    }

    private static List<String> allRoleNames() {
        return allRoles()
                .map(obj -> (String) obj.opt("name"))
                .collect(toList());
    }

    private static Map<String, String> roleNameIdMap() {
        return allRoles()
                .collect(toMap(obj -> (String) obj.opt("name"), obj -> String.valueOf(obj.opt("id"))));
    }

    /**
     * Source: https://github.com/moby/moby/blob/master/pkg/namesgenerator/names-generator.go
     */
    private static class UserNameGenerator {
        static final String[] left = new String[] {
                "admiring", "adoring", "affectionate", "agitated", "amazing", "angry", "awesome", "blissful", "boring",
                "dazzling", "determined", "distracted", "dreamy", "eager", "ecstatic", "elastic", "elated", "elegant",
                "gifted", "goofy", "gracious", "happy", "hardcore", "heuristic", "hopeful", "hungry", "infallible",
                "musing", "naughty", "nervous", "nifty", "nostalgic", "objective", "optimistic", "peaceful", "pedantic",
                "serene", "sharp", "silly", "sleepy", "stoic", "stupefied", "suspicious", "tender", "thirsty",
                "xenodochial", "youthful", "zealous", "zen"
        };

        static final String[] right = new String[] {
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

        static final int length = left.length * right.length;

        static String username(int i) {
            return first(i) + "_" + last(i);
        }

        static String password(int i) {
            return username(i);
        }

        static String email(int i) {
            return username(i) + "@docker.com";
        }

        public static String displayName(int i) {
            return StringUtils.capitalize(first(i)) + " " + StringUtils.capitalize(last(i));
        }

        private static String first(int i) {
            return left[i % right.length];
        }

        private static String last(int i) {
            return right[i / right.length];
        }
    }

    /**
     * Source: https://github.com/aceakash/project-name-generator
     */
    private static class ProjectNameGenerator {
        static final List<String> adjectives = asList(
                "aback", "bad", "cagey", "daffy", "eager", "fabulous", "gabby", "habitual", "icky", "jaded", "kaput",
                "labored", "macabre", "naive", "oafish", "pacific", "quack", "rabid", "sable", "taboo", "ubiquitous",
                "vacuous", "wacky", "yellow", "zany");

        static final List<String> nouns = asList(
                "account", "babies", "cabbage", "dad", "ear", "face", "gate", "hair", "ice", "jail", "kettle",
                "laborer", "machine", "name", "oatmeal", "page", "quarter", "rabbit", "sack", "table", "umbrella",
                "vacation", "walk", "yak", "zebra");

        static final int length;

        static {
            Collections.shuffle(adjectives, new Random(1L));
            Collections.shuffle(nouns, new Random(2L));
            length = Math.min(adjectives.size(), nouns.size());
        }

        static String key(int i) {
            return adjectives.get(i).substring(0, 2).toUpperCase() + nouns.get(i).substring(0, 2).toUpperCase();
        }

        static String name(int i) {
            return adjectives.get(i) + "-" + nouns.get(i);
        }
    }

    private static class RoleNameGenerator {
        static final String[] names = {
                "Administrators", "Customer", "Developers", "Project Manager", "Quality Assurance",
                "Reporter", "Reviewer"};

        static final int length = names.length;

        static String name(int i) {
            return names[i];
        }

        public static String description(int i) {
            return "A project role that represents " + name(i) + " in a project";
        }
    }

    private static String basicAuthorizationToken(String user) {
        String credentials = user + ":" + user;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
