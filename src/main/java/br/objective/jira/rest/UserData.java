package br.objective.jira.rest;

import com.atlassian.jira.user.ApplicationUser;

public class UserData {
	public String displayName;
	public String emailAddress;
	public String name;

	public static UserData fromJiraUser(ApplicationUser user) {
		UserData u = new UserData();
		u.name = user.getName();
		u.displayName = user.getDisplayName();
		u.emailAddress= user.getEmailAddress();
		return u;
	}
}
