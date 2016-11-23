package fr.upmc.requestdispatcher.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;

/**
 * The class <code>RequestDispatcherManagementConnector</code> implements a
 * standard client/server connector for the management request dispatcher
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
 * <p>Created on : 15 novembre 2016</p>
 * 
 * @author	<a href="mailto:morvanlassauzay@gmail.com">Morvan Lassauzay</a>
 * @author  <a href="mailto:victor.nea@gmail.com">Victor Nea</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			RequestDispatcherManagementConnector
extends		AbstractConnector
implements	RequestDispatcherManagementI
{
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort)
	 */
	@Override
	public void addRequestReceiver(
			final String requestSubmissionInboundPortURI, 
			final RequestNotificationOutboundPort rnop)
			throws Exception {
		((RequestDispatcherManagementI)this.offering).
						addRequestReceiver(requestSubmissionInboundPortURI, rnop);
	}
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort, java.lang.Class)
	 */
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

