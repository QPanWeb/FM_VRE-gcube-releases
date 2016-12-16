package org.gcube.smartgears.managers;

import static org.gcube.smartgears.Constants.*;
import static org.gcube.smartgears.handlers.application.request.RequestError.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gcube.smartgears.configuration.application.Exclude;
import org.gcube.smartgears.context.application.ApplicationContext;
import org.gcube.smartgears.context.application.DefaultApplicationContext;
import org.gcube.smartgears.handlers.application.ApplicationPipeline;
import org.gcube.smartgears.handlers.application.RequestEvent;
import org.gcube.smartgears.handlers.application.RequestHandler;
import org.gcube.smartgears.handlers.application.ResponseEvent;
import org.gcube.smartgears.handlers.application.request.RequestError;
import org.gcube.smartgears.handlers.application.request.RequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Filter} that executes a {@link ApplicationPipeline} of {@link RequestHandler}s before and a client request is delivered
 * to a given servlet and before the response produced by the servlet is returned to the client.
 * 
 * @author Fabio Simeoni
 * 
 */
public class RequestManager implements Filter {

	private static Logger log = LoggerFactory.getLogger(RequestManager.class);

	private final ApplicationContext context;
	private final String servlet;
	private final List<RequestHandler> handlers;

	/**
	 * Creates an instance with the name of the target servlet and a pipeline.
	 * 
	 * @param servlet the name of the servlet
	 * @param pipeline the pipeline
	 */
	public RequestManager(ApplicationContext context, String servletName, List<RequestHandler> handlers) {
		this.context = context;
		this.servlet = servletName;
		this.handlers = handlers;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	ServletException {

		HttpServletRequest httprequest = (HttpServletRequest) request;
		HttpServletResponse httpresponse = (HttpServletResponse) response;

		List<RequestHandler> filterHandlers = getPipelineWithExcluded(httprequest, handlers);
				
		if (filterHandlers.isEmpty()){
			
			log.trace("filtered handlers are empty");
			
			chain.doFilter(request, response);

		}else {
			
			ApplicationPipeline<RequestHandler> pipeline = new ApplicationPipeline<RequestHandler>(filterHandlers);
			
			log.trace("filtered handler for this call are {}", filterHandlers);
			
			// create a per-request context with temporary properties
			ApplicationContext ctx = new DefaultApplicationContext(context);

			RequestEvent event = new RequestEvent(servlet, ctx, httprequest,httpresponse);

			try {
				pipeline.forward(event);
			}
			catch(Throwable t) {
				handleError(httprequest,httpresponse,t);
				return;
			}

			try{
				// dispatch to other filters for this servlet
				chain.doFilter(request, response);
			}catch(Throwable t){
				t.printStackTrace();
				handleError(httprequest,httpresponse,t);
			}

			ResponseEvent responseEvent = new ResponseEvent(servlet, ctx, httprequest, httpresponse);

			try {

				pipeline.reverse().forward(responseEvent);
			}
			catch(Throwable t) {
				t.printStackTrace();
				handleError(httprequest,httpresponse,t);
				return;
			}
		}
	}

	private List<RequestHandler> getPipelineWithExcluded(
			HttpServletRequest request, List<RequestHandler> handlersToFilter) {
		
		String query = request.getQueryString();
		
		log.debug("servletPath is {} and pathInfo is {}",request.getServletPath(), request.getPathInfo());

		if ("wsdl".equals(query) || "wsdl=1".equals(query))
			return Collections.emptyList();
		
		String path = request.getServletPath()==null?"":request.getServletPath();
		
		path += request.getPathInfo() ==null?"":request.getPathInfo();
		
		
		log.debug("check wich handler should be excluded {}", path);
						
		for (Exclude exclude : context.configuration().excludes()){
			String excludePath= exclude.getPath();
			log.trace("exclude is {}",exclude);
			if (
					(EXCLUDE_ALL).equals(exclude) || 
					(excludePath.endsWith(EXCLUDE_ALL) && path!=null && path.startsWith(excludePath.substring(0,excludePath.length()-2))) ||
					excludePath.equals(path) || (path.endsWith("/") && excludePath.equals(path.substring(0, path.length()-1)))
					){
				//ALL handler are filtered
				if (exclude.getHandlers().isEmpty()) return Collections.emptyList();
								
				List<RequestHandler> filteredHandlers = new ArrayList<>();
				for (RequestHandler rh : handlersToFilter){
					if (!exclude.getHandlers().contains(rh.getName()))
						filteredHandlers.add(rh);
				}
				return filteredHandlers;
			}
		}
		
		//in case no path is recognized in excludes it return all handlers
		return handlersToFilter;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		// propagate filter initialisation to handler
		try {

			for (RequestHandler handler : this.handlers)

				handler.start(context);

		} catch (Throwable t) {

			throw new ServletException(t);

		}
	}

	@Override
	public void destroy() {

		for (RequestHandler handler : this.handlers)
			try {

				handler.stop();

			} catch (Throwable t) {

				log.error("cannot terminate handler {} for application {} ", handler, context.name());
			}

	}

	// helpers
/*
	private boolean shouldExcludeRequest(HttpServletRequest request) {

		String query = request.getQueryString();
		
		log.debug("servletPath is {} and pathInfo is {}",request.getServletPath(), request.getPathInfo());

		if ("wsdl".equals(query) || "wsdl=1".equals(query))
			return true;
		
		String path = request.getServletPath()==null?"":request.getServletPath();
		
		path += request.getPathInfo() ==null?"":request.getPathInfo();
		
		
		log.debug("check if should exclude call with path {}", path);
		
		for (Exclude exclude : context.configuration().excludes()){
			if (!exclude.getHandlers().isEmpty()) continue;
			String excludePath= exclude.getPath();
			log.trace("exclude is {}",exclude);
			if (
					(EXCLUDE_ALL).equals(exclude) || 
					(excludePath.endsWith(EXCLUDE_ALL) && path!=null && path.startsWith(excludePath.substring(0,excludePath.length()-2))) ||
					excludePath.equals(path) || (path.endsWith("/") && excludePath.equals(path.substring(0, path.length()-1)))
					)
				return true;
		}
		return false;
	}*/

	private void handleError(HttpServletRequest request, HttpServletResponse response,Throwable t) throws IOException {

		RequestError error = t instanceof RequestException?
				RequestException.class.cast(t).error():
					application_error;

				if (error == application_error) {
					response.sendError(error.code(),error.message());
				}else {
					if (error == request_not_authorized_error){
						response.setHeader("WWW-Authenticate", "Basic realm=\"Smartgears\"");
						log.info("setting WWW-Authenticate to response header");
					}
					response.getWriter().write("Error ("+error.code()+") : "+error.message()+"\nStacktrace:\n");
					t.printStackTrace(response.getWriter());
					response.setStatus(error.code());
				}

	}
}
