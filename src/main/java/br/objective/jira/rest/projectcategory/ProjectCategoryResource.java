package br.objective.jira.rest.projectcategory;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.atlassian.jira.component.ComponentAccessor;

@Path("/projectCategory")
public class ProjectCategoryResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ProjectCategoryDTO> search(@DefaultValue("") @QueryParam("q") String q) {
		return ComponentAccessor.getProjectManager().getAllProjectCategories().stream()
			.filter(pc -> pc.getName().toLowerCase().contains(q.toLowerCase()))
			.map(pc -> ProjectCategoryDTO.from(pc))
			.collect(toList());
	}

}
