package org.gcube.data.analysis.statisticalmanager.stubs.types.schema;

import static org.gcube.data.analysis.statisticalmanager.stubs.SMConstants.TYPES_NAMESPACE;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(namespace = TYPES_NAMESPACE)

@XmlEnum(String.class)
public enum ComputationalAgentClass {
	
	DISTRIBUTIONS,
	MODELS,
	EVALUATORS,
	CLUSTERERS,
	TRANSDUCERS
}
