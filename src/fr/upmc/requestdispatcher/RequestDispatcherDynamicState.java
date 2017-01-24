package fr.upmc.requestdispatcher;

import fr.upmc.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;

public class RequestDispatcherDynamicState
implements RequestDispatcherDynamicStateI
{
	private static final long serialVersionUID = 1L;
	
	/** URI of the request dispatcher to which this dynamic state relates.			*/
	protected final String		requestDispatcherURI ;
	
	/** */
	protected final Double movingAverage;
	
	public				RequestDispatcherDynamicState(
			String requestDispatcherURI,
			double movingAverage
			) throws Exception
	{
		super() ;

		this.requestDispatcherURI = requestDispatcherURI ;
		this.movingAverage = movingAverage;
		
	}

	@Override
	public Double getMovingAverage()
	{
		return movingAverage;
	}

}
