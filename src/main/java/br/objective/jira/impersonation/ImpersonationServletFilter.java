package br.objective.jira.impersonation;

import static com.atlassian.jira.component.ComponentAccessor.getGlobalPermissionManager;
import static com.atlassian.jira.component.ComponentAccessor.getJiraAuthenticationContext;
import static com.atlassian.jira.component.ComponentAccessor.getUserManager;
import static com.atlassian.jira.permission.GlobalPermissionKey.ADMINISTER;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

public class ImpersonationServletFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(ImpersonationServletFilter.class);

	public static final String IMPERSONATE_HEADER = "obj-impersonate";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		JiraAuthenticationContext authContext = getJiraAuthenticationContext();
		ApplicationUser origUser = authContext.getLoggedInUser();

		Optional<String> impersonateUsername = getImpersonateUsername(request);
		boolean impersonated = false;

		try {
			if (canImpersonate(origUser, impersonateUsername) && impersonateUsername.isPresent())
				impersonated = impersonate(authContext, origUser, impersonateUsername.get());

			chain.doFilter(request, response);
		} finally {
			if (impersonated)
				authContext.setLoggedInUser(origUser);
		}
	}

	private boolean canImpersonate(ApplicationUser origUser, Optional<String> impersonateUsername) throws IOException, ServletException {
		boolean canImpersonate = hasPermissionToImpersonate(origUser);
		if (!canImpersonate && impersonateUsername.isPresent())
			log.warn("User \""+ determineUsername(origUser) +"\" tried to impersonate \""+ impersonateUsername.get() +"\".");

		return canImpersonate;
	}

	private boolean hasPermissionToImpersonate(ApplicationUser user) {
		return user != null && getGlobalPermissionManager().hasPermission(ADMINISTER, user);
	}

	private Optional<String> getImpersonateUsername(ServletRequest request) {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		return Optional.ofNullable(httpServletRequest.getHeader(IMPERSONATE_HEADER));
	}

	private String determineUsername(ApplicationUser user) {
		return user == null ? "Non logged in user" : user.getUsername();
	}

	private boolean impersonate(JiraAuthenticationContext authContext, ApplicationUser origUser, String impersonateUsername) {
		ApplicationUser impersonateUser = getUserManager().getUserByName(impersonateUsername);

		if (impersonateUsername.equals(origUser.getUsername())) {
			log.info("User \""+ origUser.getUsername() +"\" tried to impersonate himself. Skipping.");
			return false;
		} else if (isImpersonable(impersonateUser)) {
			authContext.setLoggedInUser(impersonateUser);
			log.info("User \""+ origUser.getUsername() +"\" impersonated as \""+ impersonateUsername +"\".");
			return true;
		} else {
			log.info("User \""+ origUser.getUsername() +"\" tried to use impersonate, but the user \""+ impersonateUsername +"\" is invalid.");
			return false;
		}
	}

	private boolean isImpersonable(ApplicationUser user) {
		return user != null && user.isActive();
	}

}
