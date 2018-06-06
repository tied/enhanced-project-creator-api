/**
 * Properties:
 * @string (required) "taskboard-user": Taskboard user - username.
 * @string (required) "taskboard-password": Taskboard user - password.
 * @string (required) "taskboard-endpoint": Taskboard URL.
 */

import com.atlassian.jira.workflow.ImmutableWorkflowDescriptor;

import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import com.opensymphony.workflow.InvalidInputException;

@WithPlugin("br.objective.jira.enhanced-project-creator-api")

import br.objective.jira.workflow.validators.ValidatorsFacade;
import br.objective.jira.workflow.utils.ScriptUtils;

int actionId = transientVars.get("actionId");
ImmutableWorkflowDescriptor wdes = transientVars.get("descriptor");
String user = ScriptUtils.getAttributeValueOrNull(wdes.getAction(actionId), "taskboard-user");
String password = ScriptUtils.getAttributeValueOrNull(wdes.getAction(actionId), "taskboard-password");
String endpoint = ScriptUtils.getAttributeValueOrNull(wdes.getAction(actionId), "taskboard-endpoint");

try {
    ValidatorsFacade.assigneeAndWIPValidation(issue, actionId, user, password, endpoint);
} catch (InvalidInputException e) {
    invalidInputException = e;
} catch (Exception e) {
    log.error("INTERNAL - GS Task Board: Ignoring WIP validation to the issue \""+ issue.getKey() +"\". Error: "+ ScriptUtils.getErrorMessage(e));
}
