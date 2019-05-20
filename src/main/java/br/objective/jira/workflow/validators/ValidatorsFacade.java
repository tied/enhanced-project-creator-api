package br.objective.jira.workflow.validators;

import java.io.IOException;
import java.util.Optional;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.json.JSONException;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.loader.ActionDescriptor;

import br.objective.jira.workflow.utils.TransitionUtils;
import br.objective.taskboard.TaskboardConnection;

public class ValidatorsFacade {

	public static void parentTransitionValidation(Issue subtask, String transitionName) throws Throwable {
		parentObjectNotNullValidation(subtask);
		IssueTransitionValidator.executeValidators(subtask.getParentObject(), transitionName, parentTransitionErrorMessage(subtask));
	}

	public static void parentWIPValidation(Issue subtask, String transitionName, String taskboardUser, String taskboardPassword, String taskboardEndpoint) throws InvalidInputException, IOException, JSONException {
		parentObjectNotNullValidation(subtask);
		Optional<ActionDescriptor> transitionActionDescriptor = TransitionUtils.getTransitionActionDescriptor(subtask.getParentObject(), transitionName);
		if (!transitionActionDescriptor.isPresent())
			throw new IllegalArgumentException("No ActionDescriptor found with the \""+ transitionName +"\" name.");

		TaskboardConnection taskboard = new TaskboardConnection(taskboardUser, taskboardPassword, taskboardEndpoint);
		WIPValidator.wip(subtask.getParentObject(), transitionActionDescriptor.get().getId(), taskboard, parentTransitionErrorMessage(subtask));
	}

	public static void wipValidation(Issue issue, int actionId, String taskboardUser, String taskboardPassword, String taskboardEndpoint) throws InvalidInputException, IOException, JSONException {
		TaskboardConnection taskboard = new TaskboardConnection(taskboardUser, taskboardPassword, taskboardEndpoint);
		WIPValidator.wip(issue, actionId, taskboard);
	}

	private static void parentObjectNotNullValidation(Issue subtask) {
		if (subtask == null || subtask.getParentObject() == null)
			throw new IllegalArgumentException("Error on parentValidation: the \"subtask\" and the \"parentIssue\" cannot be null.");
	}

	private static String parentTransitionErrorMessage(Issue subtask) {
		return "The parent issue ("+ subtask.getParentObject().getKey() +") would be transitioned as a result "+ subtask.getKey() +"'s transition, but the parent can't be transitioned:";
	}

}
