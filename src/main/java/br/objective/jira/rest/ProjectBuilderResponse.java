package br.objective.jira.rest;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ProjectBuilderResponse implements Serializable{
	private static final long serialVersionUID = 2643704078232781352L;
	public boolean success = true;
	public Long idOfCreatedProject= null;
	public List<String> errors = new LinkedList<>();

	public ProjectBuilderResponse withError(String errorMessage) {
		success = false;
		errors.add(errorMessage);
		return this;
	}

	public ProjectBuilderResponse withError(String errorMessage, Exception e) {
		success = false;
		errors.add(errorMessage + ". " + e.getMessage());
		e.printStackTrace();
		return this;
	}
}