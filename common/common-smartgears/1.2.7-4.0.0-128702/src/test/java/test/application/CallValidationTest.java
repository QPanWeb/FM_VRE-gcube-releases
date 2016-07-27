package test.application;

import static app.Request.*;
import static junit.framework.Assert.*;
import static org.gcube.smartgears.handlers.application.request.RequestError.*;
import static org.gcube.smartgears.lifecycle.application.ApplicationState.*;
import static utils.TestUtils.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.smartgears.Constants;
import org.gcube.smartgears.context.application.ApplicationContext;
import org.gcube.smartgears.handlers.application.lifecycle.ProfileManager;
import org.gcube.smartgears.handlers.application.request.RequestValidator;
import org.junit.Test;

import app.SomeApp;

import com.sun.jersey.api.client.UniformInterfaceException;

public class CallValidationTest {


	@Test
	public void rejectsCallsWhenServiceIsInactive() {
		
		SomeApp app = new SomeApp();
		
		app.handlers().set(new ProfileManager()).set(new RequestValidator());
		
		
		Runnable test = new Runnable() {
			
			@Override
			public void run() {
				fail("call should have been rejected");
			}
		};
		
		ApplicationContext context = app.startWith(test);
		
		context.lifecycle().moveTo(stopped);
		
		try {
			app.send(request());
		}
		catch(UniformInterfaceException e) {
			assertEquals(application_unavailable_error.code(), e.getResponse().getStatus());
		}
		
		
		context.lifecycle().moveTo(failed);
		
		try {
			app.send(request());
		}
		catch(UniformInterfaceException e) {
			assertEquals(application_failed_error.code(), e.getResponse().getStatus());
			
		}
		
	}
	
	@Test
	public void rejectsCallsWithoutScope() throws Throwable {
		
		SomeApp app = new SomeApp();
		
		app.handlers().set(new ProfileManager()).set(new RequestValidator());
		
		app.start();
		
		try {
			app.send(request().inScope(null));; //call in no scope
			fail();
		}
		catch(UniformInterfaceException e) {
			assertEquals(request_not_authorized_error.code(), e.getResponse().getStatus());
		}
		
	}
	
	@Test
	public void rejectsCallsWithBadScope() throws Throwable {
		
		SomeApp app = new SomeApp();
		
		app.handlers().set(new ProfileManager()).set(new RequestValidator());
		
		app.start();
		
		try {
			app.send(request().inScope("/bad/scope")); //call in no scope
			fail();
		}
		catch(UniformInterfaceException e) {
			assertEquals(invalid_request_error.code(), e.getResponse().getStatus());
		}
		
	}
	
	@Test
	public void propagatesScope() throws Throwable {
		
		SomeApp app = new SomeApp();
		
		app.handlers().set(new ProfileManager()).set(new RequestValidator());
		
		Runnable test = new Runnable() {
			
			@Override
			public void run() {
				assertEquals(scope,ScopeProvider.instance.get());
			}
		};
		
		app.startWith(test);
		
		app.send(request());
		
	}
	
	@Test
	public void respectsAllExcludeWildCard() throws Throwable {
		
		SomeApp app = new SomeApp();
		
		app.configuration().excludes().add(Constants.EXCLUDE_ALL);
		
		app.handlers().set(new ProfileManager()).set(new RequestValidator());
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		Runnable test = new Runnable() {
			
			@Override
			public void run() {
				latch.countDown();
			}
		};
		
		app.startWith(test);
		
		app.send(request().inScope(null));
		
		latch.await(500,TimeUnit.MILLISECONDS);
		
	}
	
	@Test
	public void respectsExcludeWildCard() throws Throwable {
		
		SomeApp app = new SomeApp();
		
		app.configuration().excludes().add("/path"+Constants.EXCLUDE_ALL);
		
		app.handlers().set(new ProfileManager()).set(new RequestValidator());
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		Runnable test = new Runnable() {
			
			@Override
			public void run() {
				latch.countDown();
			}
		};
		
		app.startWith(test);
		
		app.send(request().at("path/test").inScope(null));
		
		latch.await(500,TimeUnit.MILLISECONDS);
		
	}
	
	
	@Test
	public void respectsExactExclude() throws Throwable {
		
		SomeApp app = new SomeApp();
		
		app.configuration().excludes().add("/path");
		
		app.handlers().set(new ProfileManager()).set(new RequestValidator());
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		Runnable test = new Runnable() {
			
			@Override
			public void run() {
				latch.countDown();
			}
		};
		
		app.startWith(test);
		
		app.send(request().at("path").inScope(null));
		
		latch.await(500,TimeUnit.MILLISECONDS);
		
	}
	
}
