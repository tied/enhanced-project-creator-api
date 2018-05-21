package br.objective.jira.workflow.conditions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.atlassian.jira.issue.Issue;

class IssuesCondition {

	public static boolean isIssuesOnStatusCategory(Collection<Issue> issues, String statusCategory, String... issueTypeIdsToFilter) {
		Stream<Issue> issuesToSearch;
		if (issueTypeIdsToFilter != null && issueTypeIdsToFilter.length > 0) {
			List<String> issueTypeIdsList = Arrays.asList(issueTypeIdsToFilter);
			issuesToSearch = issues.stream()
				.filter(issue -> issueTypeIdsList.contains(issue.getIssueTypeId()));
		} else {
			issuesToSearch = issues.stream();
		}

		return issuesToSearch.allMatch(issue -> issue.getStatus().getStatusCategory().getKey().equals(statusCategory));
	}

}
