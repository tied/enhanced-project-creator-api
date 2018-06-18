package br.objective.jira.workflow.utils;

import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.workflow.loader.ActionDescriptor;

import br.objective.jira.rest.LoggedUser;

public class ScriptUtils {

	public static String getAttributeValueOrNull(ActionDescriptor actDes, String attributeName) {
		Object attribute = actDes.getMetaAttributes().get(attributeName);
		return attribute == null ? null : attribute.toString();
	}

	public static String getErrorMessage(Exception e) {
		return e.getMessage() == null ? e.toString() : e.getMessage();
	}

	public static ApplicationUser getLoggedUser() {
		return LoggedUser.get();
	}

}
