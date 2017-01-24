package fr.upmc.admissioncontroller.ports;

import java.util.Map;

import fr.upmc.admissioncontroller.AdmissionController;
import fr.upmc.admissioncontroller.interfaces.AdmissionControllerServicesI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

/**
 * The class <code>AdmissionControllerServicesInboudPort</code> implements an inbound
 * port offering the <code>AdmissionControllerServicesI</code> interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 15 novembre 2016</p>
 * 
 * @author	<a href="mailto:morvanlassauzay@gmail.com">Morvan Lassauzay</a>
 * @author  <a href="mailto:victor.nea@gmail.com">Victor Nea</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
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

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public boolean submitApplication(
			final RequestSubmissionOutboundPort rgrsobp,
			final String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		final AdmissionController ac = (AdmissionController) this.owner ;
		return ac.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return ac.submitApplication(rgrsobp, 
														RgRequestNotificationInboundPortURI);
							
						}
					}) ;
	}

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(java.lang.Class, java.util.Map, fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public boolean submitApplication(
			final Class<?> offeredInterface,
			final Map<String, String> mehtodNamesMap,
			final RequestSubmissionOutboundPort rgrsobp,
			final String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		final AdmissionController ac = (AdmissionController) this.owner ;
		return ac.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return ac.submitApplication(
											offeredInterface, 
											mehtodNamesMap,
											rgrsobp, 
											RgRequestNotificationInboundPortURI
											);
						}
					}) ;
		
	}

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#connectComputer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void connectComputer(
			final String ComputerURI, 
			final String ComputerServicesInboundPortURI,
			final String ComputerStaticStateDataInboundPortURI, 
			final String ComputerDynamicStateDataInboundPortURI)
			throws Exception {
		
		final AdmissionController ac = (AdmissionController) this.owner ;
		this.owner.handleRequestAsync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							ac.connectComputer(
									ComputerURI, 
									ComputerServicesInboundPortURI,
									ComputerStaticStateDataInboundPortURI, 
									ComputerDynamicStateDataInboundPortURI
									);
							
							return null;
						}
					}) ;
		
	}
}
