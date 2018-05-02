package br.objective.jira.workflow.utils;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.TransitionOptions;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class TransitionUtils {

	private static final Logger log = LoggerFactory.getLogger(TransitionUtils.class);

	public static Optional<ActionDescriptor> getTransitionActionDescriptor(Issue issue, String transitionName) {
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issue);

		return jiraWorkflow.getAllActions().stream()
			.filter(actionDescriptor -> transitionName.equals(actionDescriptor.getName()))
			.findFirst();
	}

	public static boolean isValidTransition(Issue issue, ActionDescriptor action, ApplicationUser user) {
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

	public static Optional<String> getNextStatusNameForAction(Issue issue, int actionId) {
		try {
			String statusId = ComponentAccessor.getWorkflowManager().getNextStatusIdForAction(issue, actionId);
			Status status = ComponentAccessor.getConstantsManager().getStatus(statusId);
			return Optional.of(status.getName());
		} catch (Exception e) {
			log.error(e.getMessage());
			return Optional.empty();
		}
	}

}
