package jade;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import jade.core.*; 
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

public class Simulation extends Agent {
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	
	// get a JADE runtime
	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = -3200493830320510548L;{
	try {
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		//Creates new container
		jade.wrapper.AgentContainer home = runtime.createAgentContainer(new ProfileImpl());
		// Adds new agent to container
		AgentController a = home.createNewAgent("HC",HostCache.class.getName(), new Object[0]);
		a.start();
		//home.createNewAgent("2", "jade.HostCache", null);
		} catch (jade.wrapper.StaleProxyException e) {
		System.err.println("Error launching agent...");
		}
	}
	
	
	
}

