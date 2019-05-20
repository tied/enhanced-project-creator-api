package br.objective.jira.workflow.postFunctions;

import com.atlassian.jira.issue.Issue;

import br.objective.jira.workflow.utils.TransitionUtils;

public class PostFunctionsFacade {

	public static void doTransition(Issue issue, String transitionName) throws IllegalArgumentException {
		TransitionUtils.doTransition(issue, transitionName);
	}

}
