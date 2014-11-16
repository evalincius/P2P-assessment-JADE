/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package jade;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

/**
 * This agent implements a simple Ping Agent that registers itself with the DF and 
 * then waits for ACLMessages.
 * If  a REQUEST message is received containing the string "ping" within the content 
 * then it replies with an INFORM message whose content will be the string "pong". 
 * 
 * @author Tiziana Trucco - CSELT S.p.A.
 * @version  $Date: 2010-04-08 13:08:55 +0200 (gio, 08 apr 2010) $ $Revision: 6297 $  
 */
public class HostCache extends Agent {

	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	//private ArrayList SuperPeerList = new ArrayList();
	//private ArrayList NormalPeerList = new ArrayList();
    Map SuperPeerList = new HashMap();
    Map NormalPeerList = new HashMap();
	private int ID = 0;

    private Random randomGenerator;
	private class WaitPingAndReplyBehaviour extends CyclicBehaviour {
		public WaitPingAndReplyBehaviour(Agent a) {
			super(a);
		}
		public void action() {
			ACLMessage  msg = myAgent.receive();
			if(msg != null){
				ACLMessage reply = msg.createReply();

				if(msg.getPerformative()== ACLMessage.REQUEST){
					String content = msg.getContent();
					StringTokenizer st = new StringTokenizer(content);
					ArrayList listContent = new ArrayList();
					while (st.hasMoreElements()) {
						listContent.add(st.nextElement().toString().toLowerCase());
					}
					if ((listContent.get(0) != null) && (((String) listContent.get(0)).indexOf("register") != -1)){
						myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received REGISTER Request from "+msg.getSender().getLocalName());
						double Bandwidth = Double.parseDouble((String) listContent.get(1));
						
						// randomly selects Supernodes
						Random random = new Random();
						List<Integer> keys = new ArrayList<Integer>(SuperPeerList.keySet());
						List<String> RandSPeersList = new ArrayList<String>();
						int i = 3;
						while(keys.size()!=0 && i>0){
							 int randInt = random.nextInt(keys.size());
							 String RandSP = (String) SuperPeerList.get(keys.get(randInt));
							 //if returning SPeer, checks if its name is rot in random list
							 if(!msg.getSender().getLocalName().equals(RandSP)){
								 RandSPeersList.add(RandSP);
								 keys.remove(randInt);
								 i--;
							 }
							
						}
						String SPeerList = "";
						for(int j=0; j<RandSPeersList.size(); j++){
							SPeerList = SPeerList + " " + RandSPeersList.get(j); 
						} 
						//Checks returning Peers
						if(SuperPeerList.containsValue(msg.getSender().getLocalName())){
							List<Integer> keylist = new ArrayList<Integer>(SuperPeerList.keySet());
							for(int a=0; a<keylist.size(); a++){
								if(SuperPeerList.get(keylist.get(a)).equals(msg.getSender().getLocalName())){
									ID = keylist.get(a);
								}
							}
							
						}
						if(NormalPeerList.containsValue(msg.getSender().getLocalName())){
							List<Integer> keylist = new ArrayList<Integer>(NormalPeerList.keySet());
							for(int a=0; a<NormalPeerList.keySet().size(); a++){
								if(NormalPeerList.get(keylist.get(a)).equals(msg.getSender().getLocalName())){
									ID = keylist.get(a);
								}
							}
							
						}
						else{
							//increments ID
							ID++;
							//if new peer, checks BW and assigns new ID
							if(Bandwidth>=2){
								SuperPeerList.put( ID, msg.getSender().getLocalName());
							}else{
								NormalPeerList.put( ID, msg.getSender().getLocalName());
							} 
						}
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent("comfirm"+ " "+ ID +" " + SPeerList);
					}
					else{
						myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected request ["+content+"] received from "+msg.getSender().getLocalName());
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("( UnexpectedContent ("+content+"))"+"name->"+msg.getSender().getLocalName());
					}

				}
				else {
					myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected message ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
					reply.setContent("( (Unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+") )"+"name->"+msg.getSender().getLocalName());   
				}
				send(reply);
			}
			else {
				block();
			}
		}//end of action
	
	} // END of inner class WaitPingAndReplyBehaviour


	protected void setup() {
		// Registration with the DF 
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();   
		sd.setType("PingAgent"); 
		sd.setName(getName());
		sd.setOwnership("TILAB");
		dfd.setName(getAID());
		dfd.addServices(sd);
		try {
			DFService.register(this,dfd);
			WaitPingAndReplyBehaviour PingBehaviour = new  WaitPingAndReplyBehaviour(this);
			addBehaviour(PingBehaviour);
		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
			doDelete();
		}
	}
}
