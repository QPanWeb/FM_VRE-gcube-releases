/**
 * 
 */
package org.gcube.informationsystem.resourceregistry.er;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gcube.informationsystem.model.impl.embedded.HeaderImpl;
import org.gcube.informationsystem.model.impl.embedded.PropagationConstraintImpl;
import org.gcube.informationsystem.model.impl.relation.ConsistsOfImpl;
import org.gcube.informationsystem.model.impl.relation.IsIdentifiedByImpl;
import org.gcube.informationsystem.model.impl.utils.ISMapper;
import org.gcube.informationsystem.model.reference.embedded.Header;
import org.gcube.informationsystem.model.reference.embedded.PropagationConstraint;
import org.gcube.informationsystem.model.reference.embedded.PropagationConstraint.RemoveConstraint;
import org.gcube.informationsystem.model.reference.entity.Facet;
import org.gcube.informationsystem.model.reference.entity.Resource;
import org.gcube.informationsystem.model.reference.relation.ConsistsOf;
import org.gcube.informationsystem.model.reference.relation.IsIdentifiedBy;
import org.gcube.informationsystem.resourceregistry.ScopedTest;
import org.gcube.informationsystem.resourceregistry.api.exceptions.ResourceRegistryException;
import org.gcube.informationsystem.resourceregistry.er.entity.ResourceManagement;
import org.gcube.resourcemanagement.model.impl.entity.facet.CPUFacetImpl;
import org.gcube.resourcemanagement.model.impl.entity.facet.ContainerStateFacetImpl;
import org.gcube.resourcemanagement.model.impl.entity.facet.MemoryFacetImpl;
import org.gcube.resourcemanagement.model.impl.entity.facet.NetworkingFacetImpl;
import org.gcube.resourcemanagement.model.impl.entity.facet.SimplePropertyFacetImpl;
import org.gcube.resourcemanagement.model.impl.entity.facet.SoftwareFacetImpl;
import org.gcube.resourcemanagement.model.impl.entity.resource.HostingNodeImpl;
import org.gcube.resourcemanagement.model.impl.relation.consistsof.HasPersistentMemoryImpl;
import org.gcube.resourcemanagement.model.impl.relation.consistsof.HasVolatileMemoryImpl;
import org.gcube.resourcemanagement.model.reference.entity.facet.CPUFacet;
import org.gcube.resourcemanagement.model.reference.entity.facet.ContainerStateFacet;
import org.gcube.resourcemanagement.model.reference.entity.facet.MemoryFacet;
import org.gcube.resourcemanagement.model.reference.entity.facet.NetworkingFacet;
import org.gcube.resourcemanagement.model.reference.entity.facet.SimplePropertyFacet;
import org.gcube.resourcemanagement.model.reference.entity.facet.SoftwareFacet;
import org.gcube.resourcemanagement.model.reference.entity.facet.MemoryFacet.MemoryUnit;
import org.gcube.resourcemanagement.model.reference.entity.resource.EService;
import org.gcube.resourcemanagement.model.reference.entity.resource.HostingNode;
import org.gcube.resourcemanagement.model.reference.relation.consistsof.HasPersistentMemory;
import org.gcube.resourcemanagement.model.reference.relation.consistsof.HasVolatileMemory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * @author Luca Frosini (ISTI - CNR)
 * 
 */
public class SmartgearResourcesTest extends ScopedTest {

	private static Logger logger = LoggerFactory
			.getLogger(SmartgearResourcesTest.class);

	public static final String HOSTING_NODE = "{\"@class\":\"HostingNode\",\"header\":{\"@class\":\"Header\",\"uuid\":\"f0460614-9ffb-4ecd-bf52-d91e8d81d604\",\"creator\":null,\"creationTime\":null,\"lastUpdateTime\":null},\"consistsOf\":[{\"@class\":\"IsIdentifiedBy\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"NetworkingFacet\",\"header\":null,\"hostName\":\"pc-frosini.isti.cnr.it\",\"domainName\":\"isti.cnr.it\",\"mask\":null,\"broadcastAddress\":null,\"ipaddress\":\"127.0.1.1\",\"Port\":8080}},{\"@class\":\"ConsistsOf\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"CPUFacet\",\"header\":null,\"model\":\"Intel(R) Core(TM) i5-3450 CPU @ 3.10GHz\",\"vendor\":\"GenuineIntel\",\"clockSpeed\":\"2872.828\",\"cache_size\":\"6144 KB\",\"flags\":\"fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc aperfmperf eagerfpu pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm epb tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts\",\"bogomips\":\"6199.91\",\"fpu\":\"yes\",\"stepping\":\"9\",\"address_sizes\":\"36 bits physical, 48 bits virtual\",\"wp\":\"yes\",\"clflush_size\":\"64\",\"siblings\":\"4\",\"microcode\":\"0x17\",\"cpu_family\":\"6\",\"cpu_cores\":\"4\",\"physical_id\":\"0\",\"cpuid_level\":\"13\",\"fpu_exception\":\"yes\",\"apicid\":\"0\",\"cache_alignment\":\"64\",\"processor\":\"0\",\"core_id\":\"0\",\"initial_apicid\":\"0\",\"modelNumber\":\"58\"}},{\"@class\":\"ConsistsOf\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"CPUFacet\",\"header\":null,\"model\":\"Intel(R) Core(TM) i5-3450 CPU @ 3.10GHz\",\"vendor\":\"GenuineIntel\",\"clockSpeed\":\"2617.199\",\"cache_size\":\"6144 KB\",\"flags\":\"fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc aperfmperf eagerfpu pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm epb tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts\",\"bogomips\":\"6199.91\",\"fpu\":\"yes\",\"stepping\":\"9\",\"address_sizes\":\"36 bits physical, 48 bits virtual\",\"wp\":\"yes\",\"clflush_size\":\"64\",\"siblings\":\"4\",\"microcode\":\"0x17\",\"cpu_family\":\"6\",\"cpu_cores\":\"4\",\"physical_id\":\"0\",\"cpuid_level\":\"13\",\"fpu_exception\":\"yes\",\"apicid\":\"2\",\"cache_alignment\":\"64\",\"processor\":\"1\",\"core_id\":\"1\",\"initial_apicid\":\"2\",\"modelNumber\":\"58\"}},{\"@class\":\"ConsistsOf\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"CPUFacet\",\"header\":null,\"model\":\"Intel(R) Core(TM) i5-3450 CPU @ 3.10GHz\",\"vendor\":\"GenuineIntel\",\"clockSpeed\":\"2610.660\",\"cache_size\":\"6144 KB\",\"flags\":\"fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc aperfmperf eagerfpu pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm epb tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts\",\"bogomips\":\"6199.91\",\"fpu\":\"yes\",\"stepping\":\"9\",\"address_sizes\":\"36 bits physical, 48 bits virtual\",\"wp\":\"yes\",\"clflush_size\":\"64\",\"siblings\":\"4\",\"microcode\":\"0x17\",\"cpu_family\":\"6\",\"cpu_cores\":\"4\",\"physical_id\":\"0\",\"cpuid_level\":\"13\",\"fpu_exception\":\"yes\",\"apicid\":\"4\",\"cache_alignment\":\"64\",\"processor\":\"2\",\"core_id\":\"2\",\"initial_apicid\":\"4\",\"modelNumber\":\"58\"}},{\"@class\":\"ConsistsOf\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"CPUFacet\",\"header\":null,\"model\":\"Intel(R) Core(TM) i5-3450 CPU @ 3.10GHz\",\"vendor\":\"GenuineIntel\",\"clockSpeed\":\"2712.257\",\"cache_size\":\"6144 KB\",\"flags\":\"fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc aperfmperf eagerfpu pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm epb tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts\",\"bogomips\":\"6199.91\",\"fpu\":\"yes\",\"stepping\":\"9\",\"address_sizes\":\"36 bits physical, 48 bits virtual\",\"wp\":\"yes\",\"clflush_size\":\"64\",\"siblings\":\"4\",\"microcode\":\"0x17\",\"cpu_family\":\"6\",\"cpu_cores\":\"4\",\"physical_id\":\"0\",\"cpuid_level\":\"13\",\"fpu_exception\":\"yes\",\"apicid\":\"6\",\"cache_alignment\":\"64\",\"processor\":\"3\",\"core_id\":\"3\",\"initial_apicid\":\"6\",\"modelNumber\":\"58\"}},{\"@class\":\"ConsistsOf\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"SoftwareFacet\",\"header\":null,\"name\":\"amd64\",\"group\":\"Linux\",\"version\":\"4.4.0-47-generic\",\"description\":null,\"qualifier\":null,\"optional\":false}},{\"@class\":\"ConsistsOf\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"SimplePropertyFacet\",\"header\":null,\"name\":\"ENVIRONMENT_VARIABLES\",\"value\":\"\",\"TERM\":\"xterm\",\"XDG_SESSION_PATH\":\"/org/freedesktop/DisplayManager/Session0\",\"JAVA_HOME\":\"/usr/lib/jvm/java-7-oracle\",\"J2REDIR\":\"/usr/lib/jvm/java-7-oracle/jre\",\"LESSCLOSE\":\"/usr/bin/lesspipe %s %s\",\"UPSTART_SESSION\":\"unix:abstract=/com/ubuntu/upstart-session/1000/1613\",\"J2SDKDIR\":\"/usr/lib/jvm/java-7-oracle\",\"SESSION_MANAGER\":\"local/pc-frosini.isti.cnr.it:@/tmp/.ICE-unix/1849,unix/pc-frosini.isti.cnr.it:/tmp/.ICE-unix/1849\",\"LC_NUMERIC\":\"it_IT.UTF-8\",\"GNOME_DESKTOP_SESSION_ID\":\"this-is-deprecated\",\"Java\":\"1.7.0_80\",\"COMPIZ_CONFIG_PROFILE\":\"ubuntu\",\"GDMSESSION\":\"ubuntu\",\"IM_CONFIG_PHASE\":\"1\",\"MANDATORY_PATH\":\"/usr/share/gconf/ubuntu.mandatory.path\",\"SmartGearsDistributionBundle\":\"2.0.1-SNAPSHOT\",\"PWD\":\"/home/lucafrosini/Desktop/SmartGears-Bundle\",\"SESSIONTYPE\":\"gnome-session\",\"GIO_LAUNCHED_DESKTOP_FILE_PID\":\"7474\",\"GTK_IM_MODULE\":\"ibus\",\"XDG_GREETER_DATA_DIR\":\"/var/lib/lightdm-data/lucafrosini\",\"XDG_SESSION_TYPE\":\"x11\",\"NLSPATH\":\"/usr/dt/lib/nls/msg/%L/%N.cat\",\"TERMINATOR_UUID\":\"urn:uuid:7e1ff3e9-f2ca-4c64-b5db-922901ce32dc\",\"XDG_MENU_PREFIX\":\"gnome-\",\"LC_ADDRESS\":\"it_IT.UTF-8\",\"ghn-update-interval-in-secs\":\"60\",\"XDG_CONFIG_DIRS\":\"/etc/xdg/xdg-ubuntu:/usr/share/upstart/xdg:/etc/xdg\",\"XDG_CURRENT_DESKTOP\":\"Unity\",\"QT_LINUX_ACCESSIBILITY_ALWAYS_ON\":\"1\",\"GHN_HOME\":\"/home/lucafrosini/Desktop/SmartGears-Bundle/SmartGears\",\"XAUTHORITY\":\"/home/lucafrosini/.Xauthority\",\"GDM_LANG\":\"en_US\",\"XDG_SEAT\":\"seat0\",\"SmartGears\":\"2.2.0-SNAPSHOT\",\"CATALINA_PID\":\"/home/lucafrosini/Desktop/SmartGears-Bundle/tomcat.pid\",\"XDG_SESSION_ID\":\"c2\",\"XDG_SEAT_PATH\":\"/org/freedesktop/DisplayManager/Seat0\",\"XDG_VTNR\":\"7\",\"LC_TIME\":\"it_IT.UTF-8\",\"GNOME_KEYRING_CONTROL\":\"\",\"GTK_MODULES\":\"gail:atk-bridge:unity-gtk-module\",\"LC_TELEPHONE\":\"it_IT.UTF-8\",\"SHLVL\":\"2\",\"XFILESEARCHPATH\":\"/usr/dt/app-defaults/%L/Dt\",\"GTK2_MODULES\":\"overlay-scrollbar\",\"COMPIZ_BIN_PATH\":\"/usr/bin/\",\"COLORTERM\":\"gnome-terminal\",\"JOB\":\"unity-settings-daemon\",\"UPSTART_JOB\":\"unity7\",\"LC_NAME\":\"it_IT.UTF-8\",\"IBUS_DISABLE_SNOOPER\":\"1\",\"XDG_DATA_DIRS\":\"/usr/share/ubuntu:/usr/share/gnome:/usr/local/share/:/usr/share/:/var/lib/snapd/desktop\",\"QT_QPA_PLATFORMTHEME\":\"appmenu-qt5\",\"QT_IM_MODULE\":\"ibus\",\"GIO_LAUNCHED_DESKTOP_FILE\":\"/usr/share/applications/terminator.desktop\",\"UPSTART_INSTANCE\":\"\",\"LOGNAME\":\"lucafrosini\",\"WINDOWID\":\"81788932\",\"LC_PAPER\":\"it_IT.UTF-8\",\"SmartGearsDistribution\":\"2.0.0-SNAPSHOT\",\"SESSION\":\"ubuntu\",\"GPG_AGENT_INFO\":\"/home/lucafrosini/.gnupg/S.gpg-agent:0:1\",\"LC_IDENTIFICATION\":\"it_IT.UTF-8\",\"DERBY_HOME\":\"/usr/lib/jvm/java-7-oracle/db\",\"XMODIFIERS\":\"@im=ibus\",\"LD_LIBRARY_PATH\":\"/home/lucafrosini/MATLAB/MATLAB_Runtime/v90/runtime/glnxa64\",\"UPSTART_EVENTS\":\"xsession started\",\"OLDPWD\":\"/home/lucafrosini/Desktop/SmartGears-Bundle\",\"DBUS_SESSION_BUS_ADDRESS\":\"unix:abstract=/tmp/dbus-mf1KaSOYwP\",\"SHELL\":\"/bin/bash\",\"GNOME_KEYRING_PID\":\"\",\"LANGUAGE\":\"en_US\",\"QT_ACCESSIBILITY\":\"1\",\"DESKTOP_SESSION\":\"ubuntu\",\"INSTANCE\":\"\",\"DISPLAY\":\":0\",\"CLUTTER_IM_MODULE\":\"xim\",\"LC_MONETARY\":\"it_IT.UTF-8\",\"USER\":\"lucafrosini\",\"CATALINA_HOME\":\"/home/lucafrosini/Desktop/SmartGears-Bundle/tomcat\",\"HOME\":\"/home/lucafrosini\",\"LESSOPEN\":\"| /usr/bin/lesspipe %s\",\"QT4_IM_MODULE\":\"xim\",\"CATALINA_OPTS\":\"-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n -Xmx2000m -Xms2000m\",\"DEFAULTS_PATH\":\"/usr/share/gconf/ubuntu.default.path\",\"ORBIT_SOCKETDIR\":\"/tmp/orbit-lucafrosini\",\"XDG_SESSION_DESKTOP\":\"ubuntu\",\"LC_MEASUREMENT\":\"it_IT.UTF-8\",\"BUNDLE_HOME\":\"/home/lucafrosini/Desktop/SmartGears-Bundle\",\"XDG_RUNTIME_DIR\":\"/run/user/1000\",\"LANG\":\"en_US.UTF-8\"}},{\"@class\":\"ConsistsOf\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"ContainerStateFacet\",\"header\":null,\"value\":\"started\"}},{\"@class\":\"HasVolatileMemory\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"MemoryFacet\",\"header\":null,\"size\":15736,\"used\":9042,\"unit\":\"MB\"},\"memoryType\":\"RAM\"},{\"@class\":\"HasVolatileMemory\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"MemoryFacet\",\"header\":null,\"size\":1917,\"used\":374,\"unit\":\"MB\",\"jvmMaxMemory\":1917},\"memoryType\":\"JVM\"},{\"@class\":\"HasPersistentMemory\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"MemoryFacet\",\"header\":null,\"size\":273894,\"used\":101270,\"unit\":\"MB\"}}],\"isRelatedTo\":[]}";
	public static final String ESERVICE = "{\"@class\":\"EService\",\"header\":{\"@class\":\"Header\",\"uuid\":\"3ace4bd0-e5cd-49a3-97a8-a0a9468ce6d4\",\"creator\":null, \"creationTime\":null, \"lastUpdateTime\":null},\"consistsOf\":[{\"@class\":\"IsIdentifiedBy\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"SoftwareFacet\",\"header\":null,\"name\":\"WhnManager\",\"group\":\"VREManagement\",\"version\":\"2.0.0-SNAPSHOT\",\"description\":\"Web Hosting Node Service\",\"qualifier\":null,\"optional\":false}},{\"@class\":\"ConsistsOf\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"AccessPointFacet\",\"header\":null,\"entryName\":\"whnmanager\",\"endpoint\":\"http://pc-frosini.isti.cnr.it:8080/whn-manager/gcube/vremanagement/ws/whnmanager\",\"protocol\":null,\"description\":null,\"authorization\": {\"@class\":\"ValueSchema\",\"value\":\"gcube-token\",\"schema\":null},\"properties\":null}},{\"@class\":\"ConsistsOf\",\"header\":null,\"relationProperty\":null,\"target\":{\"@class\":\"AccessPointFacet\",\"header\":null,\"entryName\":\"WhnManager-remote-management\",\"endpoint\":\"http://pc-frosini.isti.cnr.it:8080/whn-manager/gcube/resource\",\"protocol\":null,\"description\":null,\"authorization\":{\"@class\":\"ValueSchema\",\"value\":\"gcube-token\",\"schema\":null},\"properties\":null}}],\"isRelatedTo\":[]}";
	
	@Test
	public void testHostingNode() throws JsonParseException,
			JsonMappingException, IOException, ResourceRegistryException {
		HostingNode hostingNode = ISMapper.unmarshal(HostingNode.class,
				HOSTING_NODE);
		logger.debug("{}", hostingNode);

		ResourceManagement resourceManagement = new ResourceManagement();
		resourceManagement.setElementType(HostingNode.NAME);
		resourceManagement.setJSON(HOSTING_NODE);

		String hnJson = resourceManagement.create();
		logger.debug("Created : {}", hnJson);
		hostingNode = ISMapper.unmarshal(HostingNode.class, hnJson);
		logger.debug("Unmarshalled {} {}", HostingNode.NAME, hostingNode);

		UUID hnUUID = hostingNode.getHeader().getUUID();

		resourceManagement = new ResourceManagement();
		resourceManagement.setUUID(hnUUID);
		String read = resourceManagement.read();
		HostingNode readHN = ISMapper.unmarshal(HostingNode.class, read);
		logger.debug("Read {} {}", HostingNode.NAME, readHN);

		Assert.assertTrue(hnUUID.compareTo(readHN.getHeader().getUUID()) == 0);

		resourceManagement = new ResourceManagement();
		resourceManagement.setUUID(hnUUID);
		boolean deleted = resourceManagement.delete();
		Assert.assertTrue(deleted);
	}

	public void deleteResource() throws Exception {
		UUID uuid = UUID.fromString("");
		ResourceManagement resourceManagement = new ResourceManagement();
		resourceManagement.setUUID(uuid);
		//resourceManagement.removeFromContext();
		boolean deleted = resourceManagement.delete();
		Assert.assertTrue(deleted);
		
	}
	
	
	@Test
	public void testEService() throws JsonParseException, JsonMappingException,
			IOException, ResourceRegistryException {
		EService eService = ISMapper.unmarshal(EService.class, ESERVICE);
		logger.debug("{}", eService);

		ResourceManagement resourceManagement = new ResourceManagement();
		resourceManagement.setElementType(EService.NAME);
		resourceManagement.setJSON(ESERVICE);

		String json = resourceManagement.create();
		eService = ISMapper.unmarshal(EService.class, json);
		logger.debug("Created {} {}", EService.NAME, eService);

		UUID eServiceUUID = eService.getHeader().getUUID();

		resourceManagement = new ResourceManagement();
		resourceManagement.setUUID(eServiceUUID);
		String read = resourceManagement.read();
		EService readEService = ISMapper.unmarshal(EService.class, read);
		logger.debug("Read {} {}", EService.NAME, readEService);

		Assert.assertTrue(eServiceUUID.compareTo(readEService.getHeader()
				.getUUID()) == 0);

		resourceManagement = new ResourceManagement();
		resourceManagement.setUUID(eServiceUUID);
		boolean deleted = resourceManagement.delete();
		Assert.assertTrue(deleted);

	}

	public static final String MEMORY_TYPE = "memoryType";
	public static final String MEMORY_TYPE_RAM = "RAM";
	public static final String MEMORY_TYPE_JVM = "JVM";
	public static final String JVM_MAX_MEMORY = "jvmMaxMemory";

	@Test
	public void testHostingNodeOperations() throws ResourceRegistryException,
			IOException {
		UUID uuid = UUID.randomUUID();

		HostingNode hostingNode = new HostingNodeImpl();
		Header header = new HeaderImpl(uuid);
		hostingNode.setHeader(header);

		NetworkingFacet networkingFacet = new NetworkingFacetImpl();
		try {
			networkingFacet.setIPAddress(InetAddress.getLocalHost()
					.getHostAddress());
		} catch (UnknownHostException e) {
			logger.warn("unable to detect the IP address of the host");
		}
		networkingFacet.setHostName("pc-frosini.isti.cnr.it");
		networkingFacet.setDomainName(getDomain(networkingFacet.getHostName()));
		networkingFacet.setAdditionalProperty("Port", 8080);

		PropagationConstraint propagationConstraint = new PropagationConstraintImpl();
		propagationConstraint
				.setRemoveConstraint(RemoveConstraint.cascadeWhenOrphan);

		IsIdentifiedBy<HostingNode, NetworkingFacet> isIdentifiedBy = new IsIdentifiedByImpl<>(
				hostingNode, networkingFacet, propagationConstraint);
		hostingNode.addFacet(isIdentifiedBy);

		List<CPUFacet> cpuFacets = getCPUFacets();
		for (CPUFacet cpuFacet : cpuFacets) {
			ConsistsOf<HostingNode, CPUFacet> consistsOf = new ConsistsOfImpl<HostingNode, CPUFacet>(
					hostingNode, cpuFacet, propagationConstraint);
			hostingNode.addFacet(consistsOf);
		}

		SoftwareFacet softwareFacet = new SoftwareFacetImpl();
		OperatingSystemMXBean mxbean = ManagementFactory
				.getOperatingSystemMXBean();
		softwareFacet.setGroup(mxbean.getName()); // softwareFacet.setGroup(System.getProperty("os.name"));
		softwareFacet.setName(mxbean.getArch()); // softwareFacet.setName(System.getProperty("os.arch"));
		softwareFacet.setVersion(mxbean.getVersion()); // softwareFacet.setName(System.getProperty("os.version"));
		ConsistsOf<HostingNode, SoftwareFacet> sfR = new ConsistsOfImpl<HostingNode, SoftwareFacet>(
				hostingNode, softwareFacet, propagationConstraint);
		hostingNode.addFacet(sfR);

		SimplePropertyFacet simplePropertyFacet = addEnvironmentVariables();
		ConsistsOf<HostingNode, SimplePropertyFacet> spfR = new ConsistsOfImpl<HostingNode, SimplePropertyFacet>(
				hostingNode, simplePropertyFacet, propagationConstraint);
		hostingNode.addFacet(spfR);

		ContainerStateFacet containerStateFacet = getContainerStateFacet(null);
		hostingNode.addFacet(containerStateFacet);

		MemoryFacet ramFacet = getRamInfo(null);
		HasVolatileMemory<HostingNode, MemoryFacet> hasVolatileRAMMemory = new HasVolatileMemoryImpl<HostingNode, MemoryFacet>(
				hostingNode, ramFacet, propagationConstraint);
		hasVolatileRAMMemory
				.setAdditionalProperty(MEMORY_TYPE, MEMORY_TYPE_RAM);
		hostingNode.addFacet(hasVolatileRAMMemory);

		MemoryFacet jvmMemoryFacet = getJVMMemoryInfo(null);
		HasVolatileMemory<HostingNode, MemoryFacet> hasVolatileJVMMemory = new HasVolatileMemoryImpl<HostingNode, MemoryFacet>(
				hostingNode, jvmMemoryFacet, propagationConstraint);
		hasVolatileJVMMemory
				.setAdditionalProperty(MEMORY_TYPE, MEMORY_TYPE_JVM);
		hostingNode.addFacet(hasVolatileJVMMemory);

		MemoryFacet disk = getDiskSpace(null);
		HasPersistentMemory<HostingNode, MemoryFacet> hasPersistentMemory = new HasPersistentMemoryImpl<HostingNode, MemoryFacet>(
				hostingNode, disk, propagationConstraint);
		hostingNode.addFacet(hasPersistentMemory);

		ResourceManagement resourceManagement = new ResourceManagement();
		resourceManagement.setElementType(HostingNode.NAME);
		resourceManagement.setJSON(ISMapper.marshal(hostingNode));

		String json = resourceManagement.create();

		HostingNode hostingNodeToUpdate = ISMapper.unmarshal(HostingNode.class,
				json);

		
		/* Testing convenient methods */
		@SuppressWarnings("unchecked")
		List<? extends ConsistsOf<? extends Resource, ? extends Facet>> hasVolatileMemoryList = hostingNodeToUpdate.getConsistsOf(HasVolatileMemory.class);
		Assert.assertTrue(hasVolatileMemoryList.size()==2);
		
		@SuppressWarnings("unchecked")
		List<? extends ConsistsOf<? extends Resource, ? extends Facet>> hasPeristentMemoryList = hostingNodeToUpdate.getConsistsOf(HasPersistentMemory.class);
		Assert.assertTrue(hasPeristentMemoryList.size()==1);
		
		@SuppressWarnings("unchecked")
		List<? extends ConsistsOf<? extends Resource, MemoryFacet>> memoryList = hostingNodeToUpdate.getConsistsOf(ConsistsOf.class, MemoryFacet.class);
		Assert.assertTrue(memoryList.size()==3);
		
		List<ContainerStateFacet> csfList = hostingNodeToUpdate.getFacets(ContainerStateFacet.class);
		Assert.assertTrue(csfList.size()==1);
		
		List<SimplePropertyFacet> spfList = hostingNodeToUpdate.getFacets(SimplePropertyFacet.class);
		Assert.assertTrue(spfList.size()==1);
		
		List<SoftwareFacet> sfList = hostingNodeToUpdate.getFacets(SoftwareFacet.class);
		Assert.assertTrue(sfList.size()==1);
		
		List<CPUFacet> cfList = hostingNodeToUpdate.getFacets(CPUFacet.class);
		Assert.assertTrue(cfList.size()==4);
		
		List<NetworkingFacet> nfList = hostingNodeToUpdate.getFacets(NetworkingFacet.class);
		Assert.assertTrue(nfList.size()==1);
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<IsIdentifiedBy> isbnfList = hostingNodeToUpdate.getConsistsOf(IsIdentifiedBy.class, NetworkingFacet.class);
		Assert.assertTrue(isbnfList.size()==1);
		
		/* Testing convenient methods */
		
		
		List<ConsistsOf<? extends Resource, ? extends Facet>> consistsOfToRemove = new ArrayList<>();

		List<ConsistsOf<? extends Resource, ? extends Facet>> consistsOfList = hostingNodeToUpdate
				.getConsistsOf();
		for (ConsistsOf<? extends Resource, ? extends Facet> c : consistsOfList) {

			if (c.getTarget() instanceof ContainerStateFacet) {
				containerStateFacet = (ContainerStateFacet) c.getTarget();
				containerStateFacet = getContainerStateFacet(containerStateFacet);
				continue;
			}

			if (c instanceof HasVolatileMemory) {
				String memoryType = (String) c
						.getAdditionalProperty(MEMORY_TYPE);
				if (memoryType.compareTo(MEMORY_TYPE_RAM) == 0) {
					ramFacet = (MemoryFacet) c.getTarget();
					ramFacet = getRamInfo(ramFacet);
					continue;
				}

				if (memoryType.compareTo(MEMORY_TYPE_JVM) == 0) {
					jvmMemoryFacet = (MemoryFacet) c.getTarget();
					jvmMemoryFacet = getJVMMemoryInfo(jvmMemoryFacet);
					continue;
				}

			}

			if (c instanceof HasPersistentMemory) {
				disk = (MemoryFacet) c.getTarget();
				disk = getDiskSpace(disk);
				continue;
			}

			consistsOfToRemove.add(c);

		}

		consistsOfList.removeAll(consistsOfToRemove);

		resourceManagement = new ResourceManagement();
		resourceManagement.setUUID(uuid);
		resourceManagement.setJSON(ISMapper.marshal(hostingNodeToUpdate));

		String updatedHN = resourceManagement.update();
		logger.debug("Updated {}", updatedHN);

		HostingNode updatedHostingNode = ISMapper.unmarshal(HostingNode.class,
				updatedHN);
		logger.debug("Updated Hosting Node {}", updatedHostingNode);

		resourceManagement = new ResourceManagement();
		resourceManagement.setUUID(uuid);
		boolean deleted = resourceManagement.delete();
		Assert.assertTrue(deleted);

	}

	private ContainerStateFacet getContainerStateFacet(
			ContainerStateFacet containerStateFacet) {
		if (containerStateFacet == null) {
			containerStateFacet = new ContainerStateFacetImpl();
		}
		containerStateFacet.setValue("ready");
		return containerStateFacet;
	}

	public static final String MESSAGE = "message";

	private MemoryFacet getDiskSpace(MemoryFacet memoryFacet) {
		if (memoryFacet == null) {
			memoryFacet = new MemoryFacetImpl();
		}

		long free = 0;
		long total = 0;
		try {
			FileStore fileStore = Files.getFileStore(Paths.get("./"));
			free = fileStore.getUsableSpace() / 1048576; // 1048576 = 1024*1024
															// user to convert
															// bytes in MByte
			total = fileStore.getTotalSpace() / 1048576; // 1048576 = 1024*1024
															// user to convert
															// bytes in MByte
		} catch (IOException ioe) {
			logger.warn("Unable to detect disk space information", ioe);
			memoryFacet.setAdditionalProperty(MESSAGE,
					"Unable to detect disk space information.");
		}

		memoryFacet.setUnit(MemoryUnit.MB);
		memoryFacet.setSize(total);
		memoryFacet.setUsed(total - free);

		return memoryFacet;
	}

	@SuppressWarnings("restriction")
	private MemoryFacet getRamInfo(MemoryFacet memoryFacet) {
		if (memoryFacet == null) {
			memoryFacet = new MemoryFacetImpl();
		}

		OperatingSystemMXBean mxbean = ManagementFactory
				.getOperatingSystemMXBean();
		com.sun.management.OperatingSystemMXBean sunmxbean = (com.sun.management.OperatingSystemMXBean) mxbean;
		long freeMemory = sunmxbean.getFreePhysicalMemorySize() / 1048576; // in
																			// MB
		long totalMemory = sunmxbean.getTotalPhysicalMemorySize() / 1048576; // in
																				// MB

		memoryFacet.setUnit(MemoryUnit.MB);
		memoryFacet.setSize(totalMemory);
		memoryFacet.setUsed(totalMemory - freeMemory);

		return memoryFacet;
	}

	private MemoryFacet getJVMMemoryInfo(MemoryFacet memoryFacet) {
		if (memoryFacet == null) {
			memoryFacet = new MemoryFacetImpl();
		}

		long jvmFreeMemory = Runtime.getRuntime().freeMemory() / 1048576; // 1048576
																			// =
																			// 1024*1024
																			// user
																			// to
																			// convert
																			// bytes
																			// in
																			// MByte
		long jvmTotalMemory = Runtime.getRuntime().totalMemory() / 1048576; // 1048576
																			// =
																			// 1024*1024
																			// user
																			// to
																			// convert
																			// bytes
																			// in
																			// MByte
		long jvmMaxMemory = Runtime.getRuntime().maxMemory() / 1048576; // 1048576
																		// =
																		// 1024*1024
																		// user
																		// to
																		// convert
																		// bytes
																		// in
																		// MByte

		memoryFacet.setUnit(MemoryUnit.MB);
		memoryFacet.setSize(jvmTotalMemory);
		memoryFacet.setUsed(jvmTotalMemory - jvmFreeMemory);
		memoryFacet.setAdditionalProperty(JVM_MAX_MEMORY, jvmMaxMemory);

		return memoryFacet;
	}

	private static String sanitizeKey(String key) {
		return key.trim().replace(" ", "_");
	}

	private SimplePropertyFacet addEnvironmentVariables() {

		Map<String, String> map = new HashMap<String, String>();
		map.putAll(System.getenv());

		SimplePropertyFacet simplePropertyFacet = new SimplePropertyFacetImpl();
		simplePropertyFacet.setName("ENVIRONMENT_VARIABLES");
		simplePropertyFacet.setValue("");

		for (Map.Entry<String, String> entry : map.entrySet()) {
			String varname = entry.getKey();
			if ((varname.compareToIgnoreCase("CLASSPATH") == 0)
					|| (varname.compareToIgnoreCase("PATH") == 0)
					|| (varname.contains("SSH")) || (varname.contains("MAIL"))
					|| (varname.compareToIgnoreCase("LS_COLORS") == 0)) {
				continue;
			}

			simplePropertyFacet.setAdditionalProperty(
					sanitizeKey(entry.getKey()), entry.getValue());

		}

		simplePropertyFacet.setAdditionalProperty("Java",
				System.getProperty("java.version"));

		return simplePropertyFacet;
	}

	private static String getDomain(String hostname) {
		try {
			Pattern pattern = Pattern
					.compile("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})");
			Matcher regexMatcher = pattern.matcher(hostname);
			if (regexMatcher.matches()) { // it's an IP address, nothing to trim
				return hostname;
			}
			return hostname.substring(hostname.indexOf(".") + 1);
		} catch (Exception e) {
			logger.warn("Error while getting domain from hostname");
			return hostname;
		}
	}

	public static final String CPU_PROCESSOR = "processor";
	public static final String CPU_VENDOR_ID = "vendor_id";
	public static final String CPU_MODEL_NAME = "model name";
	public static final String CPU_CPU_MHZ = "cpu MHz";
	public static final String CPU_MODEL_T = "model\t";
	public static final String CPU_MODEL_B = "model\b";
	public static final String CPU_MODEL_NUMBER = "modelNumber";

	public static List<CPUFacet> getCPUFacets() {

		List<CPUFacet> cpuFacets = new ArrayList<>();

		File file = new File("/proc/cpuinfo");

		if (!file.exists()) {
			logger.warn("cannot acquire CPU info (no /proc/cpuinfo)");
			return cpuFacets;
		}

		BufferedReader input = null;

		try {
			input = new BufferedReader(new FileReader(file));

			String line = null;

			CPUFacet cpuFacet = null;

			while ((line = input.readLine()) != null) {

				if ((line.startsWith(CPU_PROCESSOR))) { // add the current
														// processor to the map
					cpuFacet = new CPUFacetImpl();
					cpuFacets.add(cpuFacet);
				}

				try {
					if (line.contains(CPU_VENDOR_ID)) {
						cpuFacet.setVendor(line.split(":")[1].trim());
						continue;
					}
				} catch (Exception e) {
					continue;
				}

				try {
					if (line.contains(CPU_MODEL_NAME)) {
						cpuFacet.setModel(line.split(":")[1].trim());
						continue;
					}
				} catch (Exception e) {
					continue;
				}

				try {
					if (line.contains(CPU_CPU_MHZ)) {
						cpuFacet.setClockSpeed(line.split(":")[1].trim());
						continue;
					}
				} catch (Exception e) {
					continue;
				}

				try {
					if ((line.contains(CPU_MODEL_T))
							|| (line.contains(CPU_MODEL_B))) {
						cpuFacet.setAdditionalProperty(CPU_MODEL_NUMBER,
								line.split(":")[1].trim());
						continue;
					}
				} catch (Exception e) {
					continue;
				}

				try {
					String[] nameValue = line.split(":");
					cpuFacet.setAdditionalProperty(sanitizeKey(nameValue[0]),
							line.split(":")[1].trim());
				} catch (Exception e) {

				}

			}
		} catch (Exception e) {
			logger.warn("unable to acquire CPU info", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.warn("unable to close stream", e);
				}
			}
		}
		return cpuFacets;
	}
}
