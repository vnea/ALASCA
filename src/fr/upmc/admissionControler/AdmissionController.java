package fr.upmc.admissionControler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI;
import fr.upmc.admissionControler.ports.AdmissionControllerServicesInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.requestdispatcher.RequestDispatcher;
import fr.upmc.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;

public class AdmissionController
extends		AbstractComponent
implements	AdmissionControllerServicesI,
			ComputerStateDataConsumerI
{

	// ------------------------------------------------------------------------
	// Component internal state
	// ------------------------------------------------------------------------
	/** URI of this dispatcher.											*/
	protected String				AdmissionControllerURI ;
	
	// ports
	protected AdmissionControllerServicesInboundPort acsip ;
	
	// Computers
	public static final String	ComputerServicesOutboundPortURIPrefix = "cs-obp" ;
	public static final String	ComputerStaticStateDataOutboundPortURIPrefix = "css-dop-" ;
	public static final String	ComputerDynamicStateDataOutboundPortURIPrefix = "cds-dop" ;
	protected Map<String,ComputerServicesOutboundPort> csPorts;
	protected Map<String,ComputerStaticStateDataOutboundPort> cssPorts;
	protected Map<String,ComputerDynamicStateDataOutboundPort> cdsPorts;
	int nbComputers = 0;
	protected Map<String,boolean[][]> 		reservedCores;
	
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
		

		this.csPorts = new HashMap<String,ComputerServicesOutboundPort>() ;
		this.cssPorts = new HashMap<String,ComputerStaticStateDataOutboundPort>() ;
		this.cdsPorts = new HashMap<String,ComputerDynamicStateDataOutboundPort>() ;
		this.reservedCores = new HashMap<String, boolean[][]>() ;
			
	}


	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			for (Entry<String, ComputerServicesOutboundPort> entry :  csPorts.entrySet()) {
				ComputerServicesOutboundPort csop = entry.getValue();
				if (csop.connected()) {
					csop.doDisconnection() ;
				}				
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


	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public boolean submitApplication(
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		this.logMessage("Application submission");
		
		// Instantiate application on 2 VMs if possible 
		// else 1 VM
		// else refuse submission
		// VM is create with 2 cores
		List<String> computersOk = new ArrayList<String>();
		Iterator<Entry<String, boolean[][]>> entries = this.reservedCores.entrySet().iterator();
		while (entries.hasNext()  &&  computersOk.size() < 2){
			Entry<String, boolean[][]> thisEntry = entries.next();
			boolean[][] cores = (boolean[][]) thisEntry.getValue(); 
			int nbCoresFree = 0;
			for (int p = 0 ; p < cores.length ; p++) {
				for (int c = 0 ; c < cores[p].length ; c++) {
					if (!cores[p][c]) {
						nbCoresFree++;
					} 
				}
			}
			while (nbCoresFree >= 2  &&  computersOk.size() < 2){
				nbCoresFree = nbCoresFree - 2;
				computersOk.add(thisEntry.getKey());
			}
		}
		
		if(computersOk.size() > 0){
			this.logMessage("Create Request Dispatcher "+RdURIPrefix + this.nbRds);
			createRequestDispatcherAndConnect(rgrsobp, RgRequestNotificationInboundPortURI);
			for (int i = 0 ; i < computersOk.size() ; i++){
				this.logMessage("Create VM "+ VmURIPrefix + this.nbVms);
				createVmAndConnect(this.rdmPorts.get(this.nbRds-1));
				AllocatedCore[] acs = this.csPorts.get(computersOk.get(i)).allocateCores(2) ;
				this.vms.get(this.nbVms-1).allocateCores(acs);
			}
		}
		else{
			this.logMessage("Submission refused");
			return false;
		}
		
		return true;
	}
	
	
	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(java.lang.Class, java.util.Map, fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public boolean submitApplication(
			Class<?> offeredInterface,
			Map<String,String> methodNamesMap,
			RequestSubmissionOutboundPort rgrsobp,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		this.logMessage("Application submission");
		
		// Instantiate application on 2 VMs if possible 
		// else 1 VM
		// else refuse submission
		// VM is create with 2 cores
		List<String> computersOk = new ArrayList<String>();
		Iterator<Entry<String, boolean[][]>> entries = this.reservedCores.entrySet().iterator();
		while (entries.hasNext()  &&  computersOk.size() < 2){
			Entry<String, boolean[][]> thisEntry = entries.next();
			boolean[][] cores = (boolean[][]) thisEntry.getValue(); 
			int nbCoresFree = 0;
			for (int p = 0 ; p < cores.length ; p++) {
				for (int c = 0 ; c < cores[p].length ; c++) {
					if (!cores[p][c]) {
						nbCoresFree++;
					} 
				}
			}
			while (nbCoresFree >= 2  &&  computersOk.size() < 2){
				nbCoresFree = nbCoresFree - 2;
				computersOk.add(thisEntry.getKey());
			}
		}
		
		if(computersOk.size() > 0){
			this.logMessage("Create Request Dispatcher "+RdURIPrefix + this.nbRds);
			createRequestDispatcherAndConnect(rgrsobp, RgRequestNotificationInboundPortURI);
			
			Class<?> connectorClass = 
					this.makeConnectorClassJavassist(
							"fr.upmc.admissionControler.admissionController.GenerateConnector" + this.nbRds, 
							AbstractConnector.class, 
							RequestSubmissionI.class, 
							RequestSubmissionI.class, 
							(HashMap<String, String>) methodNamesMap);
			
			for (int i = 0 ; i < computersOk.size() ; i++){
				this.logMessage("Create VM "+ VmURIPrefix + this.nbVms);
				createVmAndConnect(this.rdmPorts.get(this.nbRds-1), connectorClass);
				AllocatedCore[] acs = this.csPorts.get(computersOk.get(i)).allocateCores(2) ;
				this.vms.get(this.nbVms-1).allocateCores(acs);
			}
		}
		else{
			this.logMessage("Submission refused");
			return false;
		}
		
		return true;
	}
	
	
	
	/*
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#connectComputer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void connectComputer (String ComputerURI,
								String ComputerServicesInboundPortURI,
								String ComputerStaticStateDataInboundPortURI,
								String ComputerDynamicStateDataInboundPortURI) throws Exception{
		
		this.csPorts.put(ComputerURI, 
				new ComputerServicesOutboundPort(
						ComputerServicesOutboundPortURIPrefix + this.nbComputers,
						new AbstractComponent() {}
				)) ;
		this.addPort(this.csPorts.get(ComputerURI));
		this.csPorts.get(ComputerURI).publishPort() ;
		this.csPorts.get(ComputerURI).doConnection(
						ComputerServicesInboundPortURI,
						ComputerServicesConnector.class.getCanonicalName()) ;
		
		this.cssPorts.put(ComputerURI, 
					new ComputerStaticStateDataOutboundPort(
						ComputerStaticStateDataOutboundPortURIPrefix + this.nbComputers,
						this,
						ComputerURI
					)) ;
		this.addPort(this.cssPorts.get(ComputerURI)) ;
		this.cssPorts.get(ComputerURI).publishPort() ;
		this.cssPorts.get(ComputerURI).doConnection(
				ComputerStaticStateDataInboundPortURI,
				DataConnector.class.getCanonicalName()) ;
		
		this.cdsPorts.put(ComputerURI, 
				new ComputerDynamicStateDataOutboundPort(
					ComputerDynamicStateDataOutboundPortURIPrefix + this.nbComputers,
					this,
					ComputerURI
				)) ;
		this.addPort(this.cdsPorts.get(ComputerURI)) ;
		this.cdsPorts.get(ComputerURI).publishPort() ;
		this.cdsPorts.get(ComputerURI).
		doConnection(
			ComputerDynamicStateDataInboundPortURI,
			ControlledDataConnector.class.getCanonicalName()) ;
		
		this.cdsPorts.get(ComputerURI).startUnlimitedPushing(500);
	
		this.nbComputers++;
	}
	
	
	/**
	 * Create a VM and connect her to the dispatcher.
	 * 
	 * @param rdmop
	 * @throws Exception
	 */
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
		this.addPort(avmPort);
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
	
	
	/**
	 * Create a VM and connect her to the dispatcher.
	 * 
	 * @param rdmop
	 * @param connectorClass
	 * @throws Exception
	 */
	private void createVmAndConnect (
				RequestDispatcherManagementOutboundPort rdmop,
				Class<?> connectorClass
				) throws Exception {
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
		this.addPort(avmPort);
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
					findPortFromURI(VmRequestNotificationOutboundPortURIPrefix + this.nbVms),
				connectorClass);
		
		this.nbVms ++;
		// --------------------------------------------------------------------
	}
	
	/**
	 * Create a Dispatcher and connect him with a request generator
	 * 
	 * @param rgrsobp
	 * @param RgRequestNotificationInboundPortURI
	 * @throws Exception
	 */
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
		this.addPort(rdmPort);
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
	
	
	/**
	 * 
	 * 
	 * @param connectorCanonicalClassName
	 * @param connectorSuperclass
	 * @param connectorImplementedInterface
	 * @param offeredInterface
	 * @param methodNamesMap
	 * @return
	 * @throws Exception
	 */
	public Class<?> makeConnectorClassJavassist(String connectorCanonicalClassName, Class<?> connectorSuperclass,
			Class<?> connectorImplementedInterface, Class<?> offeredInterface, HashMap<String,String> methodNamesMap
			) throws Exception
	{
		ClassPool pool = ClassPool.getDefault() ;
		CtClass cs = pool.get(connectorSuperclass.getCanonicalName()) ;
		CtClass cii = pool.get(connectorImplementedInterface.getCanonicalName()) ;
//		CtClass oi = pool.get(offeredInterface.getCanonicalName()) ;
		CtClass connectorCtClass = pool.makeClass(connectorCanonicalClassName) ; 
		connectorCtClass.setSuperclass(cs) ;
		Method[] methodsToImplement = connectorImplementedInterface.getDeclaredMethods() ; 
		for (int i = 0 ; i < methodsToImplement.length ; i++) {
			String source = "public " ;
			source += methodsToImplement[i].getReturnType().getName() + " " ; 
			source += methodsToImplement[i].getName() + "(" ;
			Class<?>[] pt = methodsToImplement[i].getParameterTypes() ; 
			String callParam = "" ;
			for (int j = 0 ; j < pt.length ; j++) {
				String pName = "aaa" + j ;
				source += pt[j].getCanonicalName() + " " + pName ; 
				callParam += pName ;
				if (j < pt.length - 1) {
					source += ", " ;
					callParam += ", " ; 
				}
			}
			source += ")" ;
			Class<?>[] et = methodsToImplement[i].getExceptionTypes() ; 
			if (et != null && et.length > 0) {
				source += " throws " ;
				for (int z = 0 ; z < et.length ; z++) { 
					source += et[z].getCanonicalName() ; 
					if (z < et.length - 1) {
						source += "," ; 
					}
				} 
			}
			source += "\n{ return ((" ;
			source += offeredInterface.getCanonicalName() + ")this.offering)." ; 
			source += methodNamesMap.get(methodsToImplement[i].getName()) ; 
			source += "(" + callParam + ") ;\n}" ;
			CtMethod theCtMethod = CtMethod.make(source, connectorCtClass) ; 
			connectorCtClass.addMethod(theCtMethod) ;
		}
		connectorCtClass.setInterfaces(new CtClass[]{cii}) ; 
		cii.detach() ; 
		cs.detach() ; 
		Class<?> ret = connectorCtClass.toClass() ; 
		connectorCtClass.detach() ;
		return ret ;
	}



	/* 
	 * @see fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI#acceptComputerStaticData(java.lang.String, fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI)
	 */
	@Override
	public void acceptComputerStaticData(String computerURI,
			ComputerStaticStateI staticState) throws Exception {
		// TODO Auto-generated method stub
		
	}



	/* 
	 * @see fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI#acceptComputerDynamicData(java.lang.String, fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI)
	 */
	@Override
	public void acceptComputerDynamicData(String computerURI,
		ComputerDynamicStateI cds) throws Exception {

		this.reservedCores.put(cds.getComputerURI(), cds.getCurrentCoreReservations());
	}	
}	
	
