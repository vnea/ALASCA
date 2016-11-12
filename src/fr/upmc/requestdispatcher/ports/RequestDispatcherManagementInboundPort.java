package fr.upmc.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.requestdispatcher.RequestDispatcher;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;

/**
 * The class <code>RequestGeneratorManagementInboundPort</code> implements the
 * inbound port through which the component management methods are called.
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
public class			RequestDispatcherManagementInboundPort
extends		AbstractInboundPort
implements	RequestDispatcherManagementI
{
	private static final long serialVersionUID = 1L ;

	public				RequestDispatcherManagementInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(RequestDispatcherManagementI.class, owner) ;

		assert	owner != null && owner instanceof RequestDispatcher ;
	}

	public				RequestDispatcherManagementInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RequestDispatcherManagementI.class, owner);

		assert	owner != null && owner instanceof RequestDispatcher ;
	}

	@Override
	public void addRequestReceiver(
			final String requestSubmissionInboundPortURI, 
			final RequestNotificationOutboundPort rnop)
			throws Exception {
		
		final RequestDispatcher rd = (RequestDispatcher) this.owner ;
		this.owner.handleRequestAsync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							rd.addRequestReceiver(requestSubmissionInboundPortURI, rnop);
							return null;
						}
					}) ;
		
	}
}
