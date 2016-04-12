package org.gcube.opensearch.opensearchlibrary.queryelements.extensions.time;

import java.util.Map;

import org.gcube.opensearch.opensearchlibrary.DescriptionDocument;
import org.gcube.opensearch.opensearchlibrary.queryelements.QueryElement;
import org.gcube.opensearch.opensearchlibrary.queryelements.QueryElementFactory;
import org.gcube.opensearch.opensearchlibrary.urlelements.URLElementFactory;
import org.w3c.dom.Element;

/**
 * A factory class for the construction of GeoQueryElement objects
 * 
 * @author gerasimos.farantatos
 *
 */
public class TimeQueryElementFactory implements QueryElementFactory {

	QueryElementFactory f;
	
	/**
	 * Creates a new instance of the factory
	 * @param f The {@link QueryElementFactory} used to create Query elements which will be next in the chain of responsibility
	 */
	public TimeQueryElementFactory(QueryElementFactory f) {
		this.f = f;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.gcube.opensearch.opensearchlibrary.queryelements.QueryElementFactory#newInstance(Element)
	 */
	public TimeQueryElement newInstance(Element query, Map<String, String> nsPrefixes) throws Exception {
		QueryElement el = f.newInstance(query, nsPrefixes);
		return new TimeQueryElement(query, nsPrefixes, el);
	}
}