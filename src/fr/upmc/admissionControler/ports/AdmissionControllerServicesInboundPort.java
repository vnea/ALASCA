package fr.upmc.admissionControler.ports;

import java.util.Map;

import fr.upmc.admissionControler.AdmissionController;
import fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

public class AdmissionControllerServicesInboundPort 
extends		AbstractInboundPort
implements	AdmissionControllerServicesI
{
	private static final long serialVersionUID = 1L;

	public	AdmissionControllerServicesInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AdmissionControllerServicesI.class, owner) ;

		assert owner instanceof AdmissionController ;
	}

	public AdmissionControllerServicesInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AdmissionControllerServicesI.class, owner);

		assert owner instanceof AdmissionController ;
	}

	@Override
	public void submitApplication(
			final RequestSubmissionOutboundPort rgrsobp,
			final String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		final AdmissionController ac = (AdmissionController) this.owner ;
		this.owner.handleRequestAsync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							ac.submitApplication(rgrsobp, RgRequestNotificationInboundPortURI);
							return null;
						}
					}) ;
	}

	@Override
	public void submitApplication(
			final Class<?> offeredInterface,
			final Map<String, String> mehtodNamesMap,
			final RequestSubmissionOutboundPort rgrsobp,
			final String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		final AdmissionController ac = (AdmissionController) this.owner ;
		this.owner.handleRequestAsync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							ac.submitApplication(
											offeredInterface, 
											mehtodNamesMap,
											rgrsobp, 
											RgRequestNotificationInboundPortURI
											);
							return null;
						}
					}) ;
		
	}
}
