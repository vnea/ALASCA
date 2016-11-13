package fr.upmc.admissionControler.connectors;

import java.util.Map;

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

	@Override
	public void submitApplication(
			Class<?> offeredInterface,
			Map<String, String> mehtodNamesMap,
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		((AdmissionControllerServicesI)this.offering).
				submitApplication(
						offeredInterface, 
						mehtodNamesMap,
						rgrsobp, 
						RgRequestNotificationInboundPortURI
						);
	}
	
	

}

