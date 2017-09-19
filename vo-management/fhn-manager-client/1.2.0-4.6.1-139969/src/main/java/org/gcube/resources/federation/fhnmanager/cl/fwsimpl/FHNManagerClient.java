package org.gcube.resources.federation.fhnmanager.cl.fwsimpl;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.QueryParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

//import org.gcube.common.authorization.client.Constants;
import org.gcube.common.clients.Call;
import org.gcube.common.clients.delegates.AsyncProxyDelegate;
import org.gcube.common.clients.delegates.ProxyDelegate;
import org.gcube.common.clients.exceptions.ServiceException;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.resources.federation.fhnmanager.api.FHNManager;
import org.gcube.resources.federation.fhnmanager.api.exception.FHNManagerException;
import org.gcube.resources.federation.fhnmanager.api.type.OccopusInfrastructure;
import org.gcube.resources.federation.fhnmanager.api.type.Node;
import org.gcube.resources.federation.fhnmanager.api.type.ResourceTemplate;
import org.gcube.resources.federation.fhnmanager.api.type.ServiceProfile;
import org.gcube.resources.federation.fhnmanager.api.type.VMProvider;

public class FHNManagerClient implements FHNManager {

	private final AsyncProxyDelegate<WebTarget> delegate;

	public FHNManagerClient(ProxyDelegate<WebTarget> config) {
		this.delegate = new AsyncProxyDelegate<WebTarget>(config);
	}

	// private HttpURLConnection makeRequest(URL url, String method, boolean
	// includeTokenInHeader) throws Exception{
	// HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	// if (includeTokenInHeader)
	// connection.setRequestProperty(Constants.SCOPE_HEADER_ENTRY,ScopeProvider.instance.get());
	// connection.setRequestMethod(method);
	// return connection;
	// }

	@Override
	public VMProvider getVMProviderbyId(final String vmProviderId) throws FHNManagerException {
		Call<WebTarget, VMProvider> call = new Call<WebTarget, VMProvider>() {
			public VMProvider call(WebTarget endpoint) throws Exception {
				return endpoint.path("vmproviders").path(vmProviderId).request(MediaType.TEXT_XML)
						.get(new GenericType<VMProvider>() {
						});
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public Set<VMProvider> findVMProviders(final String serviceProfileId) throws FHNManagerException {
		Call<WebTarget, Set<VMProvider>> call = new Call<WebTarget, Set<VMProvider>>() {
			public Set<VMProvider> call(WebTarget endpoint) throws Exception {
				return endpoint.path("vmproviders").queryParam("serviceProfileId", serviceProfileId)
						.request(MediaType.TEXT_XML).get(new GenericType<Set<VMProvider>>() {
						});
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public Set<ServiceProfile> allServiceProfiles() throws FHNManagerException {
		Call<WebTarget, Set<ServiceProfile>> call = new Call<WebTarget, Set<ServiceProfile>>() {
			public Set<ServiceProfile> call(WebTarget endpoint) throws Exception {
				return endpoint.path("serviceprofiles").request(MediaType.TEXT_XML)
						.get(new GenericType<Set<ServiceProfile>>() {
						});
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void startNode(final String nodeId) throws FHNManagerException {
		Call<WebTarget, Void> call = new Call<WebTarget, Void>() {
			public Void call(WebTarget endpoint) throws Exception {
				endpoint.path("nodes/start").queryParam("nodeId", nodeId).request(MediaType.TEXT_XML).get();
				return null;
			}
		};
		try {
			delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void stopNode(final String nodeId) throws FHNManagerException {
		Call<WebTarget, Void> call = new Call<WebTarget, Void>() {
			public Void call(WebTarget endpoint) throws Exception {
				endpoint.path("nodes/stop").queryParam("nodeId", nodeId).request(MediaType.TEXT_XML).get();
				return null;
			}
		};
		try {
			delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void deleteNode(final String nodeId) throws FHNManagerException {
		Call<WebTarget, Void> call = new Call<WebTarget, Void>() {
			public Void call(WebTarget endpoint) throws Exception {
				endpoint.path("nodes/delete").queryParam("nodeId", nodeId).request(MediaType.TEXT_XML).get();
				return null;
			}
		};
		try {
			delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public Collection<ResourceTemplate> findResourceTemplate(final String vmProviderId) throws FHNManagerException {
		Call<WebTarget, Collection<ResourceTemplate>> call = new Call<WebTarget, Collection<ResourceTemplate>>() {
			public Collection<ResourceTemplate> call(WebTarget endpoint) throws Exception {
				return endpoint.path("resourceTemplate").queryParam("vmProviderId", vmProviderId)
						.request(MediaType.TEXT_XML).get(new GenericType<Collection<ResourceTemplate>>() {
						});
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	// @Override
	// public Node getNodeById(final String nodeId) throws FHNManagerException {
	// Call<WebTarget, Node> call = new Call<WebTarget, Node>() {
	// public Node call(WebTarget endpoint) throws Exception {
	// return
	// endpoint.path("nodes").path(nodeId).request(MediaType.TEXT_XML).get(new
	// GenericType<Node>() {
	// });
	// }
	// };
	// try {
	// return delegate.make(call);
	// } catch (FHNManagerException e) {
	// throw e;
	// } catch (ServiceException e) {
	// throw e;
	// } catch (Exception e) {
	// throw new ServiceException(e);
	// }
	// }
	//

	@Override
	public Node getNodeById(final String nodeId) throws FHNManagerException {
		Call<WebTarget, Node> call = new Call<WebTarget, Node>() {
			public Node call(WebTarget endpoint) throws Exception {
				return endpoint.path("nodes").path(nodeId).request(MediaType.TEXT_XML).get(new GenericType<Node>() {
				});
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public Set<Node> findNodes(final String serviceProfileId, final String vmProviderId) throws FHNManagerException {
		Call<WebTarget, Set<Node>> call = new Call<WebTarget, Set<Node>>() {
			public Set<Node> call(WebTarget endpoint) throws Exception {
				System.out.println(endpoint.path("nodes").queryParam("serviceProfileId", serviceProfileId)
						.queryParam("vmProviderId", vmProviderId));
				return endpoint.path("nodes").queryParam("serviceProfileId", serviceProfileId)
						.queryParam("vmProviderId", vmProviderId).request(MediaType.TEXT_XML)
						.get(new GenericType<Set<Node>>() {
						});
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}

	}

	@Override
	public Node createNode(final String vmProviderId, final String serviceProfileId, final String resourceTemplateId)
			throws FHNManagerException {
		Call<WebTarget, Node> call = new Call<WebTarget, Node>() {
			public Node call(WebTarget endpoint) throws Exception {
				Node n = endpoint.path("nodes/create").queryParam("vmProviderId", vmProviderId)
						.queryParam("serviceProfileId", serviceProfileId)
						.queryParam("resourceTemplateId", resourceTemplateId).request(MediaType.TEXT_XML)
						.get(new GenericType<Node>() {
						});
				return n;
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}

	}

	@Override
	public OccopusInfrastructure createInfrastructureByTemplate(final String infrastructureTemplateId)
			throws FHNManagerException {
		Call<WebTarget, OccopusInfrastructure> call = new Call<WebTarget, OccopusInfrastructure>() {
			public OccopusInfrastructure call(WebTarget endpoint) throws Exception {
				OccopusInfrastructure n = endpoint.path("infrastructures/create")
						.queryParam("infrastructureTemplateId", infrastructureTemplateId).request(MediaType.TEXT_XML)
						.get(new GenericType<OccopusInfrastructure>() {
						});
				return n;
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void destroyInfrastructure(final String infrastructureId) {
		// TODO Auto-generated method stub
		Call<WebTarget, Void> call = new Call<WebTarget, Void>() {
			public Void call(WebTarget endpoint) throws Exception {
				endpoint.path("infrastructures/delete").queryParam("infrastructureId", infrastructureId)
						.request(MediaType.TEXT_XML).get();
				return null;
			}
		};
		try {
			delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}

	}

	@Override
	public OccopusInfrastructure getInfrastructureById(final String infraId) throws FHNManagerException {
		Call<WebTarget, OccopusInfrastructure> call = new Call<WebTarget, OccopusInfrastructure>() {
			public OccopusInfrastructure call(WebTarget endpoint) throws Exception {
				return endpoint.path("infrastructures").path(infraId).request(MediaType.TEXT_XML)
						.get(new GenericType<OccopusInfrastructure>() {
						});
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public Set<OccopusInfrastructure> getAllInfrastructures() {
		Call<WebTarget, Set<OccopusInfrastructure>> call = new Call<WebTarget, Set<OccopusInfrastructure>>() {
			public Set<OccopusInfrastructure> call(WebTarget endpoint) throws Exception {
				return endpoint.path("infrastructures").request(MediaType.TEXT_XML)
						.get(new GenericType<Set<OccopusInfrastructure>>() {
						});
			}
		};
		try {
			return delegate.make(call);
		} catch (FHNManagerException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

}
