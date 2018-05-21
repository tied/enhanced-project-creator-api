package br.objective.jira.workflow.validators;

import static br.objective.jira.workflow.utils.TransitionUtils.getTransitionActionDescriptor;
import static br.objective.jira.workflow.utils.TransitionUtils.isValidTransition;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.AbstractWorkflow;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import br.objective.jira.rest.LoggedUser;

class IssueTransitionValidator {

	private static final Logger log = LoggerFactory.getLogger(IssueTransitionValidator.class);

	public static void executeValidation(Issue issue, String transitionName, String messageBeforeError) throws Throwable {
		try {
			executeValidation(issue, transitionName);
		} catch (InvalidInputException e) {
			throw new InvalidInputException(messageBeforeError +" "+ e.getMessage());
		}
	}

	public static void executeValidation(Issue issue, String transitionName) throws Throwable {
		if (isBlank(transitionName)) {
			String error = "Error on validation: \"transitionName\" is required.";
			log.error(error);
			throw new IllegalArgumentException(error);
		}

		if (issue == null) {
			String error = "Error on \""+ transitionName +"\" validation. The issue is required.";
			log.error(error);
			throw new IllegalArgumentException(error);
		}

		Optional<ActionDescriptor> actionOpt = getTransitionActionDescriptor(issue, transitionName);
		if (!actionOpt.isPresent()) {
			String error = "Error on validation: Transition \""+ transitionName +"\" doesn't exist.";
			log.error(error);
			throw new IllegalArgumentException(error);
		}
		ActionDescriptor action = actionOpt.get();

		if (!isValidTransition(issue, action, LoggedUser.get()))
			throw new InvalidInputException("Transition \""+ transitionName +"\" isn't valid for the current state of the issue \""+ issue.getKey() +"\".");

		if (action.getValidators().isEmpty())
			return;

		Workflow workflow = ComponentAccessor.getWorkflowManager().makeWorkflow(LoggedUser.get());
		if (!(workflow instanceof AbstractWorkflow)) {
			String error = "Error on \""+ transitionName +"\" validation to the issue \""+ issue.getKey() +"\". Error: Workflow isn't instance of AbstractWorkflow.";
			log.error(error);
			throw new ClassCastException(error);
		}

		try {
			validate(issue, action, (AbstractWorkflow) workflow);
		} catch (WorkflowValidationReflectionException e) {
			String error = "Error on \""+ transitionName +"\" validation to the issue \""+ issue.getKey() +"\". Error: " + e.getMessage();
			log.error(error);
			throw new WorkflowValidationReflectionException(error, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void validate(Issue issue, ActionDescriptor action, AbstractWorkflow workflow) throws Throwable {
		WorkflowStore store = workflow.getConfiguration().getWorkflowStore();
		WorkflowEntry entry = store.findEntry(issue.getWorkflowId());
		PropertySet ps = store.getPropertySet(issue.getWorkflowId());
		Map<Object, Object> transientVars = generateTransientVars(issue, action, workflow, store, entry);

		executeVerifyInputs(workflow, entry, action.getValidators(), Collections.unmodifiableMap(transientVars), ps);
	}

	private static Map<Object, Object> generateTransientVars(Issue issue, ActionDescriptor action, AbstractWorkflow workflow, WorkflowStore store, WorkflowEntry entry) throws FactoryException, StoreException {
		Map<Object, Object> transientVars = new HashMap<>();
		transientVars.put("issue", issue);
		transientVars.put("actionId", action.getId());
		transientVars.put("descriptor", workflow.getConfiguration().getWorkflow(entry.getWorkflowName()));
		transientVars.put("context", getContext(workflow));
		transientVars.put("configuration", workflow.getConfiguration());
		transientVars.put("store", store);
		transientVars.put("currentSteps", store.findCurrentSteps(issue.getWorkflowId()));
		transientVars.put("entry", entry);
		return transientVars;
	}

	private static WorkflowContext getContext(AbstractWorkflow workflow) throws WorkflowValidationReflectionException {
		try {
			Field contextField = AbstractWorkflow.class.getDeclaredField("context");
			contextField.setAccessible(true);
			return (WorkflowContext) contextField.get(workflow);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new WorkflowValidationReflectionException("Error accessing \"context\" field by reflection.", e);
		}
	}

	private static void executeVerifyInputs(AbstractWorkflow workflow, WorkflowEntry entry, List<ValidatorDescriptor> validators, Map<Object, Object> transientVars, PropertySet ps) throws Throwable {
		try {
			Method verifyInputsMethod = AbstractWorkflow.class.getDeclaredMethod("verifyInputs", WorkflowEntry.class, List.class, Map.class, PropertySet.class);
			verifyInputsMethod.setAccessible(true);
			verifyInputsMethod.invoke(workflow, entry, validators, transientVars, ps);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
			throw new WorkflowValidationReflectionException("Error invoking \"verifyInputs\" method by reflection.", e);
		}
	}

	private static class WorkflowValidationReflectionException extends RuntimeException {
		private static final long serialVersionUID = -5425038284430396434L;
		public WorkflowValidationReflectionException(String message, Exception e) {
			super(message, e);
		}
	}

}
