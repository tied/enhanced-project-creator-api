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

import br.objective.jira.rest.LoggedUser;

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

	public static void doTransition(Issue issue, String transitionName) {
		ApplicationUser loggedUser = LoggedUser.get();
		IssueService issueService = ComponentAccessor.getIssueService();

		Optional<ActionDescriptor> actionOpt = getTransitionActionDescriptor(issue, transitionName);
		if (!actionOpt.isPresent()) {
			String error = "Error on transition: Transition \""+ transitionName +"\" doesn't exist.";
			log.error(error);
			throw new IllegalArgumentException(error);
		}

		IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(
			loggedUser,
			issue.getId(),
			actionOpt.get().getId(),
			issueService.newIssueInputParameters()
		);
		if(!transitionValidationResult.isValid()) {
			String error = "Error on \""+ transitionName +"\" transition to the issue \""+ issue.getKey() +"\". Error: "+ transitionValidationResult.getErrorCollection();
			log.error(error);
			throw new IllegalArgumentException(error);
		}

		IssueService.IssueResult issueResult = issueService.transition(loggedUser, transitionValidationResult);
		if (!issueResult.isValid()) {
			String error = "Error on \""+ transitionName +"\" transition to the issue \""+ issue.getKey() +"\". Error: "+ issueResult.getErrorCollection();
			log.error(error);
			throw new IllegalArgumentException(error);
		}
	}

}
