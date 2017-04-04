package br.objective.jira.rest;

import java.util.List;
import java.util.Map;

public class ProjectData {
	public String name;
	public String key;
	public String description;
	public String projectTypeKey;
	public String projectTemplateKey;
	public String lead;
	public Long permissionScheme;
	public Long issueTypeScreenScheme;
	public Long workflowScheme;
	public Long notificationScheme;
	public Long fieldConfigurationScheme;
	public Long issueTypeScheme;
	public Long [] customFields;
	public Map<String, List<String>> userInRoles;
}
