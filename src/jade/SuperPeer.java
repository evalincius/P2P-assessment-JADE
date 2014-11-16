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
import java.util.Random;
import java.util.StringTokenizer;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

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
public class SuperPeer extends Agent {

	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private ArrayList SuperPeerList = new ArrayList();
	private ArrayList<String> Neighbours = new ArrayList();

	private String ID;
	Random r = new Random();
	private class WaitPingAndReplyBehaviour extends CyclicBehaviour {
	private boolean registered = false;
	private boolean msgReceived = false;
	private boolean MSGsent = false;

		public WaitPingAndReplyBehaviour(Agent a) {
			super(a);
			//Gives delay for test purposes, time to open sniffer 
			try {
				//20s = 20000
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		public void action() {
			ACLMessage  msg = myAgent.receive();
			registerWithHc(msg);
			if(registered){
			connectWithServent(msg);
			connectWithOtherSuperPeers();
			getSuperPeersResponce(msg);
			}
			
			
		}
		
		public void registerWithHc(	ACLMessage  msg2){
			// gets argumets for test purposes
			ArrayList<String> ListOfHC = new ArrayList<String>();
			Object[] args = getArguments();
			//gets bandwidth's value 
			String BW = args[0].toString();
			double Bandwidth = Double.parseDouble(BW);
			//gets all host caches that are available
			try{
				for(int i =1; i<args.length; i++ ){
					ListOfHC.add(args[i].toString());
					//System.out.println(Bandwidth);
				}
			}catch (NullPointerException e) {
			}
			//randomly selects one host cache
		    int rand = r.nextInt(ListOfHC.size());
		    String randomHC = ListOfHC.get(rand);
		    //creates register message and sends it to randomly selected host cache 
			ACLMessage RegMsg = new ACLMessage(ACLMessage.REQUEST);
			//attaches BW to the message 
			RegMsg.setContent("register"+ " " + BW);
			AID recei = new AID(randomHC, AID.ISLOCALNAME);
			RegMsg.addReceiver(recei);
			if(!registered){
				myAgent.send(RegMsg);
				registered = true;
			}
			
			ACLMessage  msg = msg2;
			if(msg != null){
				//ACLMessage reply = msg.createReply();
				if(msg.getPerformative() == ACLMessage.INFORM){
					String content = msg.getContent();
					//splits received message to tokens to get message and ID strings
					StringTokenizer st = new StringTokenizer(content);
					ArrayList listContent = new ArrayList();
					while (st.hasMoreElements()) {
						listContent.add(st.nextElement().toString().toLowerCase());
					}
					if ((content != null) && (((String) listContent.get(0)).indexOf("comfirm") != -1)){
						myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received COMFIRMATION from "+msg.getSender().getLocalName());
						ID = (String) listContent.get(1);
						for(int i=2; i<listContent.size(); i++){
							SuperPeerList.add((String) listContent.get(i));
						}
						System.out.println("SPLISTAS SuperPeer!!! "+ SuperPeerList);
						if(SuperPeerList.size()==0){
							registered = false;
						}					
					
					
					}}}else {
				block();
			}
		}//end registerWithHC
		
		public void connectWithServent(ACLMessage  msg2){
			ACLMessage  msg = msg2;
			if(msg != null){
				ACLMessage reply = msg.createReply();

				if(msg.getPerformative()== ACLMessage.REQUEST){
					//System.out.println("Got ping request");
					String content = msg.getContent();
					StringTokenizer st = new StringTokenizer(content);
					ArrayList listContent = new ArrayList();
					while (st.hasMoreElements()) {
						listContent.add(st.nextElement().toString().toLowerCase());
					}
					if ((listContent.get(0) != null) && (((String) listContent.get(0)).indexOf("ping") != -1)){
						//System.out.println("gotPing");
						myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received PING Request from "+msg.getSender().getLocalName());
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent("pong");
						send(reply);
					}
				}}else {
				block();
			}

		}//End of connectWithServent
		public void connectWithOtherSuperPeers(){
			//if did not send yet do
			if(!MSGsent){
				for(int i=0;i<SuperPeerList.size(); i++){
					MSGsent =true;
					ACLMessage PingMSG = new ACLMessage(ACLMessage.REQUEST);
					PingMSG.setContent("ping");
					String SP = (String) SuperPeerList.get(i);
					AID SuperPeer = new AID(SP, AID.ISLOCALNAME);
					PingMSG.addReceiver(SuperPeer);
					if(Neighbours.size()<5){
						myAgent.send(PingMSG);
					}
					
				}
			}
		}//end of connectWithSuperPeer
		public void getSuperPeersResponce(ACLMessage  msg2){
			ArrayList<String> ListOfHC = new ArrayList<String>();			
			ACLMessage  msg = msg2;
			if(msg != null){
				//ACLMessage reply = msg.createReply();
				if(msg.getPerformative()== ACLMessage.INFORM){
					String content = msg.getContent();
					//splits received message to tokens to get message and ID strings
					StringTokenizer st = new StringTokenizer(content);
					ArrayList listContent = new ArrayList();
					while (st.hasMoreElements()) {
						listContent.add(st.nextElement().toString().toLowerCase());
					}
					if ((content != null) && (((String) listContent.get(0)).indexOf("pong") != -1)){
						myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received PONG from "+msg.getSender().getLocalName());
						if(Neighbours.size()<5){
							Neighbours.add(msg.getSender().getLocalName());
							System.out.println(getLocalName()+" NEIGHBOUR WITH "+msg.getSender().getLocalName());
						}

					}
				}}
			else {
				block();
			}
		}//end of getSuperPeersResponce
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
