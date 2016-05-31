package org.gcube.application.aquamaps.aquamapsspeciesview.servlet.save;

import org.gcube.application.aquamaps.aquamapsspeciesview.client.rpc.data.save.SaveOperationProgress;
import org.gcube.application.aquamaps.aquamapsspeciesview.client.rpc.data.save.SaveRequest;
import org.gcube.application.framework.core.session.ASLSession;

public interface SaveHandler {

	public SaveOperationProgress getProgress();
	public void setRequest(ASLSession session,SaveRequest request);
	public void startProcess();
}
