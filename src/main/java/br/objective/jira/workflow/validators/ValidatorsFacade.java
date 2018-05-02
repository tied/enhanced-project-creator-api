package br.objective.jira.workflow.validators;

import java.io.IOException;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.json.JSONException;
import com.opensymphony.workflow.InvalidInputException;

import br.objective.taskboard.TaskboardConnection;

public class ValidatorsFacade {

	public static void parentValidation(Issue childIssue, String transitionName) throws Throwable {
		if (childIssue == null || childIssue.getParentObject() == null)
			throw new IllegalArgumentException("Error on parentValidation: the \"childIssue\" and the \"parentIssue\" cannot be null.");
		String errorMessage = "The parent issue ("+ childIssue.getParentObject().getKey() +") would be transitioned as a result "+ childIssue.getKey() +"'s transition, but the parent can't be transitioned:";
		IssueTransitionValidator.executeValidation(childIssue.getParentObject(), transitionName, errorMessage);
	}

	public static void assigneeAndWIPValidation(Issue issue, int actionId, String taskboardUser, String taskboardPassword, String taskboardEndpoint) throws InvalidInputException, IOException, JSONException {
		AssigneeValidator.validateAssignee(issue);

		TaskboardConnection taskboard = new TaskboardConnection(taskboardUser, taskboardPassword, taskboardEndpoint);
		WIPValidator.validateWIP(issue, actionId, taskboard);
	}

}
