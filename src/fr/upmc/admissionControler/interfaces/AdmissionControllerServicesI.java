package fr.upmc.admissionControler.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

public interface AdmissionControllerServicesI 
extends		OfferedI,
			RequiredI
{
	
	public void submitApplication(
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception ;
	
}
