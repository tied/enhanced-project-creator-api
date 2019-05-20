package br.objective.jira.filter;

import static br.objective.jira.impersonation.ImpersonationServletFilter.IMPERSONATE_HEADER;
import static com.atlassian.jira.permission.GlobalPermissionKey.ADMINISTER;
import static junit.framework.Assert.assertEquals;
import static org.apache.log4j.lf5.LogLevel.INFO;
import static org.apache.log4j.lf5.LogLevel.WARN;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.lf5.LogLevel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import br.objective.jira.impersonation.ImpersonationServletFilter;
@RunWith(PowerMockRunner.class)
@PrepareForTest({ComponentAccessor.class, LoggerFactory.class})
public class ImpersonationServletFilterTest {

	@Test
	public void givenAdminUser_whenImpersionateValidUser_thenImpersionate() {
		given()
			.loggedUser("admin.user").withAdminPermission()
			.impersonating("existent.active.user").exists().isActive()

		.whenImpersonate()

		.then()
			.assertUserImpersonated()
			.assertMessageWasLogged(INFO);
	}

	@Test
	public void givenNonAdminUser_whenImpersionateValidUser_thenDontImpersionate() {
		given()
			.loggedUser("ordinary.user").withoutAdminPermission()
			.impersonating("existent.active.user").exists().isActive()

		.whenImpersonate()

		.then()
			.assertUserDidntImpersonate()
			.assertMessageWasLogged(WARN);
	}

	@Test
	public void givenNonLoggedUser_whenImpersionateValidUser_thenDontImpersionate() {
		given()
			.nonLoggedUser()
			.impersonating("existent.active.user").exists().isActive()

		.whenImpersonate()

		.then()
			.assertUserDidntImpersonate()
			.assertMessageWasLogged(WARN);
	}

	@Test
	public void givenAdminUser_whenImpersionateInexistentUser_thenDontImpersionate() {
		given()
			.loggedUser("admin.user").withAdminPermission()
			.impersonating("inexistent.user").doesntExists()

		.whenImpersonate()

		.then()
			.assertUserDidntImpersonate()
			.assertMessageWasLogged(INFO);
	}

	@Test
	public void givenAdminUser_whenImpersionateInactiveUser_thenDontImpersionate() {
		given()
			.loggedUser("admin.user").withAdminPermission()
			.impersonating("inexistent.active.user").exists().isInactive()

		.whenImpersonate()

		.then()
			.assertUserDidntImpersonate()
			.assertMessageWasLogged(INFO);
	}

	@Test
	public void givenAdminUser_whenImpersionateHimself_thenDontImpersionate() {
		given()
			.loggedUser("admin.user").withAdminPermission()
			.impersonating("admin.user").exists().isActive()

		.whenImpersonate()

		.then()
			.assertUserDidntImpersonate()
			.assertMessageWasLogged(INFO);
	}

	private ImpersonationServletFilterTestDSL given() {
		return new ImpersonationServletFilterTestDSL();
	}

	private static class ImpersonationServletFilterTestDSL {

		private HttpServletRequest request = mock(HttpServletRequest.class);
		private FilterChain chain = mock(FilterChain.class);
		private ServletResponse response = mock(ServletResponse.class);

		private final GlobalPermissionManager permissionManagerMock = mock(GlobalPermissionManager.class);
		private final JiraAuthenticationContext authContext = mock(JiraAuthenticationContext.class);
		private final UserManager userManagerMock = mock(UserManager.class);

		private ApplicationUser origUserMock;
		private ApplicationUser impersonateUserMock;

		private static final Logger log = mock(Logger.class);

		private final ImpersonationServletFilter subject;

		public ImpersonationServletFilterTestDSL() {
			mockStatic(ComponentAccessor.class);
			when(ComponentAccessor.getGlobalPermissionManager()).thenReturn(permissionManagerMock);
			when(ComponentAccessor.getJiraAuthenticationContext()).thenReturn(authContext);
			when(ComponentAccessor.getUserManager()).thenReturn(userManagerMock);

			mockStatic(LoggerFactory.class);
			reset(log);
			when(LoggerFactory.getLogger(any(Class.class))).thenReturn(log);

			subject = new ImpersonationServletFilter();
		}

		public ImpersonationServletFilterTestDSLLoggedUser loggedUser(String username) {
			return new ImpersonationServletFilterTestDSLLoggedUser().loggedUser(username);
		}

		public ImpersonationServletFilterTestDSLLoggedUser nonLoggedUser() {
			return new ImpersonationServletFilterTestDSLLoggedUser().nonLoggedUser();
		}

		private ImpersonationServletFilterTestDSLImpersonate impersonating(String username) {
			return new ImpersonationServletFilterTestDSLImpersonate(username);
		}

		private ImpersonationServletFilterTestDSL whenImpersonate() {
			try {
				subject.doFilter(request, response, chain);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			return this;
		}

		public ImpersonationServletFilterTestDSLAsserter then() {
			return new ImpersonationServletFilterTestDSLAsserter();
		}

		private class ImpersonationServletFilterTestDSLLoggedUser {

			public ImpersonationServletFilterTestDSLLoggedUser loggedUser(String username) {
				origUserMock = mock(ApplicationUser.class);
				when(origUserMock.getUsername()).thenReturn(username);
				when(authContext.getLoggedInUser()).thenReturn(origUserMock);
				return this;
			}

			public ImpersonationServletFilterTestDSLLoggedUser nonLoggedUser() {
				when(authContext.getLoggedInUser()).thenReturn(null);
				return this;
			}

			public ImpersonationServletFilterTestDSLLoggedUser withAdminPermission() {
				when(permissionManagerMock.hasPermission(ADMINISTER, origUserMock)).thenReturn(true);
				return this;
			}

			public ImpersonationServletFilterTestDSLLoggedUser withoutAdminPermission() {
				when(permissionManagerMock.hasPermission(ADMINISTER, origUserMock)).thenReturn(false);
				return this;
			}


			public ImpersonationServletFilterTestDSLImpersonate impersonating(String username) {
				return ImpersonationServletFilterTestDSL.this.impersonating(username);
			}

		}

		private class ImpersonationServletFilterTestDSLImpersonate {

			public ImpersonationServletFilterTestDSLImpersonate(String username) {
				when(request.getHeader(IMPERSONATE_HEADER)).thenReturn(username);
				impersonateUserMock = mock(ApplicationUser.class);
				when(impersonateUserMock.getUsername()).thenReturn(username);
			}

			public ImpersonationServletFilterTestDSLImpersonate exists() {
				when(userManagerMock.getUserByName(impersonateUserMock.getUsername())).thenReturn(impersonateUserMock);
				return this;
			}

			public ImpersonationServletFilterTestDSLImpersonate doesntExists() {
				when(userManagerMock.getUserByName(impersonateUserMock.getUsername())).thenReturn(null);
				return this;
			}

			public ImpersonationServletFilterTestDSLImpersonate isActive() {
				when(impersonateUserMock.isActive()).thenReturn(true);
				return this;
			}

			public ImpersonationServletFilterTestDSLImpersonate isInactive() {
				when(impersonateUserMock.isActive()).thenReturn(false);
				return this;
			}

			public ImpersonationServletFilterTestDSL whenImpersonate() {
				return ImpersonationServletFilterTestDSL.this.whenImpersonate();
			}

		}

		private class ImpersonationServletFilterTestDSLAsserter {

			public ImpersonationServletFilterTestDSLAsserter() {
				assertDoFilterWasExecuted();
			}

			private void assertDoFilterWasExecuted() {
				try {
					verify(chain, times(1)).doFilter(request, response);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}

			public ImpersonationServletFilterTestDSLAsserter assertUserImpersonated() {
				ArgumentCaptor<ApplicationUser> userArguments = ArgumentCaptor.forClass(ApplicationUser.class);
				verify(authContext, times(2)).setLoggedInUser(userArguments.capture());
				assertEquals("Must impersonate the right user.",  impersonateUserMock.getUsername(), userArguments.getAllValues().get(0).getUsername());
				assertEquals("Must set original logged user after execution.", origUserMock.getUsername(), userArguments.getAllValues().get(1).getUsername());
				return this;
			}

			public ImpersonationServletFilterTestDSLAsserter assertUserDidntImpersonate() {
				verify(authContext, never()).setLoggedInUser(any());
				return this;
			}

			public void assertMessageWasLogged(LogLevel level) {
				if (WARN.equals(level))
					verify(log, times(1)).warn(anyString());
				else if (INFO.equals(level))
					verify(log, times(1)).info(anyString());
				else
					throw new IllegalArgumentException("Unsupported level.");
			}
		}
	}

}
