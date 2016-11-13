package fr.upmc.requestdispatcher.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;

/**
 * The class <code>RequestGeneratorManagementConnector</code> implements a
 * standard client/server connector for the management request generator
 * management interface.
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
public class			RequestDispatcherManagementConnector
extends		AbstractConnector
implements	RequestDispatcherManagementI
{
	
	@Override
	public void addRequestReceiver(
			final String requestSubmissionInboundPortURI, 
			final RequestNotificationOutboundPort rnop)
			throws Exception {
		((RequestDispatcherManagementI)this.offering).
						addRequestReceiver(requestSubmissionInboundPortURI, rnop);
	}
	
	@Override
	public void addRequestReceiver(
			String requestSubmissionInboundPortURI, 
			RequestNotificationOutboundPort rnop,
			Class<?> connectorClass
			) throws Exception {
		((RequestDispatcherManagementI)this.offering).
							addRequestReceiver(requestSubmissionInboundPortURI, 
												rnop,
												connectorClass);
	}
}

