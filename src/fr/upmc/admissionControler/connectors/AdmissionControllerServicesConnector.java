package fr.upmc.admissionControler.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI;


public class AdmissionControllerServicesConnector
extends		AbstractConnector
implements	AdmissionControllerServicesI
{
	
	@Override
	public void submitApplication(
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		((AdmissionControllerServicesI)this.offering).
				submitApplication(rgrsobp, RgRequestNotificationInboundPortURI);
	}
	
	

}

