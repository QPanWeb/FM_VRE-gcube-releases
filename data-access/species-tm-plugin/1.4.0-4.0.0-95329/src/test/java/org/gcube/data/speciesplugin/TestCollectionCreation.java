/**
 * 
 */
package org.gcube.data.speciesplugin;

import static org.gcube.data.tml.proxies.TServiceFactory.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.data.speciesplugin.requests.RequestBinder;
import org.gcube.data.speciesplugin.requests.SpeciesRequest;
import org.gcube.data.streams.Stream;
import org.gcube.data.tml.proxies.BindRequest;
import org.gcube.data.tml.proxies.Binding;
import org.gcube.data.tml.proxies.TBinder;
import org.gcube.data.tml.proxies.TReader;
import org.gcube.data.trees.data.Tree;
import org.gcube.data.trees.patterns.AnyPattern;

/**
 * @author "Federico De Faveri defaveri@isti.cnr.it"
 *
 */
public class TestCollectionCreation {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		ScopeProvider.instance.set("/gcube/devsec");
		
		URI uri = new URI("http://node6.d.d4science.research-infrastructures.eu:8080/wsrf/services/gcube/data/tm/binder");
		TBinder binder = binder().at(uri).withTimeout(5, TimeUnit.MINUTES).build();

		//TBinder binder = binder().matching(plugin("species-tree-plugin")).withTimeout(5, TimeUnit.MINUTES).build();
		
		RequestBinder db = new RequestBinder();

		SpeciesRequest request = new SpeciesRequest();
		/*request.setName("Sarda Sarda collection");
		request.setDescription("Sarda Sarda collection");
		request.setScientificNames(Arrays.asList(new String[]{"Sarda Sarda"}));
		request.setDatasources(Arrays.asList(new String[]{"WoRMS"}));*/
		
		request.setName("carcharias collection Itis");
		request.setDescription("carcharias collection");
		request.setScientificNames(Arrays.asList(new String[]{"carcharias"}));
		request.setDatasources(Arrays.asList(new String[]{"ITIS"}));
		
		
		request.setStrictMatch(true);
		request.setRefreshPeriod(5);
		request.setTimeUnit(TimeUnit.MINUTES);
		System.out.println(request.toString());

		BindRequest params = new BindRequest("species-tree-plugin",db.bind(request));
		System.out.println(params.toString());
		
		List<Binding> bindings = binder.bind(params);
		
		System.out.println(bindings.toString());

		Binding binding = bindings.get(0);
		
		TReader reader = reader().at(binding.readerRef()).build();

		Stream<Tree> stream = reader.get(new AnyPattern());
		while(stream.hasNext()) System.out.println(stream.next());

	}

}
