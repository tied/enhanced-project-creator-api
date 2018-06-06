package br.objective.jira.workflow.validators;

import java.io.IOException;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.json.JSONException;
import com.opensymphony.workflow.InvalidInputException;

import br.objective.taskboard.TaskboardConnection;

public class ValidatorsFacade {

	public static void parentTransitionValidation(Issue subtask, String transitionName) throws Throwable {
		if (subtask == null || subtask.getParentObject() == null)
			throw new IllegalArgumentException("Error on parentValidation: the \"subtask\" and the \"parentIssue\" cannot be null.");
		String errorMessage = "The parent issue ("+ subtask.getParentObject().getKey() +") would be transitioned as a result "+ subtask.getKey() +"'s transition, but the parent can't be transitioned:";
		IssueTransitionValidator.executeValidators(subtask.getParentObject(), transitionName, errorMessage);
	}

	public static void assigneeAndWIPValidation(Issue issue, int actionId, String taskboardUser, String taskboardPassword, String taskboardEndpoint) throws InvalidInputException, IOException, JSONException {
		AssigneeValidator.assigneeNotEmpty(issue);

		TaskboardConnection taskboard = new TaskboardConnection(taskboardUser, taskboardPassword, taskboardEndpoint);
		WIPValidator.wip(issue, actionId, taskboard);
	}

}
