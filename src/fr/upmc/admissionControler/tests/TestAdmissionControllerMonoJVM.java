package fr.upmc.admissionControler.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.upmc.admissionControler.AdmissionController;
import fr.upmc.admissionControler.connectors.AdmissionControllerServicesConnector;
import fr.upmc.admissionControler.ports.AdmissionControllerServicesOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;


public class TestAdmissionControllerMonoJVM
extends		AbstractCVM
{
	
	// Computers
	public static final int		NUMBER_OF_COMPUTERS = 2 ;
	public static final int		NUMBER_OF_PROCESSORS_PER_COMPUTER = 2 ;
	public static final int		NUMBER_OF_CORES_PER_PROCESSOR = 2 ;

	public static final String 	ComputerURIPrefix = "computer-";
	public static final String	ComputerServicesInboundPortURIPrefix = "cs-ibp-" ;
	public static final String	ComputerStaticStateDataInboundPortURIPrefix = "css-dip-" ;
	public static final String	ComputerStaticStateDataOutboundPortURIPrefix = "css-dop-" ;
	public static final String	ComputerDynamicStateDataInboundPortURIPrefix = "cds-dip-" ;
	public static final String	ComputerDynamicStateDataOutboundPortURIPrefix = "cds-dop" ;

	protected int							defautFrequency ;
	protected int							maxFrequencyGap ;
	protected Set<Integer>					admissibleFrequencies ;
	protected Map<Integer,Integer>			processingPower ;
	protected Computer[]					computers ;
		
		
	public static final int     NUMBER_OF_APPLICATIONS = 3;
	
	// Request Generators
	public static final String 	RgURIPrefix = "rg-";
	public static final String	RgManagementInboundPortURIPrefix = "rgm-ibp" ;
	public static final String	RgManagementOutboundPortURIPrefix = "rgm-obp" ;
	public static final String	RgRequestSubmissionOutboundPortURIPrefix = "rg-rsobp" ;
	public static final String	RgRequestNotificationInboundPortURIPrefix = "rg-rnibp" ;
	protected List<RequestGeneratorManagementOutboundPort>		rgmPorts 
						= new ArrayList<RequestGeneratorManagementOutboundPort>();	
	
	protected List<RequestGenerator> rgs = new ArrayList<RequestGenerator>();
	int nbRgs = 0;
	
	// Admission controller
	public static final String	ACServicesInboundPortURI = "acsip" ;
	public static final String	ACServicesOutboundPortURI = "acsop" ;
	
	/** Port connected to the admission controller component to 
	 * do admission request.									*/
	protected AdmissionControllerServicesOutboundPort	acsop ;
	
	
	public				TestAdmissionControllerMonoJVM()
	throws Exception
	{
		super();
	}
	
	@Override
	public void			deploy() throws Exception
	{
		AbstractComponent.configureLogging("", "", 0, '|') ;
		Processor.DEBUG = true ;
		
		// --------------------------------------------------------------------
		// Create an admission controller component
		// --------------------------------------------------------------------
		AdmissionController ac = 
				new AdmissionController("ac",	// admission controller component URI,
									  ACServicesInboundPortURI);
		this.addDeployedComponent(ac) ;
		
		this.acsop = new AdmissionControllerServicesOutboundPort(
										ACServicesOutboundPortURI,
										new AbstractComponent() {}) ;
		this.acsop.publishPort() ;
		this.acsop.
				doConnection(
					ACServicesInboundPortURI,
					AdmissionControllerServicesConnector.class.getCanonicalName()) ;
		
		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		ac.toggleTracing() ;
		ac.toggleLogging() ;
		// --------------------------------------------------------------------
		
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
			
			this.addDeployedComponent(computers[c]) ;
			
			ac.connectComputer( ComputerURIPrefix + c,
								ComputerServicesInboundPortURIPrefix + c, 
								ComputerStaticStateDataInboundPortURIPrefix + c, 
								ComputerDynamicStateDataInboundPortURIPrefix + c);
		}
		
		// complete the deployment at the component virtual machine level.
		super.deploy();
	}
	
	
	/**
	 * @see fr.upmc.components.cvm.AbstractCVM#start()
	 */
	@Override
	public void			start() throws Exception
	{
		super.start() ;
	}
	

	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws Exception
	{
		this.acsop.doDisconnection();
		
		for(int i = 0 ; i < this.rgs.size() ; i++){
			if (this.rgmPorts.get(i).connected()) {
				this.rgmPorts.get(i).doDisconnection() ;
			}
		}
		
		super.shutdown();
	}
	
	private void createRequestGenerator() throws Exception {
		// --------------------------------------------------------------------
		// Creating the request generator component.
		// --------------------------------------------------------------------
		RequestGenerator rg =
			new RequestGenerator(
					RgURIPrefix + this.nbRgs,			// generator component URI
					500.0,			// mean time between two requests
					6000000000L,	// mean number of instructions in requests
					RgManagementInboundPortURIPrefix + this.nbRgs,
					RgRequestSubmissionOutboundPortURIPrefix + this.nbRgs,
					RgRequestNotificationInboundPortURIPrefix + this.nbRgs) ;
		this.rgs.add(rg);
		this.addDeployedComponent(rg) ;
		
		// Create a mock up port to manage to request generator component
		// (starting and stopping the generation).
		RequestGeneratorManagementOutboundPort rgmPort = 
				new RequestGeneratorManagementOutboundPort(
						RgManagementInboundPortURIPrefix + this.nbRgs,
						new AbstractComponent() {}) ;
		this.rgmPorts.add(rgmPort) ;
		rgmPort.publishPort() ;
		rgmPort.doConnection(
				RgManagementInboundPortURIPrefix + this.nbRgs,
				RequestGeneratorManagementConnector.class.getCanonicalName()) ;

		// Toggle on tracing and logging in the request generator to
		// follow the submission and end of execution notification of
		// individual requests.
		rg.toggleTracing() ;
		rg.toggleLogging() ;
		
		this.nbRgs++;
	}
	
	/**
	 * generate requests for 20 seconds and then stop generating.
	 *
	 * @throws Exception
	 */
	public void			testScenario() throws Exception
	{
		Thread.sleep(1000L) ;
		for (int i = 0 ; i < NUMBER_OF_APPLICATIONS ; i++) {
			createRequestGenerator();
			
			this.acsop.submitApplication(
				(RequestSubmissionOutboundPort) this.rgs.get(i).
					findPortFromURI(RgRequestSubmissionOutboundPortURIPrefix + i),
				RgRequestNotificationInboundPortURIPrefix + i);
			
			Thread.sleep(1000L) ;
			// If rgs is connected with request dispatcher so admission is OK
			if(this.rgs.get(i).
				findPortFromURI(RgRequestSubmissionOutboundPortURIPrefix + i).connected()){
				// start the request generation in the request generator.
				Thread.sleep(2000L) ;
				this.rgs.get(i).startGeneration() ;
			}
			
			Thread.sleep(10000L) ;
		}
		
		for (int i = 0 ; i < NUMBER_OF_APPLICATIONS ; i++){
			this.rgs.get(i).stopGeneration();
		}
	}

	/**
	 * execute the test application.
	 * 
	 * @param args	command line arguments, disregarded here.
	 */
	public static void	main(String[] args)
	{
		// Uncomment next line to execute components in debug mode.
		// AbstractCVM.toggleDebugMode() ;
		try {
			final TestAdmissionControllerMonoJVM trcm = new TestAdmissionControllerMonoJVM() ;
			// Deploy the components
			trcm.deploy() ;
			System.out.println("starting...") ;
			// Start them.
			trcm.start() ;
			// Execute the chosen request generation test scenario in a
			// separate thread.
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						trcm.testScenario() ;
					} catch (Exception e) {
						throw new RuntimeException(e) ;
					}
				}
			}).start() ;
			// Sleep to let the test scenario execute to completion.
			Thread.sleep(90000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
			trcm.shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
