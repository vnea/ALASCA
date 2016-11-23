package fr.upmc.admissionControler.connectors;

import java.util.Map;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI;

/**
 * The class <code>AdmissionControllerServicesConnector</code> implements 
 * a connector for ports exchanging through the interface 
 * <code>AdmissionControllerServicesI</code>.
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
public class AdmissionControllerServicesConnector
extends		AbstractConnector
implements	AdmissionControllerServicesI
{
	
	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public boolean submitApplication(
			RequestSubmissionOutboundPort rgsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		return ((AdmissionControllerServicesI)this.offering).
				submitApplication(rgsobp, RgRequestNotificationInboundPortURI);
	}

	/*
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(java.lang.Class, java.util.Map, fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public boolean submitApplication(
			Class<?> offeredInterface,
			Map<String, String> mehtodNamesMap,
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		return ((AdmissionControllerServicesI)this.offering).
				submitApplication(
						offeredInterface, 
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
		
		((AdmissionControllerServicesI)this.offering).
		connectComputer(
				ComputerURI, 
				ComputerServicesInboundPortURI,
				ComputerStaticStateDataInboundPortURI, 
				ComputerDynamicStateDataInboundPortURI
				);
		
	}
	
	

}

