package br.objective.jira.workflow.validators;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.workflow.InvalidInputException;

public class ValidatorsFacade {

	public static void parentValidation(String transitionName, Issue childIssue) throws Throwable {
		if (childIssue == null || childIssue.getParentObject() == null)
			throw new InvalidInputException("Error on parentValidation: the \"childIssue\" and the \"parentIssue\" cannot be null.");
		String errorMessage = "The parent issue ("+ childIssue.getParentObject().getKey() +") would be transitioned as a result "+ childIssue.getKey() +"'s transition, but the parent can't be transitioned:";
		IssueTransitionValidator.executeValidation(transitionName, childIssue.getParentObject(), errorMessage);
	}

}
