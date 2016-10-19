package fr.upmc.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.interfaces.RequestGeneratorManagementI;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;

/**
 * The class <code>RequestGeneratorManagementOutboundPort</code> implements the
 * outbound port through which one calls the component management methods.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 5 mai 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			RequestDispatcherManagementOutboundPort
extends		AbstractOutboundPort
implements	RequestDispatcherManagementI
{
	public				RequestDispatcherManagementOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(RequestGeneratorManagementI.class, owner) ;

		assert	owner != null ;
	}

	public				RequestDispatcherManagementOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RequestGeneratorManagementI.class, owner) ;

		assert	uri != null && owner != null ;
	}

	@Override
	public void addRequestSubmissioner(String requestSubmissionInboundPortURI, RequestNotificationOutboundPort rnop)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
}
