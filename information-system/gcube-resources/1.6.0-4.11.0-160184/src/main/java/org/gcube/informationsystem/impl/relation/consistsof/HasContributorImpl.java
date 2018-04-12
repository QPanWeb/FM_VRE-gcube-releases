package org.gcube.informationsystem.impl.relation.consistsof;

import org.gcube.informationsystem.model.embedded.PropagationConstraint;
import org.gcube.informationsystem.model.entity.Resource;
import org.gcube.informationsystem.model.entity.facet.ContactFacet;
import org.gcube.informationsystem.model.relation.consistsof.HasContributor;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Luca Frosini (ISTI - CNR)
 */
@JsonTypeName(value = HasContributor.NAME)
public class HasContributorImpl<Out extends Resource, In extends ContactFacet>
		extends HasContactImpl<Out, In> implements HasContributor<Out, In> {

	protected HasContributorImpl() {
		super();
	}

	public HasContributorImpl(Out source, In target,
			PropagationConstraint propagationConstraint) {
		super(source, target, propagationConstraint);
	}

}
