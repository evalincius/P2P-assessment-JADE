package jade;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

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

/**
 * This agent is for simulation. It reads the txt file to get the names and 
 * details of peers and hostcaches. then it creates new container and launches 
 * those Peers and hostcaches.
 * 
 * @author Edgaras Valincius
 * @references Tiziana Trucco - CSELT S.p.A.(used the ping message example)  
 */
public class Simulation extends Agent {
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	
	// get a JADE runtime

	
	private static final long serialVersionUID = -3200493830320510548L;{
	try {
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		//Creates new container
		jade.wrapper.AgentContainer home = runtime.createAgentContainer(new ProfileImpl());
		//initialize buffered Reader
		BufferedReader reader = null;
		try {
			File file = new File("/Users/ed/Peers.txt"); 
			reader = new BufferedReader(new FileReader(file));
			ArrayList<String> ListOfHC = new ArrayList<String>();
		    String line;
		    while ((line = reader.readLine()) != null) {
		    	String Type = "peer",Name = "peer",BW = "peer";
				ArrayList<String> Peer = new ArrayList<String>();
		    	String str = line;
				StringTokenizer st = new StringTokenizer(str);
				while (st.hasMoreElements()) {
					Peer.add(st.nextElement().toString().toLowerCase());
				}
					//gets peers type name and BW from the list
					Type = Peer.get(0);
					Name = Peer.get(1);
					BW = Peer.get(2);
					if(Type.equals("hc")){
						//creates HostCache with unique given name
						AgentController a = home.createNewAgent(Name,HostCache.class.getName(), new Object[0]);
						a.start();
						ListOfHC.add(Name);
					}else{
					if(Type.equals("peer")){
						if(Double.parseDouble(BW)>=2){
							//creates SuperPeer with unique given name
							//sends list of HostCaches as an arguments
						    Object[] args = new Object[10];
							args[0]= BW;
						    for(int i=0; i<ListOfHC.size(); i++){
								args[i+1]= ListOfHC.get(i);
						    }
							AgentController b = home.createNewAgent(Name,SuperPeer.class.getName(), args);
							b.start();
						}
						if(Double.parseDouble(BW)<2){
							//creates NormalPeer with unique given name
							//sends list of HostCaches as an arguments
						    Object[] args = new Object[10];
							args[0]= BW;
						    for(int i=0; i<ListOfHC.size(); i++){
								args[i+1]= ListOfHC.get(i);
						    }
							AgentController b = home.createNewAgent(Name,NormalPeer.class.getName(), args);
							b.start();
						}
					}
				}
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        reader.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		
		} catch (jade.wrapper.StaleProxyException e) {
		System.err.println("Error launching agent...");
		}
	}
	
	
	
}

