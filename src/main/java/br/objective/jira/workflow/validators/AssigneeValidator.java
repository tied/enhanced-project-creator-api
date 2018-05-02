package br.objective.jira.workflow.validators;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.workflow.InvalidInputException;

class AssigneeValidator {

	public static void validateAssignee(Issue issue) throws InvalidInputException {
		if (issue == null)
			throw new IllegalArgumentException("Issue is required.");

		ApplicationUser assignee = issue.getAssignee();
		if (assignee == null || isBlank(assignee.getUsername()))
			throw new InvalidInputException("Assignee is required.");
	}

}
