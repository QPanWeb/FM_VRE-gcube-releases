package org.gcube.informationsystem.registry.impl.contexts;

	
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gcube.common.core.contexts.GCUBEServiceContext;
import org.gcube.common.core.contexts.GHNContext;
import static org.gcube.common.core.contexts.GHNContext.Mode;
import org.gcube.common.core.informationsystem.client.ISClient;
import org.gcube.common.core.informationsystem.client.queries.GCUBERIQuery;
import org.gcube.common.core.informationsystem.publisher.ISLocalPublisher;
import org.gcube.common.core.resources.GCUBEResource;
import org.gcube.common.core.utils.events.GCUBEProducer;
import org.gcube.common.core.utils.events.GCUBETopic;
import org.gcube.common.core.utils.handlers.GCUBEScheduledHandler;
import org.gcube.informationsystem.registry.impl.local.LocalProfileConsumerImpl;
import org.gcube.informationsystem.registry.impl.resourcemanagement.EliminatePoolingThread;



/**
 * IS-Registry service context
 * 
 * @author Andrea Manzi, Manuele Simi , Lucio lelii (CNR)
 * 
 */
public class ServiceContext extends GCUBEServiceContext {

	public static final String JNDI_NAME = "gcube/informationsystem/registry";

	public static Map<String, EliminatePoolingThread> threadTable = Collections.synchronizedMap(new HashMap<String, EliminatePoolingThread>());
	
	protected static final ServiceContext cache = new ServiceContext();
	
	protected ISClient client = null;
	
	protected GCUBERIQuery queryRI = null;
	
	
	public static enum RegistryTopic implements GCUBETopic{CREATE,UPDATE, REMOVE};
	//protected GCUBEResourceXPathQuery queryGHN = null;
	
	protected boolean isNotifierCodeployed = false;

	protected boolean isICCodeployed = false;

	protected GCUBEProducer<RegistryTopic, GCUBEResource> topicProducer;
	
	@SuppressWarnings("unchecked")
	protected class NotificationResourceScheduler extends GCUBEScheduledHandler {

		
		public NotificationResourceScheduler(long interval, Mode mode) {
			super(interval, mode);
		}
		
		@Override
		protected boolean repeat(Exception exception, int exceptionCount)  {
			if (exception!=null) {
				ServiceContext.this.logger.warn("Failed to create the notification resource (attempt "+exceptionCount+" out of 20)",exception);
				if (exceptionCount >= 20) {
					ServiceContext.this.logger.error("Max attempts reached, no more chance to register the notification resource");
					return false;
				} else 
					return true;
			} else 
				return false;
		}
		
	}
	
	
	
	private ServiceContext() {}
	
	/**
	 * 
	 * @return ServiceContext
	 */
	public static ServiceContext getContext() {
		return cache;
	}

	/**
	 * @return the JNDI name
	 */
	@Override
	public String getJNDIName() {
		return JNDI_NAME;
	}

	
	/**
	 * 
	 * @return GCUBEProducer
	 */
	public GCUBEProducer<RegistryTopic, GCUBEResource> getTopicProducer(){
		return topicProducer;
	}
	
	@Override
	protected void onReady() throws Exception {
				
		//switch to the production mode if needed
		if (GHNContext.getContext().getMode() == Mode.ROOT)
			GHNContext.getContext().setMode(Mode.CONNECTED);				
		
		/*NotificationResourceScheduler scheduler = new NotificationResourceScheduler(20, GCUBEScheduledHandler.Mode.LAZY);		
		
		scheduler.setScheduled(new GCUBEHandler(){ 
			@Override
			public void run() throws Exception {
				RegistryFactory.initialize();
			}
		});
    	scheduler.run();*/
		this.subscribeToLocalRegistrationEvents();
	}

	
	@Override
	protected void onInitialisation() throws Exception {
		this.client =  GHNContext.getImplementation(ISClient.class);
		topicProducer= new GCUBEProducer<RegistryTopic, GCUBEResource>();
		//topicProducer.registerTopics(RegistryTopic.CREATE, RegistryTopic.UPDATE, RegistryTopic.REMOVE);
	}


	
	/**
	 * Listeners for local events
	 * @throws Exception
	 */
	private void subscribeToLocalRegistrationEvents() throws Exception{
		ISLocalPublisher pub = GHNContext.getImplementation(ISLocalPublisher.class);
		logger.debug("Subscribing IS-Registry for local profiles' events");
	    pub.subscribeLocalProfileEvents(new LocalProfileConsumerImpl());
	}
	
	public void waitUntilReady() {
     	 while (true) {     		 
     		 if (ServiceContext.getContext().getStatus() == Status.READIED)
	           	break;
     		 else
     			 try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
	     }
     }
		
}