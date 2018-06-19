import com.onresolve.scriptrunner.runner.customisers.WithPlugin;

@WithPlugin("br.objective.jira.enhanced-project-creator-api")

import br.objective.jira.workflow.postFunctions.PostFunctionsFacade;
import br.objective.jira.workflow.utils.ScriptUtils;
import br.objective.jira.workflow.utils.TransitionUtils;

String transitionName = "Transition Name";

if (!TransitionUtils.isValidTransition(issue.getParentObject(), transitionName, ScriptUtils.getLoggedUser()))
    return;

try {
    PostFunctionsFacade.doTransition(issue.getParentObject(), transitionName);
} catch (Exception e) {
    log.error("INTERNAL - GS Task Board: Ignoring parent transition to the issue \""+ issue.getKey() +"\". Error: "+  ScriptUtils.getErrorMessage(e));
}
