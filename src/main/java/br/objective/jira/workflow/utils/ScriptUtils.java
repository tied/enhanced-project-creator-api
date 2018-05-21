package br.objective.jira.workflow.utils;

import static br.objective.jira.workflow.utils.TransitionUtils.getNextStatusNameForAction;
import static br.objective.jira.workflow.utils.TransitionUtils.getTransitionActionDescriptor;

import java.util.Optional;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class ScriptUtils {

	public static String getAttributeValueOrNull(ActionDescriptor actDes, String attributeName) {
		Object attribute = actDes.getMetaAttributes().get(attributeName);
		return attribute == null ? null : attribute.toString();
	}

	public static String getErrorMessage(Exception e) {
		return e.getMessage() == null ? e.toString() : e.getMessage();
	}

	public static boolean isIssueOnTheSameStatusAsTransition(Issue issue, String transitionName) {
		Optional<ActionDescriptor> transition = getTransitionActionDescriptor(issue, transitionName);
		if (!transition.isPresent())
			return false;
		Optional<String> transitionStatusName = getNextStatusNameForAction(issue, transition.get().getId());
		if (!transitionStatusName.isPresent())
			return false;

		return issue.getStatus().getName().equals(transitionStatusName.get());
	}

}
