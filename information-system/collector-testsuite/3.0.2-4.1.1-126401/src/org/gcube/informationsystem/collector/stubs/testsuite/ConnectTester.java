package org.gcube.informationsystem.collector.stubs.testsuite;

import java.net.URL;
import java.rmi.RemoteException;

import org.gcube.common.core.contexts.GCUBERemotePortTypeContext;
import org.gcube.common.core.scope.GCUBEScope;
import org.gcube.common.core.types.VOID;
import org.gcube.common.core.utils.logging.GCUBEClientLog;
import org.gcube.informationsystem.collector.stubs.BackupFailedFaultType;
import org.gcube.informationsystem.collector.stubs.ShutdownFailedFaultType;
import org.gcube.informationsystem.collector.stubs.XMLStorageAccessPortType;
import org.gcube.informationsystem.collector.stubs.XMLStorageNotAvailableFaultType;
import org.gcube.informationsystem.collector.stubs.service.XMLStorageAccessServiceLocator;

/**
 * Tester for <em>Connect</em> operation of the
 * <em>gcube/informationsystem/collector/XMLStorageAccess</em> portType
 *
 * @author Manuele Simi (ISTI-CNR)
 *
 */
public class ConnectTester {

    private static GCUBEClientLog logger = new GCUBEClientLog(ConnectTester.class);

    /**
     * @param args
     *            <ol>
     *            <li>IC host
     *            <li>IC port
     *            <li>Caller Scope
     *            </ol>
     */
    public static void main(String[] args) {

	
	 if (args.length != 3) { 
	     	logger.fatal("Usage: ConnectTester <host> <port> <Scope>");
	 	return; 
	 }
	 
	 final String portTypeURI = "http://" + args[0] + ":" + args[1] + "/wsrf/services/gcube/informationsystem/collector/XMLStorageAccess";
	//final String portTypeURI = "http://node10.d.d4science.research-infrastructures.eu:8080/wsrf/services/gcube/informationsystem/collector/XMLStorageAccess";

	XMLStorageAccessPortType port = null;
	try {
	    port = new XMLStorageAccessServiceLocator().getXMLStorageAccessPortTypePort(new URL(portTypeURI));
	    port = GCUBERemotePortTypeContext.getProxy(port, GCUBEScope.getScope(args[2]));
	} catch (Exception e) {
	    logger.error("",e);
	}

	logger.info("Submitting shutdown request to " + portTypeURI+ "...");

	try {
	    port.connect(new VOID());
	} catch (XMLStorageNotAvailableFaultType e) {
	    logger.error("",e);
	} catch (ShutdownFailedFaultType e) {
	    logger.error("",e);
	} catch (BackupFailedFaultType e) {
	    logger.error("",e);
	} catch (RemoteException e) {
	    logger.error("",e);
	}

    }


}
