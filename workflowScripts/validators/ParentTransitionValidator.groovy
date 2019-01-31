import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import com.opensymphony.workflow.InvalidInputException;

@WithPlugin("br.objective.jira.enhanced-project-creator-api")_

import br.objective.jira.workflow.validators.ValidatorsFacade;
import br.objective.jira.workflow.utils.ScriptUtils;

String transitionName = "Transition Name";

if (TransitionUtils.isIssueOnTheSameStatusAsTransition(issue.getParentObject(), transitionName))
    return;

try {
    ValidatorsFacade.parentTransitionValidation(issue, transitionName);
} catch (InvalidInputException e) {
    invalidInputException = e;
} catch (Exception e) {
    log.error("INTERNAL - GS Task Board: Ignoring parent validation to the issue \""+ issue.getKey() +"\". Error: "+  ScriptUtils.getErrorMessage(e));
}
