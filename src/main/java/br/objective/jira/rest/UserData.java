package br.objective.jira.rest;

import com.atlassian.jira.user.ApplicationUser;

public class UserData {
	public String name;
	public String email;
	public String id;

	public static UserData fromJiraUser(ApplicationUser user) {
		UserData u = new UserData();
		u.id = user.getName();
		u.name = user.getDisplayName();
		u.email= user.getEmailAddress();
		return u;
	}
}
