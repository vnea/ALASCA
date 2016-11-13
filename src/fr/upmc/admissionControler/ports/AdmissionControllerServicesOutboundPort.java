package fr.upmc.admissionControler.ports;

import java.util.Map;

import fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

public class AdmissionControllerServicesOutboundPort 
extends		AbstractOutboundPort
implements	AdmissionControllerServicesI
{
	public				AdmissionControllerServicesOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AdmissionControllerServicesI.class, owner) ;
	}

	public				AdmissionControllerServicesOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AdmissionControllerServicesI.class, owner);
	}

	@Override
	public void submitApplication(
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		((AdmissionControllerServicesI)this.connector).
				submitApplication(rgrsobp, RgRequestNotificationInboundPortURI);
	}

	@Override
	public void submitApplication(
			Class<?> offeredInterface,
			Map<String, String> mehtodNamesMap,
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI) throws Exception {
		
		((AdmissionControllerServicesI)this.connector).
				submitApplication(offeredInterface, 
								  mehtodNamesMap,
								  rgrsobp, 
								  RgRequestNotificationInboundPortURI
								 );
	}

}
