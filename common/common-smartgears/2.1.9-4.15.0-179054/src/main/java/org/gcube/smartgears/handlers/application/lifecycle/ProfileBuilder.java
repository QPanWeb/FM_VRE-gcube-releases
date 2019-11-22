package org.gcube.smartgears.handlers.application.lifecycle;

import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletRegistration;

import org.gcube.common.resources.gcore.GCoreEndpoint;
import org.gcube.common.resources.gcore.HostingNode;
import org.gcube.smartgears.configuration.application.ApplicationConfiguration;
import org.gcube.smartgears.configuration.container.ContainerConfiguration;
import org.gcube.smartgears.context.application.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileBuilder {

	private static List<String> servletExcludes = Arrays.asList("default","jsp");

	private static final Logger log = LoggerFactory.getLogger(ProfileBuilder.class);
	
	private ApplicationContext context;

	public ProfileBuilder(ApplicationContext context) {
		this.context = context;
	}

	public void fill(GCoreEndpoint endpoint) {


		ApplicationConfiguration configuration = context.configuration();
		ContainerConfiguration container = context.container().configuration();


		endpoint.profile()
		.description(configuration.description())
		.serviceName(configuration.name())
		.serviceClass(configuration.serviceClass())
		.version(configuration.version())
		.serviceId(configuration.name() + configuration.serviceClass() + configuration.version())
		.ghnId(context.container().profile(HostingNode.class).id());

		endpoint.profile().newDeploymentData()
		.activationTime(Calendar.getInstance())
		.status((context.lifecycle().state().remoteForm()));

		endpoint.profile().endpoints().clear();
		
		String baseAddress;
		if (configuration.proxied()){
			String protocol = configuration.proxyAddress().protocol();
			String port = configuration.proxyAddress().port()!=null?":"+configuration.proxyAddress().port():"";

			baseAddress=String.format("%s://%s%s%s", protocol , configuration.proxyAddress().hostname(), port,context.application().getContextPath());
		} else {
			String protocol = container.protocol();
			int port = container.port();

			baseAddress=String.format("%s://%s:%d%s", protocol , container.hostname(), port,context.application().getContextPath());
		}
		
		for (ServletRegistration servlet : context.application().getServletRegistrations().values())
			if (!servletExcludes.contains(servlet.getName()))
				for (String mapping : servlet.getMappings()) {
					String address = baseAddress+(mapping.endsWith("*")?mapping.substring(0,mapping.length()-2):mapping);
					endpoint.profile().endpoints().add().nameAndAddress(servlet.getName(),URI.create(address));
				}


	}

}
