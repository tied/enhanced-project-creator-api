package it.br.objective.jira;

import static it.br.objective.jira.JiraTestUtils.jsonResource;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.wink.client.Resource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProjectCategoryResourceIT {

	@BeforeClass
	public static void setup() {
		projectCategory()
			.withName("CREATED")
			.withDescription("Created Project Category")
		.create();
	}

	@Test
	public void givenNoQueryParam_whenGetProjectCategory_thenShouldReturnAllProjectCategories() throws JSONException {
		projectCategoryResource()
			.withNoQueryParam()
		.get()
		.assertLength(1)
			.assertProjectCategory(0, "CREATED");
	}

	@Test
	public void givenEmptyQueryParam_whenGetProjectCategory_thenShouldReturnAllProjectCategories() throws JSONException {
		projectCategoryResource()
			.withQueryParam("")
		.get()
		.assertLength(1)
			.assertProjectCategory(0, "CREATED");
	}

	@Test
	public void givenNonExistentQueryParam_whenGetProjectCategory_thenShouldReturnNoProjectCategory() throws JSONException {
		projectCategoryResource()
			.withQueryParam("nonexistent")
		.get()
		.assertIsEmpty();
	}

	@Test
	public void givenExistentQueryParam_whenGetProjectCategory_thenShouldReturnProjectCategory() throws JSONException {
		projectCategoryResource()
			.withQueryParam("creat")
		.get()
		.assertLength(1)
			.assertProjectCategory(0, "CREATED");
	}

	private static DSLProjectCategoryCreator projectCategory() {
		return new DSLProjectCategoryCreator();
	}

	private static DSLProjectCategoryResource projectCategoryResource() {
		return new DSLProjectCategoryResource();
	}

	private static class DSLProjectCategoryCreator {

		private String name;
		private String description;

		private DSLProjectCategoryCreator withName(String name) {
			this.name = name;
			return this;
		}

		private DSLProjectCategoryCreator withDescription(String description) {
			this.description = description;
			return this;
		}

		private void create() {
			JiraTestUtils.createProjectCategory(name, description);
		}
	}

	private static class DSLProjectCategoryResource {
		private String queryParam = "";

		private DSLProjectCategoryResource withNoQueryParam() {
			this.queryParam = "";
			return this;
		}

		private DSLProjectCategoryResource withQueryParam(String queryParam) {
			this.queryParam = "?q=" + queryParam;
			return this;
		}

		private DSLProjectCategoryResourceAsserter get() throws JSONException {
			Resource resource = jsonResource("http://localhost:2990/jira/rest/projectbuilder/1.0/projectCategory" + queryParam);
			JSONArray response = new JSONArray(resource.get(String.class));
			return new DSLProjectCategoryResourceAsserter(response);
		}
	}

	private static class DSLProjectCategoryResourceAsserter {
		private final JSONArray response;
		
		private DSLProjectCategoryResourceAsserter() {
			this.response = new JSONArray();
		}

		private DSLProjectCategoryResourceAsserter(JSONArray response) {
			this.response = response;
		}

		private DSLProjectCategoryResourceAsserter assertIsEmpty() {
			assertLength(0);
			return this;
		}

		private DSLProjectCategoryResourceAsserter assertLength(Integer length) {
			assertThat(response.length(), equalTo(length));
			return this;
		}

		private DSLProjectCategoryResourceAsserter assertProjectCategory(Integer index, String name) throws JSONException {
			assertThat(response.getJSONObject(index).getString("name"), equalTo(name));
			return this;
		}
	}

}
