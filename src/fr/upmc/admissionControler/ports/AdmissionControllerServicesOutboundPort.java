package fr.upmc.admissionControler.ports;

import java.util.Map;

import fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

/**
 * The class <code>AdmissionControllerServiceOutboundPort</code> implements an outbound
 * port requiring the <code>AdmissionControllerServicesI</code> interface.
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

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public boolean submitApplication(
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		return ((AdmissionControllerServicesI)this.connector).
				submitApplication(rgrsobp, RgRequestNotificationInboundPortURI);
	}

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(java.lang.Class, java.util.Map, fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public boolean submitApplication(
			Class<?> offeredInterface,
			Map<String, String> mehtodNamesMap,
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI) throws Exception {
		
		return ((AdmissionControllerServicesI)this.connector).
				submitApplication(offeredInterface, 
								  mehtodNamesMap,
								  rgrsobp, 
								  RgRequestNotificationInboundPortURI
								 );
	}

	/*
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#connectComputer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void connectComputer(
			String ComputerURI, 
			String ComputerServicesInboundPortURI,
			String ComputerStaticStateDataInboundPortURI, 
			String ComputerDynamicStateDataInboundPortURI)
			throws Exception {
		
		((AdmissionControllerServicesI)this.connector).
		connectComputer(ComputerURI, 
						ComputerServicesInboundPortURI,
						ComputerStaticStateDataInboundPortURI, 
						ComputerDynamicStateDataInboundPortURI
						 );
		
	}

}
