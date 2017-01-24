package fr.upmc.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.ports.AbstractControlledDataOutboundPort;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;

public class 			RequestDispatcherDynamicStateOutboundPort
extends		AbstractControlledDataOutboundPort
{
	private static final long	serialVersionUID = 1L ;
	protected String			requestDispatcherURI ;

	public				RequestDispatcherDynamicStateOutboundPort(
		ComponentI owner,
		String requestDispatcherURI
		) throws Exception
	{
		super(owner) ;
		this.requestDispatcherURI = requestDispatcherURI ;

		assert	owner instanceof RequestDispatcherStateDataConsumerI ;
	}

	public				RequestDispatcherDynamicStateOutboundPort(
		String uri,
		ComponentI owner,
		String requestDispatcherURI
		) throws Exception
	{
		super(uri, owner);
		this.requestDispatcherURI = requestDispatcherURI ;

		assert	owner instanceof RequestDispatcherStateDataConsumerI ;
	}

	/**
	 * @see fr.upmc.components.interfaces.DataRequiredI.PushI#receive(fr.upmc.components.interfaces.DataRequiredI.DataI)
	 */
	@Override
	public void			receive(DataRequiredI.DataI d)
	throws Exception
	{
		((RequestDispatcherStateDataConsumerI)this.owner).
						acceptRequestDispatcherDynamicData(this.requestDispatcherURI,
												  (RequestDispatcherDynamicStateI) d) ;
	}
}
