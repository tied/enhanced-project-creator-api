package br.objective.jira.workflow.conditions;

import com.atlassian.jira.issue.Issue;

public class ConditionsFacade {

	public static boolean isSubtasksOnStatusCategory(Issue parentIssue, String statusCategory, String[] issueTypeIdsToFilter) {
		return IssuesCondition.isIssuesOnStatusCategory(parentIssue.getSubTaskObjects(), statusCategory, issueTypeIdsToFilter);
	}

}
