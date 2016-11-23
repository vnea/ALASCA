package fr.upmc.requestdispatcher;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.requestdispatcher.ports.RequestDispatcherManagementInboundPort;

/**
 * The class <code>RequestDispatcher</code> implements a component that distributes
 * requests by submitting them to many Application VMs components.
 *
 * <p><strong>Description</strong></p>
 * 
 * A request has a processing time and an arrival process that both follow an
 * exponential probability distribution.  The generation process is started by
 * a component <code>RequestGenerator</code>.
 * 
 * As a component, the Request Dispatcher offers a request submission service through the
 * interface <code>RequestSubmissionI</code> implemented by
 * <code>RequestSubmissionInboundPort</code> inbound port.
 * It also offers a request notification service through the
 * interface <code>RequestNotificationI</code> implemented by
 * <code>RequestNotificationInboundPort</code> inbound port.
 *  To forward requests, the Request Dispatcher requires the interface
 * <code>RequestSubmissionI</code> through the
 * <code>RequestSubmissionOutboundPort</code> outbound port
 *  To notify the end of the execution of requests, the Request Dispatcher requires the interface
 * <code>RequestNotificationI</code> through the
 * <code>RequestNotificationOutboundPort</code> outbound port.
 * 
 * The Request Dispatcher can be managed (essentially allocated cores) and it offers the
 * interface <code>RequestDispatcherManagementI</code> through the inbound port
 * <code>RequestDispatcherManagementInboundPort</code> for this.
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
public class			RequestDispatcher
extends		AbstractComponent
implements	RequestNotificationHandlerI,
			RequestSubmissionHandlerI,
			RequestDispatcherManagementI
{
	// ------------------------------------------------------------------------
	// Component internal state
	// ------------------------------------------------------------------------

	/** URI of this dispatcher.														*/
	protected String						dispatcherURI ;
	/** Inbound port offering the management interface.								*/
	protected RequestDispatcherManagementInboundPort rdmip ;
	/** Inbound port offering the request submission service of the Dispatcher.		*/
	protected RequestSubmissionInboundPort	requestSubmissionInboundPort ;
	/** Outbound port used by the Dispatcher to notify tasks' termination.			*/
	protected RequestNotificationOutboundPort
											requestNotificationOutboundPort ;
	
	/** Map between VM URIs and the output ports used to send requests 
	 * to the service provider of each VM.											*/
	protected Map<String, RequestSubmissionOutboundPort>		requestSubmissionOutboundPorts ;
	/** the inbound port receiving end of execution notifications by VMs			*/
	protected RequestNotificationInboundPort	requestNotificationInboundPort ;
	

	/** number of VMs connected 													*/
	protected int numAppVm ;
	/** number of VMs submission ports connected 									*/
	protected int numberOfRequestSubmissionOutboundPort;
	
	// ------------------------------------------------------------------------
	// Component constructor
	// ------------------------------------------------------------------------
	
	/**
	 * create a new application VM with the given URI, the given processor
	 * cores, and the URIs to be used to create and publish its inbound and
	 * outbound ports.
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * TODO: complete!
	 * 
	 * <pre>
	 * pre	dispatcherURI != null
	 * pre	managementInboundPortURI != null
	 * pre	requestSubmissionInboundPortURI != null
	 * pre	requestNotificationOutboundPortURI != null
	 * pre	requestNotificationInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param dispatcherURI
	 * @param managementInboundPortURI
	 * @param requestSubmissionInboundPortURI
	 * @param requestNotificationOutboundPortURI
	 * @param requestNotificationInboundPortURI
	 * @throws Exception
	 */
	public				RequestDispatcher(
		String dispatcherURI,
		String managementInboundPortURI,
		String requestSubmissionInboundPortURI,
		String requestNotificationOutboundPortURI,
		String requestNotificationInboundPortURI
		) throws Exception
	{
		// The normal thread pool is used to process component services, while
		// the scheduled one is used to schedule the pushes of dynamic state
		// when requested.
		super(1, 1) ;

		// Preconditions
		assert dispatcherURI != null ;
		assert managementInboundPortURI != null ;
		assert requestSubmissionInboundPortURI != null ;
		assert requestNotificationOutboundPortURI != null ;
		assert requestNotificationInboundPortURI != null ;


		this.dispatcherURI = dispatcherURI ;

		// Interfaces and ports
		this.addOfferedInterface(RequestDispatcherManagementI.class) ;
		this.rdmip = new RequestDispatcherManagementInboundPort(
												managementInboundPortURI, this) ;
		this.addPort(this.rdmip) ;
		this.rdmip.publishPort() ;
		
		this.addOfferedInterface(RequestSubmissionI.class) ;
		this.requestSubmissionInboundPort =
						new RequestSubmissionInboundPort(
										requestSubmissionInboundPortURI, this) ;
		this.addPort(this.requestSubmissionInboundPort) ;
		this.requestSubmissionInboundPort.publishPort() ;

		this.addRequiredInterface(RequestNotificationI.class) ;
		this.requestNotificationOutboundPort =
			new RequestNotificationOutboundPort(
									requestNotificationOutboundPortURI,
									this) ;
		this.addPort(this.requestNotificationOutboundPort) ;
		this.requestNotificationOutboundPort.publishPort() ;
		
		this.addRequiredInterface(RequestSubmissionI.class) ;
		requestSubmissionOutboundPorts = new LinkedHashMap<String, RequestSubmissionOutboundPort>(); 			

		this.addOfferedInterface(RequestNotificationI.class) ;
		this.requestNotificationInboundPort =
			new RequestNotificationInboundPort(requestNotificationInboundPortURI, this) ;
		this.addPort(this.requestNotificationInboundPort) ;
		this.requestNotificationInboundPort.publishPort() ;
	
		numAppVm = 0;
		numberOfRequestSubmissionOutboundPort = 0;
	}

	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		// Disconnect ports to the requests emitter.
		// and to the requests receivers
		try {
			if (this.requestNotificationOutboundPort.connected()) {
				this.requestNotificationOutboundPort.doDisconnection() ;
			}
			
			for (Entry<String, RequestSubmissionOutboundPort> entry :  requestSubmissionOutboundPorts.entrySet()) {
				RequestSubmissionOutboundPort rsop = entry.getValue();
				if (rsop.connected()) {
					rsop.doDisconnection() ;
				}				
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdown();
	}

	// ------------------------------------------------------------------------
	// Component services
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmission(fr.upmc.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void			acceptRequestSubmission(final RequestI r)
	throws Exception
	{}

	// ------------------------------------------------------------------------
	// Component internal services
	// ------------------------------------------------------------------------
	
	/**
	 * Forward execution's end notification for a request r previously
	 * submitted. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	r != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r	request that just terminated.
	 * @throws Exception
	 */
	@Override
	public void			acceptRequestTerminationNotification(RequestI r)
	throws Exception
	{
		assert	r != null ;
		this.logMessage("Request Dispatcher " + this.dispatcherURI + " is notified that request "+ r.getRequestURI() + " has ended.") ;
		this.requestNotificationOutboundPort.notifyRequestTermination(r) ;
	}

	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmissionAndNotify(fr.upmc.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {	
		// Forward the current request to next VM in the list.
		this.logMessage("Request Dispatcher " + this.dispatcherURI + " has received "+ r.getRequestURI()+".") ;
		
		Iterator<RequestSubmissionOutboundPort> it = this.requestSubmissionOutboundPorts.values().iterator();
		int i = 0;
		while (it.hasNext())
		{
			RequestSubmissionOutboundPort requestSubmissionOutboundPort = it.next();
			if (i == numAppVm) {
				requestSubmissionOutboundPort.submitRequestAndNotify(r) ;
			}
			i++;
		}
		
		numAppVm = (numAppVm + 1) % requestSubmissionOutboundPorts.size();
	}
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort)
	 */
	@Override
	public void addRequestReceiver(
			String requestSubmissionInboundPortURI, 
			RequestNotificationOutboundPort rnop
			) throws Exception {
		String requestSubmissionOutboundPortURI = "rsobp" + numberOfRequestSubmissionOutboundPort++;
		RequestSubmissionOutboundPort rdrsobp = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI, this);
		requestSubmissionOutboundPorts.put(requestSubmissionOutboundPortURI, rdrsobp);

		this.addPort(rdrsobp) ;
		rdrsobp.publishPort() ;
		
		rdrsobp.doConnection(
				requestSubmissionInboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName()) ;

		rnop.doConnection(
				requestNotificationInboundPort.getPortURI(),
					RequestNotificationConnector.class.getCanonicalName()) ;
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
		String requestSubmissionOutboundPortURI = "rsobp" + numberOfRequestSubmissionOutboundPort++;
		RequestSubmissionOutboundPort rdrsobp = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI, this);
		requestSubmissionOutboundPorts.put(requestSubmissionOutboundPortURI, rdrsobp);

		this.addPort(rdrsobp) ;
		rdrsobp.publishPort() ;
		
		rdrsobp.doConnection(
				requestSubmissionInboundPortURI,
				connectorClass.getCanonicalName()) ;

		rnop.doConnection(
				requestNotificationInboundPort.getPortURI(),
					RequestNotificationConnector.class.getCanonicalName()) ;
	}
}
