# Enhanced Project Builder

This is a jira plugin that exposes a REST api to create a new project with all required schemes and custom field associations.
This plugin also has some functions to use in Workflow validators, conditions and post-functions.

## Endpoints

### Project creation: /rest/projectbuilder/1.0/project
Expected payload:
```
{
    "key"                      : "Jira project key, no spaces",
    "name"                     : "A project name",
    "description"              : "The project description",
    "projectTypeKey"           : "business",
    "projectTemplateKey"       : "com.atlassian.jira-core-project-templates:jira-core-project-management",
    "lead"                     : "id of the user that will 'lead' the project",
    "userInRoles"              : {
        "Role Name 1"     : ["user1", "user2"],
        "Role Name 2"     : ["user3", "user4"]
    },
    "groupInRoles"              : { // Optional
        "Role Name 2"     : ["group1", "group2"],
        "Role Name 3"     : ["group3", "group4"]
    },
    "issueTypeScheme"          : "id of issue type scheme",
    "workflowScheme"           : "id of workflow scheme",
    "issueTypeScreenScheme"    : "id of issue type screen scheme",
    "fieldConfigurationScheme" : "id of field configuration scheme",
    "notificationScheme"       : "id of notification scheme",
    "permissionScheme"         : "id of permission scheme",
    "customFields"             : [ // An array of customfield id x field configuration scheme id 
                                   // which will be associated with the project
        {"id":10000,"schemeId":10101}, 
        {"id":10001,"schemeId":10111}
    ]
}
```

### List users: /rest/projectbuilder/1.0/users
Returns the list of all jira users. Used to allow external systems to create user pickers.

You may pass a query parameter to search filter results:
```
/rest/projectbuild/1.0/users?q=name
```

### Issue fields required in transitions : /rest/projectbuilder/1.0/issue/{issueKey}/fields-required-in-transitions
Returns the list of all fields required in transitions according to an issue key and a list of transition IDs.

```
/rest/projectbuild/1.0/issue/ISSUE-1/fields-required-in-transitions
```
Payload:
```
[ 11, 21 ]
```
Response:
```
[
    {
        "id": 11,
        "requiredFields": [
            "fixVersions"
        ],
    },
    {
        "id": 21,
        "requiredFields": [],
    }
]
```

## Workflow scripts

To use this plugin in the workflow scripts, you have to put this code:
```
import com.onresolve.scriptrunner.runner.customisers.WithPlugin;
@WithPlugin("br.objective.jira.enhanced-project-creator-api")_
```
After that, you can import the classes that you need.


## Development

To debug the plugin, run:

```
atlas-run
```
-or-
```
mvn clean package
mvn jira:debug
```

And attach the debugger from your IDE to localhost:5005

This project is configured with QuickReload, so you may run `mvn package` from a second terminal and it will update loaded classes.