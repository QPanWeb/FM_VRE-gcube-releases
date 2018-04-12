package org.gcube.data.spd.manager.search.workers;

import org.gcube.data.spd.exception.MaxRetriesReachedException;
import org.gcube.data.spd.manager.search.Worker;
import org.gcube.data.spd.model.exceptions.ExternalRepositoryException;
import org.gcube.data.spd.model.exceptions.StreamBlockingException;
import org.gcube.data.spd.plugin.fwk.AbstractPlugin;
import org.gcube.data.spd.plugin.fwk.writers.ClosableWriter;
import org.gcube.data.spd.plugin.fwk.writers.ObjectWriter;
import org.gcube.data.spd.utils.QueryRetryCall;
import org.gcube.data.spd.utils.VOID;

public class UnfolderWorker extends Worker<String, String>{

	
	private AbstractPlugin plugin;
	
	public UnfolderWorker(ClosableWriter<String> writer, AbstractPlugin plugin) {
		super(writer);
		this.plugin = plugin;
	}

	@Override
	protected void execute(final String item, final ObjectWriter<String> outputWriter) {
		outputWriter.write(item);
		try {
			new QueryRetryCall(){

				@Override
				protected VOID execute() throws ExternalRepositoryException {
					plugin.getUnfoldInterface().unfold(outputWriter, item);	
					return VOID.instance();
				}
				
			}.call();
		} catch (MaxRetriesReachedException e) {
			logger.error("error executing unfolding",e);
			outputWriter.write(new StreamBlockingException(plugin.getRepositoryName()));
		}
		
		
	}
	
}
