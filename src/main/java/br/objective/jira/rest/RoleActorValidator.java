package br.objective.jira.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;

class RoleActorValidator {

	public static RoleActorsValidatorResult validateRolesAndUsers(ProjectData data) {
		UserManager userManager = ComponentAccessor.getUserManager();
		return validateRolesAndActors(data.userInRoles, "User", (String userName) -> userManager.getUserByKey(userName) != null);
	}

	public static RoleActorsValidatorResult validateRolesAndGroups(ProjectData data) {
		GroupManager groupManager = ComponentAccessor.getGroupManager();
		return validateRolesAndActors(data.groupInRoles, "Group", (String groupName) -> groupManager.groupExists(groupName));
	}

	private static RoleActorsValidatorResult validateRolesAndActors(Map<String, List<String>> actorsInRoles, String actorType, Predicate<String> actorExists) {
		ProjectRoleManager roleManager = ComponentAccessor.getComponentOfType(ProjectRoleManager.class);
		RoleActorsValidatorResult result = new RoleActorsValidatorResult();

		for (Entry<String, List<String>> actorsInRole : actorsInRoles.entrySet()) {
			ProjectRole role = roleManager.getProjectRole(actorsInRole.getKey());
			if (role == null) {
				result.warnings.add("Project role " + actorsInRole.getKey() + " not found.");
				continue;
			}

			List<String> validActors = new ArrayList<>();
			actorsInRole.getValue().forEach(actorName -> {
				if (actorExists.test(actorName))
					validActors.add(actorName);
				else
					result.warnings.add(actorType + " " + actorName + " was not found, therefore it was not added to the project.");
			});

			if (!validActors.isEmpty())
				result.validRoleActors.add(new RoleActors(role, validActors));
		}

		return result;
	}

	static class RoleActorsValidatorResult {
		public List<RoleActors> validRoleActors = new ArrayList<>();
		public List<String> warnings = new ArrayList<>();
	}

	static class RoleActors {
		public ProjectRole role;
		public List<String> actors;
		public RoleActors(ProjectRole role, List<String> actors) {
			this.role = role;
			this.actors = actors;
		}
	}

}
