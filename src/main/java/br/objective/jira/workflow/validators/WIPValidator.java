package br.objective.jira.workflow.validators;

import static br.objective.jira.workflow.utils.TransitionUtils.getNextStatusNameForAction;

import java.io.IOException;
import java.util.Optional;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.opensymphony.workflow.InvalidInputException;

import br.objective.taskboard.TaskboardConnection;

class WIPValidator {

	public static void validateWIP(Issue issue, int actionId, TaskboardConnection taskboard) throws InvalidInputException, IOException, JSONException {
		if (issue == null)
			throw new IllegalArgumentException("Error on WIP validation: Issue is required.");

		Optional<String> statusName = getNextStatusNameForAction(issue, actionId);
		if (!statusName.isPresent())
			throw new IllegalArgumentException("Error on WIP validation: \"actionId\" "+ actionId +" isn't valid.");

		JSONObject json = taskboard.getWIPValidatorResponse(issue.getKey(), issue.getAssigneeUser().getUsername(), statusName.get());
		if (json.getBoolean("isWipExceeded"))
			throw new InvalidInputException(json.getString("message"));
	}

}
