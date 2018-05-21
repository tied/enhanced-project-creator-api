package br.objective.jira.workflow.postFunctions;

import com.atlassian.jira.issue.Issue;

import br.objective.jira.workflow.utils.TransitionUtils;

public class PostFunctionsFacade {

	public static void doTransitionOnParent(Issue subtask, String transitionName) throws IllegalArgumentException {
		if (subtask == null || subtask.getParentObject() == null)
			throw new IllegalArgumentException("Error on parentValidation: the \"childIssue\" and the \"parentIssue\" cannot be null.");
		TransitionUtils.doTransition(subtask.getParentObject(), transitionName);
	}

}
