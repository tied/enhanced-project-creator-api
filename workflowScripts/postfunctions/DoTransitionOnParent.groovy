import com.onresolve.scriptrunner.runner.customisers.WithPlugin;

@WithPlugin("br.objective.jira.enhanced-project-creator-api")

import br.objective.jira.workflow.postFunctions.PostFunctionsFacade;
import br.objective.jira.workflow.utils.ScriptUtils;

String transitionName = "Transition Name";

if (ScriptUtils.isIssueOnTheSameStatusAsTransition(issue.getParentObject(), transitionName))
    return;

try {
    PostFunctionsFacade.doTransitionOnParent(issue, transitionName);
} catch (Exception e) {
    log.error("INTERNAL - GS Task Board: Ignoring parent transition to the issue \""+ issue.getKey() +"\". Error: "+  ScriptUtils.getErrorMessage(e));
}
