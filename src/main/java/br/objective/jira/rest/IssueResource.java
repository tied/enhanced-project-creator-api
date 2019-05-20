package br.objective.jira.rest;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

@Path("/issue")
public class IssueResource {

    @Path("{issueKey}/fields-required-in-transitions")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fieldsRequiredInTransitions(@PathParam("issueKey") String issueKey, List<Long> transitions) {
        MutableIssue issue = ComponentAccessor.getIssueManager().getIssueByKeyIgnoreCase(issueKey);
        if (issue == null)
            return Response.status(Status.NOT_FOUND).build();

        JiraWorkflow workflow = ComponentAccessor.getWorkflowManager().getWorkflow(issue);

        Map<Long, ActionDescriptor> actionMap = workflow.getAllActions().stream()
                .collect(toMap(a -> (long) a.getId(), a -> a));

        List<FieldsRequiredInTransitionResponse> response = transitions.stream()
                .filter(actionMap::containsKey)
                .map(id -> {
                    ActionDescriptor action = actionMap.get(id);

                    @SuppressWarnings("unchecked")
                    List<String> requiredFields = ((List<ValidatorDescriptor>)action.getValidators()).stream()
                            .filter(v -> v.getArgs() != null)
                            .filter(v -> "com.googlecode.jsu.workflow.validator.FieldsRequiredValidator".equalsIgnoreCase((String) v.getArgs().get("class.name")))
                            .flatMap(v -> Arrays.stream(((String) v.getArgs().get("hidFieldsList")).split("@@")))
                            .collect(toList());

                    return new FieldsRequiredInTransitionResponse(id, requiredFields);
                })
                .collect(toList());
        return Response.ok(response).build();
    }

    static class FieldsRequiredInTransitionResponse {
        public Long id;
        public List<String> requiredFields;

        public FieldsRequiredInTransitionResponse(Long id, List<String> requiredFields) {
            this.id = id;
            this.requiredFields = requiredFields;
        }
    }

}
