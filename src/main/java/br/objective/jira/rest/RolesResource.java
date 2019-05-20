package br.objective.jira.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;

@Path("/roles")
public class RolesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listRoles() {
        ProjectRoleManager roleManager = ComponentAccessor.getComponentOfType(ProjectRoleManager.class);
        ProjectService projectService = ComponentAccessor.getComponentOfType(ProjectService.class);

        List<ProjectRole> allRoles = new ArrayList<>(roleManager.getProjectRoles());

        ServiceOutcome<List<Project>> result = projectService.getAllProjectsForAction(LoggedUser.get(), ProjectAction.VIEW_PROJECT);
        if(!result.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(result.getErrorCollection()).build();
        }
        List<Project> visibleProjects = result.get();

        List<RoleData> visibleRoles = visibleProjects.stream()
                .flatMap(project -> allRoles.stream()
                        .filter(role -> {
                            ProjectRoleActors arg = roleManager.getProjectRoleActors(role, project);
                            return !arg.getApplicationUsers().isEmpty();
                        })
                )
                .map(RoleData::from)
                .distinct()
                .collect(Collectors.toList());

        return Response.ok(visibleRoles).build();
    }
}
