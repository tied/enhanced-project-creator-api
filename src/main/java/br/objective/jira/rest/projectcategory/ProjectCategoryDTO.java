package br.objective.jira.rest.projectcategory;

import com.atlassian.jira.project.ProjectCategory;

public class ProjectCategoryDTO {
	public String id;
	public String name;

	public ProjectCategoryDTO(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public static ProjectCategoryDTO from(ProjectCategory projectCategory) {
		return new ProjectCategoryDTO(projectCategory.getId().toString(), projectCategory.getName());
	}

}
