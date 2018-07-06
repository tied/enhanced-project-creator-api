package it.br.objective.jira;

import static it.br.objective.jira.JiraTestUtils.jsonResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.wink.client.Resource;
import org.junit.Before;
import org.junit.Test;

import com.google.common.reflect.TypeToken;

import br.objective.jira.rest.RoleData;
import it.br.objective.jira.util.Json;

public class RolesResourceIT {

    @Before
    public void setup() {
        JiraTestUtils.createProjectBase();
        JiraTestUtils.createUserBase();
        JiraTestUtils.createRoles();
        JiraTestUtils.configurePermissionScheme();
        JiraTestUtils.addSomeUsersToSomeProjectRoles();
    }

    @Test
    public void archimedesRolesVisibility() {
        // given projects:
        // - bad-rabbit
        // - eager-machine
        // that have the following roles associated with some Actor
        // bad-rabbit:
        // - Administrators
        // - Project Manager
        // - Customer
        // eager-machine:
        // - Administrators
        // - Project Manager
        // - Developers

        Resource resource = jsonResource("http://localhost:2990/jira/rest/projectbuilder/1.0/roles", "admiring_archimedes");
        List<RoleData> result = Json.decode(resource.get().getEntity(String.class), new TypeToken<List<RoleData>>() {});

        // admiring_archimedes can see both projects, so he should be able to view roles: Administrators, Customer, Developers, Project Manager
        assertThat(result)
                .extracting(role -> role.name)
                .containsOnly("Administrators", "Customer", "Developers", "Project Manager");
    }

    @Test
    public void einsteinRolesVisibility() {
        // given projects:
        // - jaded-cabbage
        // - yellow-kettle
        // that have the following roles associated with some Actor
        // jaded-cabbage:
        // - Administrators
        // - Project Manager
        // - Quality Assurance
        // yellow-kettle:
        // - Administrators
        // - Project Manager
        // - Reviewer

        Resource resource = jsonResource("http://localhost:2990/jira/rest/projectbuilder/1.0/roles", "ecstatic_einstein");
        List<RoleData> result = Json.decode(resource.get().getEntity(String.class), new TypeToken<List<RoleData>>() {});

        // ecstatic_einstein can see both projects, so he should be able to view roles: Administrators, Project Manager, Quality Assurance, Reviewer
        assertThat(result)
                .extracting(role -> role.name)
                .containsOnly("Administrators", "Project Manager", "Quality Assurance", "Reviewer");
    }

    @Test
    public void adminRoleVisibility() {
        Resource resource = jsonResource("http://localhost:2990/jira/rest/projectbuilder/1.0/roles", "admin");
        List<RoleData> result = Json.decode(resource.get().getEntity(String.class), new TypeToken<List<RoleData>>() {});
        // admin has access to all projects, but some roles are unused
        assertThat(result)
                .extracting(role -> role.name)
                .doesNotContain("Reporter")
                .containsOnly("Administrators", "Customer", "Developers", "Project Manager", "Quality Assurance", "Reviewer");
    }
}
