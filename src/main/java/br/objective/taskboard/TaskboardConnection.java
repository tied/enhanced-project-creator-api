package br.objective.taskboard;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

public class TaskboardConnection {

	private static final Logger log = LoggerFactory.getLogger(TaskboardConnection.class);

	private String user;
	private String password;
	private String endpoint;

	public TaskboardConnection(String user, String password, String endpoint) {
		if (isBlank(user) || isBlank(password) || isBlank(endpoint))
			throw new IllegalArgumentException("All arguments are required.");

		this.user = user;
		this.password = password;
		this.endpoint = endpoint;
	}

	public JSONObject getWIPValidatorResponse(String issueKey, String username, String statusName) throws IOException, JSONException {
		String statusNameEncoded = URLEncoder.encode(statusName, UTF_8.toString());
		String url = endpoint +"/api/wip-validator?issue="+ issueKey +"&user="+ username +"&status="+ statusNameEncoded;
		String response = fetch(new URL(url));
		return new JSONObject(response);
	}

	private String fetch(URL url) throws IOException {
		HttpURLConnection connection = null;
		try {
			connection = getConnection(url);
			String response = getResponse(connection);
			log.info(connection.getResponseCode() +" - "+ connection.getResponseMessage() +" - "+ response);
			return response;
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	private HttpURLConnection getConnection(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Authorization", "Basic " + getAuth());
		return connection;
	}

	private String getResponse(HttpURLConnection connection) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getStream(connection)))) {
			return bufferedReader.lines().collect(Collectors.joining("\n"));
		}
	}

	private InputStream getStream(HttpURLConnection connection) throws IOException {
		InputStream errorStream = connection.getErrorStream();
		return errorStream != null ? errorStream : connection.getInputStream();
	}

	private String getAuth() {
		return Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
	}

}
