package fr.upmc.requestdispatcher.interfaces;

import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;

/**
 * The interface <code>RequestDispatcherManagementI</code> defines the management
 * actions provided by the request dispatcher component.
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


public interface RequestDispatcherManagementI {

		/**
		 * Connect a request receiver with the request dispatcher
		 * 
		 * @param requestSubmissionInboundPortURI 	URI of offered submission port for request
		 * @param rnop								required port for notification
		 * @throws Exception	
		 */
		public void addRequestReceiver(
				final String requestSubmissionInboundPortURI, 
				final RequestNotificationOutboundPort rnop
				) throws Exception;
		
		/**
		 * Connect a request receiver with the request dispatcher
		 * 
		 * @param requestSubmissionInboundPortURI	URI of offered submission port for request
		 * @param rnop								required port for notification
		 * @param connectorClass 					type of connector used for connection
		 * @throws Exception
		 */
		public void addRequestReceiver(
				String requestSubmissionInboundPortURI, 
				RequestNotificationOutboundPort rnop,
				Class<?> connectorClass
				) throws Exception;
}
