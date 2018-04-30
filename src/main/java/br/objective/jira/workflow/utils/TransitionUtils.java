package br.objective.jira.workflow.utils;

import java.util.Optional;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.TransitionOptions;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class TransitionUtils {

	public static Optional<ActionDescriptor> getTransitionActionDescriptor(String transitionName, Issue issue) {
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issue);

		return jiraWorkflow.getAllActions().stream()
			.filter(actionDescriptor -> transitionName.equals(actionDescriptor.getName()))
			.findFirst();
	}

	public static boolean isValidTransition(ApplicationUser user, Issue issue, ActionDescriptor action) {
		IssueService issueService = ComponentAccessor.getIssueService();
		IssueService.TransitionValidationResult validationResult = issueService.validateTransition(
			user,
			issue.getId(),
			action.getId(),
			issueService.newIssueInputParameters(),
			TransitionOptions.defaults()
		);
		return validationResult.isValid();
	}

}
