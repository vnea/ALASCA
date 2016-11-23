package fr.upmc.admissionControler.interfaces;

import java.util.Map;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

/**
 * The interface <code>AdmissionControllerServicesI</code> defines the services 
 * offered by <code>AdmissionController</code> components (submit request
 * for application execution).
 *
 * <p><strong>Description</strong></p>
 * 
 * TODO: Create and connect resources for application's execution.
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
public interface AdmissionControllerServicesI 
extends		OfferedI,
			RequiredI
{
	
	/**
	 * if cores are free on processors of the data center then a 
	 * <code>RequestDispatcher</code> and many <code>ApplicationVM</code>
	 *  are create and connect to execute the submitted application.
	 *  
	 * @param rgrsobp								Required port for request submission to the dispatcher
	 * @param RgRequestNotificationInboundPortURI   offered itf for notification from the dispatcher
	 * @return TODO
	 * @throws Exception
	 */
	public boolean submitApplication(
			RequestSubmissionOutboundPort rgsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception ;
	
	/**
	 * if cores are free on processors of the data center then a 
	 * <code>RequestDispatcher</code> and many <code>ApplicationVM</code>
	 *  are create and connect to execute the submitted application.
	 *  
	 * @param offeredInterface						Itf provided for connector
	 * @param mehtodNamesMap						Map beetween methods of provided itf and required itf
	 * @param rgrsobp								Required port for request submission to the dispatcher
	 * @param RgRequestNotificationInboundPortURI	offered itf for notification from the dispatcher
	 * @return TODO
	 * @throws Exception
	 */
	public boolean submitApplication(
			Class<?> offeredInterface,
			Map<String, String> mehtodNamesMap,
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception ;
	
	/**
	 * Connect a computer with the AdmissionController
	 * 
	 * @param ComputerURI
	 * @param ComputerServicesInboundPortURI
	 * @param ComputerStaticStateDataInboundPortURI
	 * @param ComputerDynamicStateDataInboundPortURI
	 * @throws Exception
	 */
	public void connectComputer (String ComputerURI,
			String ComputerServicesInboundPortURI,
			String ComputerStaticStateDataInboundPortURI,
			String ComputerDynamicStateDataInboundPortURI) throws Exception ;
}
