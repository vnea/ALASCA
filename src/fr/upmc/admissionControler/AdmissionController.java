package fr.upmc.admissionControler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI;
import fr.upmc.admissionControler.ports.AdmissionControllerServicesInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.hardware.tests.ComputerMonitor;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.utils.TimeProcessing;
import fr.upmc.requestdispatcher.RequestDispatcher;
import fr.upmc.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;

public class AdmissionController
extends		AbstractComponent
implements	AdmissionControllerServicesI
{

	/** URI of this dispatcher.											*/
	protected String				AdmissionControllerURI ;
	
	// ports
	protected AdmissionControllerServicesInboundPort acsip ;
	
	// Computers
	public static final int		NUMBER_OF_COMPUTERS = 2 ;
	public static final int		NUMBER_OF_PROCESSORS_PER_COMPUTER = 2 ;
	public static final int		NUMBER_OF_CORES_PER_PROCESSOR = 4 ;

	public static final String 	ComputerURIPrefix = "computer-";
	public static final String	ComputerServicesInboundPortURIPrefix = "cs-ibp-" ;
	public static final String	ComputerServicesOutboundPortURIPrefix = "cs-obp" ;
	public static final String	ComputerStaticStateDataInboundPortURIPrefix = "css-dip-" ;
	public static final String	ComputerStaticStateDataOutboundPortURIPrefix = "css-dop-" ;
	public static final String	ComputerDynamicStateDataInboundPortURIPrefix = "cds-dip-" ;
	public static final String	ComputerDynamicStateDataOutboundPortURIPrefix = "cds-dop" ;
	protected ComputerServicesOutboundPort[] csPorts;
	protected ComputerStaticStateDataOutboundPort[] cssPorts;
	protected ComputerDynamicStateDataOutboundPort[] cdsPorts;
	protected ComputerMonitor[] cms;

	protected int					defautFrequency ;
	protected int					maxFrequencyGap ;
	protected Set<Integer>			admissibleFrequencies ;
	protected Map<Integer,Integer>	processingPower ;
	protected Computer[]			computers ;
	
	// VMs
	public static final String 	VmURIPrefix = "vm-";
	public static final String	ApplicationVMManagementInboundPortURIPrefix = "avm-ibp" ;
	public static final String	ApplicationVMManagementOutboundPortURIPrefix = "avm-obp" ;
	public static final String	VmRequestSubmissionInboundPortURIPrefix = "vm-rsibp" ;
	public static final String	VmRequestNotificationOutboundPortURIPrefix = "vm-rnobp" ;
	protected List<ApplicationVMManagementOutboundPort>			avmPorts 
							= new ArrayList<ApplicationVMManagementOutboundPort>();
	
	protected List<ApplicationVM> vms = new ArrayList<ApplicationVM>();
	int nbVms = 0;
	
	// RequestDispatchers
	public static final String 	RdURIPrefix = "rd-";
	public static final String	RdManagementInboundPortURIPrefix = "rdm-ibp" ;
	public static final String	RdManagementOutboundPortURIPrefix = "rdm-obp" ;
	public static final String	RdRequestSubmissionInboundPortURIPrefix = "rd-rsibp" ;
	public static final String	RdRequestSubmissionOutboundPortURIPrefix = "rd-rsobp" ;
	public static final String	RdRequestNotificationInboundPortURIPrefix = "rd-rnibp" ;
	public static final String	RdRequestNotificationOutboundPortURIPrefix = "rd-rnobp" ;
	protected List<RequestDispatcherManagementOutboundPort>		rdmPorts 
						= new ArrayList<RequestDispatcherManagementOutboundPort>();	
	
	protected List<RequestDispatcher> rds = new ArrayList<RequestDispatcher>();
	int nbRds = 0;

	
	public				AdmissionController(
			String AdmissionControllerURI,
			String ServicesInboundPortURI
			) throws Exception
	{
		// The normal thread pool is used to process component services, while
		// the scheduled one is used to schedule the pushes of dynamic state
		// when requested.
		super(1, 1) ;

		// Preconditions
		assert AdmissionControllerURI != null ;
		assert ServicesInboundPortURI != null ;

		
		this.AdmissionControllerURI = AdmissionControllerURI ;

		// Interfaces and ports
		this.addOfferedInterface(AdmissionControllerServicesI.class) ;
		this.acsip = new AdmissionControllerServicesInboundPort(
										ServicesInboundPortURI, this) ;
		this.addPort(this.acsip) ;
		this.acsip.publishPort() ;
		
		// Computer parameters
		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500) ;	// Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000) ;	// and at 3 GHz
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000) ;	// 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000) ;
		defautFrequency = 1500;
		maxFrequencyGap = 1500;

		this.computers = new Computer[NUMBER_OF_COMPUTERS] ;
		this.csPorts = new ComputerServicesOutboundPort[NUMBER_OF_COMPUTERS] ;
		this.cssPorts = new ComputerStaticStateDataOutboundPort[NUMBER_OF_COMPUTERS] ;
		this.cdsPorts = new ComputerDynamicStateDataOutboundPort[NUMBER_OF_COMPUTERS] ;
		this.cms = new ComputerMonitor[NUMBER_OF_COMPUTERS] ;
		
		for(int c = 0 ; c < NUMBER_OF_COMPUTERS ; c++) {
			// ----------------------------------------------------------------
			// Create and deploy a computer component with its processors.
			// ----------------------------------------------------------------
			this.computers[c] = new Computer(
					ComputerURIPrefix + c,
					admissibleFrequencies,
					processingPower,  
					defautFrequency,
					maxFrequencyGap,		// max frequency gap within a processor
					NUMBER_OF_PROCESSORS_PER_COMPUTER,
					NUMBER_OF_CORES_PER_PROCESSOR,
					ComputerServicesInboundPortURIPrefix + c,
					ComputerStaticStateDataInboundPortURIPrefix + c,
					ComputerDynamicStateDataInboundPortURIPrefix + c) ;
			
			// Create a mock-up computer services port to later allocate its cores
			// to the application virtual machine.
			this.csPorts[c] = new ComputerServicesOutboundPort(
											ComputerServicesOutboundPortURIPrefix + c,
											new AbstractComponent() {}) ;
			this.csPorts[c].publishPort() ;
			this.csPorts[c].doConnection(
							ComputerServicesInboundPortURIPrefix + c,
							ComputerServicesConnector.class.getCanonicalName()) ;
			// --------------------------------------------------------------------

			// --------------------------------------------------------------------
			// Create the computer monitor component and connect its to ports
			// with the computer component.
			// --------------------------------------------------------------------
			this.cms[c] =
					new ComputerMonitor(ComputerURIPrefix + c,
										true,
										ComputerStaticStateDataOutboundPortURIPrefix + c,
										ComputerDynamicStateDataOutboundPortURIPrefix + c) ;
			
			this.cssPorts[c] =
					(ComputerStaticStateDataOutboundPort)
						this.cms[c].findPortFromURI(ComputerStaticStateDataOutboundPortURIPrefix + c) ;
			this.cssPorts[c].doConnection(
							ComputerStaticStateDataInboundPortURIPrefix + c,
							DataConnector.class.getCanonicalName()) ;

			this.cdsPorts[c] =
					(ComputerDynamicStateDataOutboundPort)
						this.cms[c].findPortFromURI(ComputerDynamicStateDataOutboundPortURIPrefix + c) ;
			this.cdsPorts[c].
					doConnection(
						ComputerDynamicStateDataInboundPortURIPrefix + c,
						ControlledDataConnector.class.getCanonicalName()) ;
		}
		
		
	}
	


	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			for(int c = 0 ; c < NUMBER_OF_COMPUTERS ; c++) {
				if (this.csPorts[c].connected()) {
					this.csPorts[c].doDisconnection() ;
				}	
				
				if (this.computers[c].isStarted())
					this.computers[c].shutdown();
				
				if (this.cms[c].isStarted())
					this.cms[c].shutdown();
			}
			
			for(int i = 0 ; i < this.vms.size() ; i++){
				if (this.avmPorts.get(i).connected()) {
					this.avmPorts.get(i).doDisconnection() ;
				}
				if (this.vms.get(i).isStarted())
					this.vms.get(i).shutdown();
			}
			
			for(int i = 0 ; i < this.rds.size() ; i++){
				if (this.rdmPorts.get(i).connected()) {
					this.rdmPorts.get(i).doDisconnection() ;
				}
				if (this.rds.get(i).isStarted())
					this.rds.get(i).shutdown();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		
		super.shutdown();
	}


	@Override
	public void submitApplication(
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		this.logMessage("Application submission");
		
		this.logMessage("Create Request Dispatcher "+RdURIPrefix + this.nbRds);
		createRequestDispatcherAndConnect(rgrsobp, RgRequestNotificationInboundPortURI);
		
		this.logMessage("Create VM "+VmURIPrefix + this.nbRds);
		createVmAndConnect(this.rdmPorts.get(this.nbRds-1));
		AllocatedCore[] acs = this.csPorts[0].allocateCores(2) ;
		this.vms.get(this.nbVms-1).allocateCores(acs);
		
		this.logMessage("Create VM "+VmURIPrefix + this.nbRds);
		createVmAndConnect(this.rdmPorts.get(this.nbRds-1));
		AllocatedCore[] acs_2 = this.csPorts[0].allocateCores(2) ;
		this.vms.get(this.nbVms-1).allocateCores(acs_2);
	}
	
	
	private void createVmAndConnect (RequestDispatcherManagementOutboundPort rdmop) throws Exception {
		// --------------------------------------------------------------------
		// Create an Application VM component
		// --------------------------------------------------------------------
		ApplicationVM vm = 
				new ApplicationVM(VmURIPrefix + this.nbVms,	// application vm component URI
						  ApplicationVMManagementInboundPortURIPrefix + this.nbVms,
						  VmRequestSubmissionInboundPortURIPrefix + this.nbVms,
						  VmRequestNotificationOutboundPortURIPrefix + this.nbVms);
		this.vms.add(vm) ;

		// Create a mock up port to manage the AVM component (allocate cores).
		ApplicationVMManagementOutboundPort avmPort = 
				new ApplicationVMManagementOutboundPort(
						ApplicationVMManagementOutboundPortURIPrefix + this.nbVms,
						new AbstractComponent() {});
		this.avmPorts.add(avmPort) ;
		avmPort.publishPort() ;
		avmPort.doConnection(
					ApplicationVMManagementInboundPortURIPrefix + this.nbVms,
					ApplicationVMManagementConnector.class.getCanonicalName()) ;

		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		vm.toggleTracing() ;
		vm.toggleLogging() ;
		
		rdmop.addRequestReceiver(
				VmRequestSubmissionInboundPortURIPrefix + this.nbVms, 
				(RequestNotificationOutboundPort)vm.
					findPortFromURI(VmRequestNotificationOutboundPortURIPrefix + this.nbVms));
		
		this.nbVms ++;
		// --------------------------------------------------------------------
	}
	
	private void createRequestDispatcherAndConnect (
							RequestSubmissionOutboundPort rgrsobp,
							String RgRequestNotificationInboundPortURI
							) throws Exception {
		// --------------------------------------------------------------------
		// Create an Request Dispatcher component
		// --------------------------------------------------------------------
		RequestDispatcher rd = 
				new RequestDispatcher(RdURIPrefix + this.nbRds, // application rd component URI,
					  RdManagementInboundPortURIPrefix + this.nbRds,
					  RdRequestSubmissionInboundPortURIPrefix + this.nbRds,
					  RdRequestNotificationOutboundPortURIPrefix + this.nbRds,
					  RdRequestNotificationInboundPortURIPrefix + this.nbRds);
		this.rds.add(rd);
		
		RequestDispatcherManagementOutboundPort rdmPort =
				new RequestDispatcherManagementOutboundPort(
						RdManagementOutboundPortURIPrefix + this.nbRds,
						new AbstractComponent() {});
		this.rdmPorts.add(rdmPort) ;
		rdmPort.publishPort() ;
		rdmPort.doConnection(
					RdManagementInboundPortURIPrefix + this.nbRds,
					RequestDispatcherManagementConnector.class.getCanonicalName()) ;
		
		// Connect request dispatcher with request generator
		rgrsobp.doConnection(
				RdRequestSubmissionInboundPortURIPrefix + this.nbRds,
				RequestSubmissionConnector.class.getCanonicalName()) ;

		RequestNotificationOutboundPort rdnobp =
				(RequestNotificationOutboundPort) rd.findPortFromURI(
						RdRequestNotificationOutboundPortURIPrefix + this.nbRds) ;
		rdnobp.doConnection(
				RgRequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName()) ;
		
		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		rd.toggleTracing() ;
		rd.toggleLogging() ;
		
		this.nbRds ++;
		// --------------------------------------------------------------------
	}
	
}	
	
