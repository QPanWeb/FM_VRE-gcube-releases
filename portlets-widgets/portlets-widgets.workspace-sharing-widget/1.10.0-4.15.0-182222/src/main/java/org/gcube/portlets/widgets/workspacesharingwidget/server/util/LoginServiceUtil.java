package org.gcube.portlets.widgets.workspacesharingwidget.server.util;

import static org.gcube.resources.discovery.icclient.ICFactory.clientFor;
import static org.gcube.resources.discovery.icclient.ICFactory.queryFor;

import java.util.ArrayList;
import java.util.List;

import org.gcube.common.resources.gcore.GenericResource;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.common.scope.impl.ScopeBean;
import org.gcube.common.scope.impl.ScopeBean.Type;
import org.gcube.portlets.widgets.workspacesharingwidget.shared.system.UserBelonging;
import org.gcube.portlets.widgets.workspacesharingwidget.shared.system.VO;
import org.gcube.portlets.widgets.workspacesharingwidget.shared.system.VRE;
import org.gcube.resources.discovery.client.api.DiscoveryClient;
import org.gcube.resources.discovery.client.queries.api.SimpleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Massimiliano Assante ISTI-CNR
 *
 *         Jan 10th 2012
 *
 *         changed by Francesco Mangiacrapa
 */
public class LoginServiceUtil {

	public static final String ROOT_ORG = "rootorganization";
	public static final String PUBLIC_LAYOUT_NAME = "	Data e-Infrastructure gateway";
	public static final String GUEST_COMMUNITY_NAME = "Guest";

	private static final Logger _log = LoggerFactory.getLogger(LoginServiceUtil.class);

	/**
	 * simply returns fake VOS for debugging purpose
	 * 
	 * @return List of VO
	 */
	protected static List<VO> getFakeVOs() {
		VO rootVO = new VO();
		rootVO.setRoot(true);
		rootVO.setName("/d4science.research-infrastructures.eu/");
		rootVO.setDescription("This is the description for the ROOT VO");
		rootVO.setImageURL("http://portal.d4science.research-infrastructures.eu/vologin/html/RedGrid.jpg");
		rootVO.setUserBelonging(UserBelonging.BELONGING);

		/***************************************/

		VO emVO = new VO();
		emVO.setRoot(false);
		emVO.setGroupName("/d4science.research-infrastructures.eu/EM/");
		emVO.setName("EM VO");
		emVO.setDescription(
				"EM and AEM Virtual Organisation The FARM Virtual Organisation is the dynamic group of individuals and/or institutions defined around a set of sharing rules in which resource providers and consumers specify clearly and carefully just what is shared, who is allowed to share, and the conditions under which sharing occurs to serve the needs of the Fisheries and Aquaculture Resources Management.");
		emVO.setImageURL("http://portal.d4science.research-infrastructures.eu/vologin/html/RedGrid.jpg");

		emVO.setUserBelonging(UserBelonging.NOT_BELONGING);
		//
		//
		VRE cool_EM_VRE = new VRE();
		cool_EM_VRE.setName("COOL EM VRE");
		cool_EM_VRE.setGroupName("/d4science.research-infrastructures.eu/EM/COOLEMVRE");
		cool_EM_VRE.setDescription("cool_EM_VRE VRE Description<br />"
				+ "This Virtual Research Environment is for cool authors, managers and researchers who produce reports containing cool data.");
		cool_EM_VRE.setImageURL("http://portal.d4science.research-infrastructures.eu/vologin/html/gcm-preview.jpg");
		cool_EM_VRE.setUserBelonging(UserBelonging.BELONGING);
		emVO.addVRE(cool_EM_VRE);

		VRE cool_EM_VRE2 = new VRE();
		cool_EM_VRE2.setName("COOL VRE 2");
		cool_EM_VRE2.setGroupName("/d4science.research-infrastructures.eu/EM/COOLEMVRE2");
		cool_EM_VRE2.setDescription("Cool VRE Description<br />"
				+ "This Virtual Research Environment is for cool authors, managers and researchers who produce reports containing cool data.");

		cool_EM_VRE2
				.setImageURL("http://portal.d4science.research-infrastructures.eu/vologin/html/aquamaps-preview.jpg");
		cool_EM_VRE2.setUserBelonging(UserBelonging.NOT_BELONGING);

		VRE cool_EM_VRE3 = new VRE();
		cool_EM_VRE3.setName("COOL EM VRE TRE");
		cool_EM_VRE3.setGroupName("/d4science.research-infrastructures.eu/EM/COOlVRE3");
		cool_EM_VRE3.setDescription("Cool VRE Description<br />"
				+ "This Virtual Research Environment is for cool authors, managers and researchers who produce reports containing cool data.");

		cool_EM_VRE3
				.setImageURL("http://portal.d4science.research-infrastructures.eu/vologin/html/aquamaps-preview.jpg");
		cool_EM_VRE3.setUserBelonging(UserBelonging.BELONGING);

		VRE demo = new VRE();
		demo.setName("Demo");
		demo.setGroupName("/d4science.research-infrastructures.eu/EM/Demo");
		demo.setDescription("Cool VRE Description<br />"
				+ "This Virtual Research Environment is for cool authors, managers and researchers who produce reports containing cool data.");

		demo.setImageURL("http://portal.d4science.research-infrastructures.eu/vologin/html/RedGrid.jpg");
		demo.setUserBelonging(UserBelonging.BELONGING);

		VRE vreGCM = new VRE();
		vreGCM.setName("GCM");
		vreGCM.setGroupName("/d4science.research-infrastructures.eu/EM/GCM");
		vreGCM.setDescription("Global Ocean Chlorophyll Monitoring (GCM) Virtual Research Environment<br />"
				+ "The phytoplankton plays a similar role to terrestrial green plants in the photosynthetic process and are credited with removing as much carbon dioxide from the atmosphere as their earthbound counterparts, making it important to monitor and model plankton into calculations of future climate change.");
		vreGCM.setImageURL("http://portal.d4science.research-infrastructures.eu/vologin/html/aquamaps-preview.jpg");
		vreGCM.setUserBelonging(UserBelonging.BELONGING);

		emVO.addVRE(cool_EM_VRE);
		emVO.addVRE(cool_EM_VRE2);
		emVO.addVRE(cool_EM_VRE3);
		emVO.addVRE(demo);
		emVO.addVRE(vreGCM);

		ArrayList<VO> toReturn = new ArrayList<VO>();
		toReturn.add(rootVO);
		toReturn.add(emVO);
		toReturn.add(emVO);
		return toReturn;
	}

	/**
	 *
	 * @param scopename
	 *            a string
	 * @return true if any VRE Exists
	 */
	protected static Boolean checkVresPresence(String scopename) {
		ScopeBean scope = null;
		scope = new ScopeBean("/" + scopename);

		try {
			_log.info("Searching for VREs into " + scope.name());
			String currScope = ScopeProvider.instance.get();
			ScopeProvider.instance.set(scope.toString());
			SimpleQuery query = queryFor(GenericResource.class);
			query.addCondition("$resource/Profile/SecondaryType/text() eq '" + Type.VRE + "'");

			DiscoveryClient<GenericResource> client = clientFor(GenericResource.class);
			boolean result = client.submit(query).size() > 0;
			ScopeProvider.instance.set(currScope);
			return result;
		} catch (Exception e) {
			_log.error("Generic Exception for " + scope.name() + " " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * 
	 * @param scopename scope name
	 * @return List of VREs
	 */
	protected static List<VRE> getVREsFromInfrastructure(String scopename) {
		List<VRE> toReturn = new ArrayList<VRE>();
		ScopeBean scope = null;
		scope = new ScopeBean("/" + scopename);

		try {
			_log.info("Searching for VREs into " + scope.name());
			String currScope = ScopeProvider.instance.get();
			ScopeProvider.instance.set(scope.toString());
			SimpleQuery query = queryFor(GenericResource.class);
			query.addCondition("$resource/Profile/SecondaryType/text() eq '" + Type.VRE + "'");

			DiscoveryClient<GenericResource> client = clientFor(GenericResource.class);
			for (GenericResource genres : client.submit(query)) {
				toReturn.add(new VRE(genres.profile().name(), genres.profile().description(), "", "", "", null));
			}
			ScopeProvider.instance.set(currScope);
			return toReturn;
		} catch (Exception e) {
			e.printStackTrace();
			_log.error("Generic Exception for " + scope.name() + " " + e.getMessage());
			return null;
		}
	}

}
