package fr.upmc.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.interfaces.RequestGeneratorManagementI;
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
		super(RequestGeneratorManagementI.class, owner) ;

		assert	owner != null && owner instanceof RequestGenerator ;
	}

	public				RequestDispatcherManagementInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RequestGeneratorManagementI.class, owner);

		assert	owner != null && owner instanceof RequestGenerator ;
	}
}
