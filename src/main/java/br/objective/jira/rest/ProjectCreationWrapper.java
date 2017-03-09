package br.objective.jira.rest;

import com.atlassian.jira.bc.project.ProjectCreationData;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

public class ProjectCreationWrapper {
	static Project createBasicProject(ProjectData data, ProjectBuilderResponse response, ApplicationUser lead) {
		ProjectCreationData projectData = new ProjectCreationData.Builder()
				.withName(data.name)
				.withKey(data.key)
				.withType(data.projectTypeKey)
				.withProjectTemplateKey(data.projectTemplateKey)
				.withDescription(data.description)
				.withLead(lead)
				.withAssigneeType(AssigneeTypes.PROJECT_LEAD)
				.build();
		
	    Project newProject = ComponentAccessor.getProjectManager().createProject(
	    		ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), 
	    		projectData);
	    
	    response.idOfCreatedProject = newProject.getId();
		return newProject;
	}
}
