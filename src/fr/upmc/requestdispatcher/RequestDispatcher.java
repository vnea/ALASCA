package fr.upmc.requestdispatcher;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementInboundPort;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.requestdispatcher.ports.RequestDispatcherManagementInboundPort;

/**
 * The class <code>RequestDispatcher</code> implements the component representing
 * a dispatcher in the data center.
 *
 * <p><strong>Description</strong></p>
 * 
 * The Application VM (AVM) component simulates the execution of web
 * applications by receiving requests, executing them and notifying the
 * emitter of the end of execution of its request (as a way to simulate
 * the return of the result).
 * 
 * The AVM is allocated cores on processors of a single computer and uses them
 * to execute the submitted requests.  It maintain a queue for requests waiting
 * a core to become idle before beginning their execution.
 * 
 * As a component, the AVM offers a request submission service through the
 * interface <code>RequestSubmissionI</code> implemented by
 * <code>RequestSubmissionInboundPort</code> inbound port. To notify the end
 * of the execution of requests, the AVM requires the interface
 * <code>RequestNotificationI</code> through the
 * <code>RequestNotificationOutboundPort</code> outbound port.
 * 
 * The AVM can be managed (essentially allocated cores) and it offers the
 * interface <code>ApplicationVMManagementI</code> through the inbound port
 * <code>ApplicationVMManagementInboundPort</code> for this.
 * 
 * AVM uses cores on processors to execute requests. To pass the request to
 * the cores, it requires the interface <code>ProcessorServicesI</code>
 * through <code>ProcessorServicesOutboundPort</code>. It receives the
 * notifications of the end of execution of the requests by offering the
 * interface <code>ProcessorServicesNotificationI</code> through the
 * inbound port <code>ProcessorServicesNotificationInboundPort</code>.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * TODO: complete!
 * 
 * <pre>
 * invariant	vmURI != null
 * invariant	applicationVMManagementInboundPortURI != null
 * invariant	requestSubmissionInboundPortURI != null
 * invariant	requestNotificationOutboundPortURI != null
 * </pre>
 * 
 * <p>Created on : 12 oct. 2016</p>
 * 
 * @author	<a href="mailto:morvan.lassauzay@orange.fr">Morvan Lassauzay</a>
 * @author	<a href="mailto:victor.nea@gmail.com">Victor Nea</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			RequestDispatcher
extends		AbstractComponent
implements	RequestNotificationHandlerI,
			RequestSubmissionHandlerI
{
	// ------------------------------------------------------------------------
	// Component internal state
	// ------------------------------------------------------------------------

	/** URI of this dispatcher.											*/
	protected String						dispatcherURI ;
	
	protected RequestDispatcherManagementInboundPort rdmip ;

	
	/** Inbound port offering the request submission service of the VM.		*/
	protected RequestSubmissionInboundPort	requestSubmissionInboundPort ;
	/** Outbound port used by the VM to notify tasks' termination.			*/
	protected RequestNotificationOutboundPort
											requestNotificationOutboundPort ;
	
	/** the output port used to send requests to the service provider.		*/
	protected RequestSubmissionOutboundPort		requestSubmissionOutboundPort ;
	/** the inbound port receiving end of execution notifications.			*/
	protected RequestNotificationInboundPort	requestNotificationInboundPort ;
	

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
	 * pre	requestSubmissionInboundPortURI != null
	 * pre	requestNotificationOutboundPortURI != null
	 * pre	requestSubmissionOutboundPort != null
	 * pre	requestNotificationInboundPort != null
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param dispatcherURI
	 * @param requestSubmissionInboundPortURI
	 * @param requestNotificationOutboundPortURI
	 * @param requestSubmissionOutboundPortURI
	 * @param requestNotificationInboundPortURI
	 * @throws Exception
	 */
	public				RequestDispatcher(
		String dispatcherURI,
		String managementInboundPortURI,
		String requestSubmissionInboundPortURI,
		String requestNotificationOutboundPortURI,
		String requestSubmissionOutboundPortURI,
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
		assert requestSubmissionOutboundPort != null ;
		assert requestNotificationInboundPort != null ;


		this.dispatcherURI = dispatcherURI ;

		// Interfaces and ports
		this.addOfferedInterface(RequestDispatcherManagementI.class) ;
		this.rdmip = new RequestDispatcherManagementInboundPort(
												managementInboundPortURI, this) ;
		
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
		this.requestSubmissionOutboundPort = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI, this) ;
		this.addPort(this.requestSubmissionOutboundPort) ;
		this.requestSubmissionOutboundPort.publishPort() ;

		this.addOfferedInterface(RequestNotificationI.class) ;
		this.requestNotificationInboundPort =
			new RequestNotificationInboundPort(requestNotificationInboundPortURI, this) ;
		this.addPort(this.requestNotificationInboundPort) ;
		this.requestNotificationInboundPort.publishPort() ;
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
		// Disconnect ports to the request emitter.
		try {
			if (this.requestNotificationOutboundPort.connected()) {
				this.requestNotificationOutboundPort.doDisconnection() ;
			}
			
			if (this.requestSubmissionOutboundPort.connected()) {
				this.requestSubmissionOutboundPort.doDisconnection() ;
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
	{
		// Submit the current request.
		this.logMessage("Request Dispatcher " + this.dispatcherURI + " is notified that request "+ r.getRequestURI() + " has ended.") ;
		this.requestSubmissionOutboundPort.submitRequestAndNotify(r) ;
	}

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

		this.requestNotificationOutboundPort.notifyRequestTermination(r) ;
	}

	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmissionAndNotify(fr.upmc.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {	
		// Submit the current request.
		this.logMessage("Request Dispatcher " + this.dispatcherURI + " is notified that request "+ r.getRequestURI() + " has ended.") ;
		this.requestSubmissionOutboundPort.submitRequestAndNotify(r) ;
	}
}
