import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
import com.atlassian.jira.issue.status.category.StatusCategory;

@WithPlugin("br.objective.jira.enhanced-project-creator-api")_

import br.objective.jira.workflow.conditions.ConditionsFacade;
import br.objective.jira.workflow.utils.ScriptUtils;

String[] issueTypeIds = {"1", "2"};
String statusCategory = StatusCategory.COMPLETE;

try {
    passesCondition = ConditionsFacade.isSubtasksOnStatusCategory(issue, statusCategory, issueTypeIds);
} catch (Exception e) {
    log.error("INTERNAL - GS Task Board: Ignoring Subtasks Condition to the issue \""+ issue.getKey() +"\". Error: "+ ScriptUtils.getErrorMessage(e));
    passesCondition = true;
}
