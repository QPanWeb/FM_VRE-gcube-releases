package org.gcube.data.spd.wormsplugin;
import static org.gcube.resources.discovery.icclient.ICFactory.clientFor;
import static org.gcube.resources.discovery.icclient.ICFactory.queryFor;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.gcube.common.resources.gcore.ServiceEndpoint;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.data.spd.model.exceptions.ExternalRepositoryException;
import org.gcube.data.spd.model.exceptions.StreamException;
import org.gcube.data.spd.model.products.ResultItem;
import org.gcube.data.spd.plugin.fwk.writers.ObjectWriter;
import org.gcube.resources.discovery.client.api.DiscoveryClient;
import org.gcube.resources.discovery.client.queries.api.SimpleQuery;


public class Test {

	/**
	 * @param args
	 * @throws ServiceException 
	 * @throws ExternalRepositoryException 
	 */
	public static void main(String[] args) throws ServiceException, ExternalRepositoryException {
		
		SimpleQuery query = queryFor(ServiceEndpoint.class);

		query.addCondition("$resource/Profile/Category/text() eq 'BiodiversityRepository' and $resource/Profile/Name eq 'WoRMS' ");
		ScopeProvider.instance.set("/gcube/devsec");

		DiscoveryClient<ServiceEndpoint> client = clientFor(ServiceEndpoint.class);

		List<ServiceEndpoint> resources = client.submit(query);

		System.out.println(resources.size());
		
		WormsPlugin a = new WormsPlugin();
		if(resources.size() != 0) {	   
			try {
				a.initialize(resources.get(0));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

//		WormsPlugin.binding = (aphia.v1_0.AphiaNameServiceBindingStub)
//				new aphia.v1_0.AphiaNameServiceLocator().getAphiaNameServicePort();
		
//		a.searchByCommonName("Galapagos", new ObjectWriter<ResultItem>() {
//
//			@Override
//			public boolean put(ResultItem t) {
//				System.out.println(t);
//				return false;
//			}
//
//			@Override
//			public void close() {
//				// 
//
//			}
//		});
		
		a.searchByScientificName("biota", new ObjectWriter<ResultItem>() {

			@Override
			public boolean isAlive() {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean write(ResultItem t) {
//				System.out.println(t.toString());
				System.out.println(t.getId() + " " + t.getScientificName() + " " + t.getDataSet());			
				System.out.println(t.getRank());
//				if (t.getParent() != null){
//					System.out.println(t.getParent().getId() + " " + t.getParent().getScientificName()  + " " + t.getParent().getCitation());
//				}
				return true;
			}

			@Override
			public boolean write(StreamException arg0) {
				// TODO Auto-generated method stub
				return false;
			}
		});

	}
}


