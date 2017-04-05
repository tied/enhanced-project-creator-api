package br.objective.jira.rest;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;

@Path("/users")
public class UsersResource {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<UserData> listUsers() {
		ApplicationUser loggedUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		JiraServiceContext jsc = new JiraServiceContextImpl(loggedUser);
		UserSearchService userSearchService = ComponentAccessor.getComponent(UserSearchService.class);
		List<ApplicationUser> allUsers = userSearchService.findUsersAllowEmptyQuery(jsc, "");
		List<UserData> result = new LinkedList<UserData>();	
		for (ApplicationUser user : allUsers) 
			result.add(UserData.fromJiraUser(user));
		
		return result;
	}
}
