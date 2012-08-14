package com.ifsoft.cti;

import java.io.*;
import java.net.*;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.jivesoftware.openfire.*;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.http.HttpBindManager;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.PrivateStorage;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.vcard.*;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.roster.RosterManager;
import org.jivesoftware.openfire.group.Group;

import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.util.JiveGlobals;

import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.muc.spi.*;

import org.xmpp.component.Component;
import org.xmpp.component.AbstractComponent;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Packet;

import org.apache.log4j.Logger;

import org.red5.server.webapp.voicebridge.*;

import com.sun.voip.server.*;
import com.sun.voip.client.*;
import com.sun.voip.*;


public class OpenlinkComponent extends AbstractComponent implements CallEventListener
{
    private static final String JINGLE_NAMESPACE = "urn:xmpp:jingle:1";
    private static final String RAW_UDP_NAMESPACE = "urn:xmpp:jingle:transports:raw-udp:1";

    private static final String RTP_AUDIO = "urn:xmpp:jingle:apps:rtp:audio";

	private ComponentManager componentManager;
	private JID componentJID = null;
	public Plugin plugin;

	private PrivateStorage privateStorage;
	private UserManager userManager;
	private RosterManager rosterManager;
	private SessionManager sessionManager;
	private OpenlinkCommandManager openlinkManger;
    private PresenceManager presenceManager;

	private Application application;

	public Map<String, OpenlinkUser> openlinkUserTable;
	public Map<String, OpenlinkUserInterest> openlinkUserInterests;
	public Map<String, OpenlinkInterest> openlinkInterests;
	public Map<String, OpenlinkInterest> callInterests;
	public Map<String, OpenlinkUser> userProfiles;

	private Map<String, CallParticipant> callParticipants;
	private Map<String, String> callStreams;

    protected Logger Log = Logger.getLogger(getClass().getName());

//-------------------------------------------------------
//
//	Init & Terminate
//
//-------------------------------------------------------

	public OpenlinkComponent(Plugin plugin)
	{
        super(16, 1000, true);

		Log.info("OpenlinkComponent");

        this.plugin = plugin;
        this.componentJID = new JID(getName() + "." + getDomain());
	}

	public void componentEnable()
	{
		try {
			privateStorage 		= XMPPServer.getInstance().getPrivateStorage();
			userManager 		= XMPPServer.getInstance().getUserManager();
			rosterManager		= XMPPServer.getInstance().getRosterManager();
			sessionManager		= XMPPServer.getInstance().getSessionManager();
			presenceManager 	= XMPPServer.getInstance().getPresenceManager();

			componentManager = ComponentManagerFactory.getComponentManager();
			componentManager.addComponent(getName(), this);

			openlinkUserInterests 	= Collections.synchronizedMap( new HashMap<String, OpenlinkUserInterest>());
			openlinkUserTable 		= Collections.synchronizedMap( new HashMap<String, OpenlinkUser>());
			openlinkInterests 		= Collections.synchronizedMap( new HashMap<String, OpenlinkInterest>());
			callInterests			= Collections.synchronizedMap( new HashMap<String, OpenlinkInterest>());
			userProfiles 			= Collections.synchronizedMap( new HashMap<String, OpenlinkUser>());

			callParticipants		= new HashMap<String, CallParticipant>();
			callStreams				= new HashMap<String, String>();


			openlinkManger = new OpenlinkCommandManager();
			//openlinkManger.addCommand(new GetProfiles(this));
			//openlinkManger.addCommand(new GetProfile(this));
			//openlinkManger.addCommand(new GetInterests(this));
			//openlinkManger.addCommand(new GetInterest(this));
			//openlinkManger.addCommand(new GetFeatures(this));
			//openlinkManger.addCommand(new MakeCall(this));
			//openlinkManger.addCommand(new IntercomCall(this));
			//openlinkManger.addCommand(new RequestAction(this));
			//openlinkManger.addCommand(new SetFeature(this));
			//openlinkManger.addCommand(new QueryFeatures(this));
			openlinkManger.addCommand(new ManageVoiceBridge(this));

		}
		catch(Exception e) {
			Log.error(e);
		}

    }


	public void componentDestroyed()
	{
		try {
			openlinkManger.stop();
			componentManager.removeComponent(getName());

		}
		catch(Exception e) {
			Log.error(e);
		}
	}

	public void setApplication(Application application)
	{
		this.application = application;
	}

	public void sendPacket(Packet packet)
	{
		try {
			componentManager.sendPacket(this, packet);

		} catch (Exception e) {
			Log.error("Exception occured while sending packet." + e);

		}
	}

//-------------------------------------------------------
//
//
//
//-------------------------------------------------------

	@Override public String getDescription()
	{
		return "Openlink Component";
	}


	@Override public String getName()
	{
		return "openlink";
	}

	@Override public String getDomain()
	{
		String hostName =  XMPPServer.getInstance().getServerInfo().getHostname();
		return JiveGlobals.getProperty("xmpp.domain", hostName);
	}

	@Override public void postComponentStart()
	{

	}

	@Override public void postComponentShutdown()
	{

	}

	public JID getComponentJID()
	{
		return new JID(getName() + "." + getDomain());
	}

//-------------------------------------------------------
//
//
//
//-------------------------------------------------------

	@Override protected void handlePresence(Presence presence)
	{
		// <presence to="lobbytkdfvwwbz@openlink.btg199251" from="lobby@conference.btg199251/lobbytkdfvwwbz" type="unavailable">
		// <x xmlns="http://jabber.org/protocol/muc#user">
		//   <item jid="lobbytkdfvwwbz@openlink.btg199251" affiliation="none" role="none">
		//     <actor jid="admin@btg199251"/>
		//   </item><status code="307"/>
		// </x></presence>

		Log.debug("handlePresence \n"+ presence.toString());

		String callId = presence.getFrom().getResource();

		if (presence.getType() == Presence.Type.unavailable)
		{
			Log.info("handlePresence clearing call "+ callId);
			CallHandler.hangup(callId, "User left call");
		}
	}

    @Override protected void handleMessage(Message received)
    {
		Log.debug("handleMessage \n"+ received.toString());
    }

	@Override protected void handleIQResult(IQ iq)
	{
		Log.debug("handleIQResult \n"+ iq.toString());

		Element element = iq.getChildElement();

		if (element != null)
		{
			String namespace = element.getNamespaceURI();

			if("http://jabber.org/protocol/pubsub#owner".equals(namespace))
			{
				Element subscriptions = element.element("subscriptions");

				if (subscriptions != null)
				{
					String node = subscriptions.attributeValue("node");

					Log.info("handleIQResult found subscription node " + node);

					if (openlinkUserInterests.containsKey(node))
					{
						Log.info("handleIQResult found user interest " + node);

						OpenlinkUserInterest openlinkUserInterest = openlinkUserInterests.get(node);

						for ( Iterator<Element> i = subscriptions.elementIterator( "subscription" ); i.hasNext(); )
						{
							Element subscription = (Element) i.next();
							JID jid = new JID(subscription.attributeValue("jid"));
							String sub = subscription.attributeValue("subscription");

							OpenlinkSubscriber openlinkSubscriber = openlinkUserInterest.getSubscriber(jid);
							openlinkSubscriber.setSubscription(sub);
							setSubscriberDetails(jid, openlinkSubscriber);

							Log.info("handleIQResult added subscriber " + jid);

						}
					}

				}
			}
		}
	}

	@Override protected void handleIQError(IQ iq)
	{
		Log.debug("handleIQError \n"+ iq.toString());
	}

   @Override public IQ handleDiscoInfo(IQ iq)
    {
    	JID jid = iq.getFrom();
		Element child = iq.getChildElement();
		String node = child.attributeValue("node");

		IQ iq1 = IQ.createResultIQ(iq);
		iq1.setType(org.xmpp.packet.IQ.Type.result);
		iq1.setChildElement(iq.getChildElement().createCopy());

		Element queryElement = iq1.getChildElement();
		Element identity = queryElement.addElement("identity");

		queryElement.addElement("feature").addAttribute("var",JINGLE_NAMESPACE);
		queryElement.addElement("feature").addAttribute("var",RAW_UDP_NAMESPACE);
		queryElement.addElement("feature").addAttribute("var",RTP_AUDIO);

		queryElement.addElement("feature").addAttribute("var",NAMESPACE_DISCO_INFO);
		queryElement.addElement("feature").addAttribute("var",NAMESPACE_XMPP_PING);

		identity.addAttribute("category", "component");
		identity.addAttribute("name", "Openlink");

		if (node == null) 				// Disco discovery of openlink
		{
			identity.addAttribute("type", "command-list");
			queryElement.addElement("feature").addAttribute("var", "http://jabber.org/protocol/commands");
			queryElement.addElement("feature").addAttribute("var", "http://xmpp.org/protocol/openlink:01:00:00");


		} else {

			// Disco discovery of Openlink command

			OpenlinkCommand command = openlinkManger.getCommand(node);

			if (command != null && command.hasPermission(jid))
			{
				identity.addAttribute("type", "command-node");
				queryElement.addElement("feature").addAttribute("var", "http://jabber.org/protocol/commands");
				queryElement.addElement("feature").addAttribute("var", "http://xmpp.org/protocol/openlink:01:00:00");
			}

		}
		//Log.debug("handleDiscoInfo "+ iq1.toString());
		return iq1;
    }


   @Override public IQ handleDiscoItems(IQ iq)
    {
    	JID jid = iq.getFrom();
		Element child = iq.getChildElement();
		String node = child.attributeValue("node");

		IQ iq1 = IQ.createResultIQ(iq);
		iq1.setType(org.xmpp.packet.IQ.Type.result);
		iq1.setChildElement(iq.getChildElement().createCopy());

		Element queryElement = iq1.getChildElement();
		Element identity = queryElement.addElement("identity");

		identity.addAttribute("category", "component");
		identity.addAttribute("name", "openlink");
		identity.addAttribute("type", "command-list");

		if ("http://jabber.org/protocol/commands".equals(node))
		{
			for (OpenlinkCommand command : openlinkManger.getCommands())
			{
				// Only include commands that the sender can invoke (i.e. has enough permissions)

				if (command.hasPermission(jid))
				{
					Element item = queryElement.addElement("item");
					item.addAttribute("jid", componentJID.toString());
					item.addAttribute("node", command.getCode());
					item.addAttribute("name", command.getLabel());
				}
			}
		}
		//Log.debug("handleDiscoItems "+ iq1.toString());
		return iq1;
    }

   @Override public IQ handleIQGet(IQ iq)
    {
		return handleIQPacket(iq);
	}

   @Override public IQ handleIQSet(IQ iq)
    {
		return handleIQPacket(iq);
	}

   private IQ handleIQPacket(IQ iq)
   {
		Log.debug("handleIQPacket \n"+ iq.toString());

		Element element = iq.getChildElement();
		IQ iq1 = IQ.createResultIQ(iq);
		iq1.setType(org.xmpp.packet.IQ.Type.result);
		iq1.setChildElement(iq.getChildElement().createCopy());

		if (element != null)
		{
			String namespace = element.getNamespaceURI();

			if("http://jabber.org/protocol/commands".equals(namespace))
				iq1 = openlinkManger.process(iq);

			if(JINGLE_NAMESPACE.equals(namespace))
				handleJingle(element, iq.getFrom(), iq.getTo().getNode());
		}
		return iq1;
	}


//-------------------------------------------------------
//
//	Jingle
//
//-------------------------------------------------------


/*
			<iq type="set" to="3366\40login.zipdx.com@sip" id="2496:sendIQ">
				<jingle xmlns="urn:xmpp:jingle:1" action="session-initiate"initiator="61d1fec1@btg199251/61d1fec1" sid="04575b7e52348f020dd949a183d43cd1">
					<content creator="initiator">
					   <description xmlns="http://voxeo.com/gordon/apps/rtmp">
						<payload-type id="97" name="SPEEX" clockrate="8000"/>
						<payload-type id="97" name="SPEEX"clockrate="16000"/>
					   </description>
					   <transport xmlns="http://voxeo.com/gordon/transports/rtmp"/>
					</content>
				</jingle>
			</iq>
*/

   	private void handleJingle(Element jingle, JID from, String conference)
   	{
		Log.info("handleJingle " + conference + " " + from + "\n" + jingle.toString());

		if (conference == null) return;

		String action = jingle.attribute("action").getValue();
		String sid = jingle.attribute("sid").getValue();
		String codecId = "0";
		String codecName = "PCMU";
		String codecClock = "8000";
		String remoteIP = null;
		String remotePort = null;
		String rtmpUrl = null;
		String playName = null;
		String publishName = null;
		String cryptoSuite = null;
		String keyParams = null;
		String sessionParams = null;
		String ssrc = null;
		String username = "MnEBbD+P7snEQs7M";
		String password = "sq+V1dtjWATOth4fPaZcVXEJ";

		if ("session-initiate".equals(action) || "session-accept".equals(action))
		{
			Element content =  jingle.element("content");
			Element description =  content.element("description");
			Element transport =  content.element("transport");

			if (description != null)
			{
				Iterator i = description.elementIterator("payload-type");

				while ( i.hasNext())
				{
					Element payload = (Element) i.next();

					if ("0".equals(payload.attribute("id").getValue()))
					{
						codecId = payload.attribute("id").getValue();
						codecName = payload.attribute("name").getValue();
						codecClock = payload.attribute("clockrate").getValue();
					}
				}

				Element encryption =  description.element("encryption");

				if (encryption != null)
				{
					String required = encryption.attribute("required").getValue();

					if ("1".equals(required))
					{
						Element crypto =  encryption.element("crypto");

						if (crypto != null)
						{
							cryptoSuite = crypto.attribute("crypto-suite").getValue();
							keyParams = crypto.attribute("key-params").getValue();
							sessionParams = crypto.attribute("session-params").getValue();
						}
					}
				}

				Element streams =  description.element("streams");

				if (streams != null)
				{
					Iterator j = streams.elementIterator("stream");

					if ( j.hasNext())
					{
						Element stream = (Element) j.next();
						ssrc = stream.element("ssrc").getText();
					}

				}

			} else Log.warn("handleJingle missing description");

			if (transport != null)
			{
				if (transport.attribute("ufrag") != null)
				{
					username = transport.attribute("ufrag").getValue();
					password = transport.attribute("pwd").getValue();
				}

				Iterator i = transport.elementIterator("candidate");

				if ( i.hasNext() == false)
				{
					Element webrtc =  transport.element("webrtc");

					if (webrtc != null)
					{
						handleWebRtc(from, sid, conference, webrtc.getText(), action);
					}

				} else {
					Element candidate = (Element) i.next();

            		if (candidate.attribute("ip") != null)
            		{
						remoteIP = candidate.attribute("ip").getValue();
					}

            		if (candidate.attribute("port") != null)
            		{
						remotePort = candidate.attribute("port").getValue();
					}

            		if (candidate.attribute("rtmpuri") != null)
            		{
						rtmpUrl = candidate.attribute("rtmpuri").getValue();
					}

            		if (candidate.attribute("playname") != null)
            		{
						playName = candidate.attribute("playname").getValue();
					}

            		if (candidate.attribute("publishname") != null)
            		{
						publishName = candidate.attribute("publishname").getValue();
					}

					if (rtmpUrl != null && publishName != null && playName != null)
					{
						Log.info("handleJingle RTMP/RTMFP url " + rtmpUrl + " " + publishName + " " + playName);

						if ("session-initiate".equals(action) )
						{
							String protocol = "RTMP";

							if (rtmpUrl.startsWith("rtmfp:")) protocol = "RTMFP";

							// Accept Jingle RTMP/RTMFP call

							CallParticipant cp = new CallParticipant();
							cp.setCallId(sid);
							cp.setProtocol(protocol);
							cp.setPhoneNumber(from.toString());
							cp.setConferenceId(from.getNode());
							cp.setConferenceDisplayName(conference);

							cp.setFromPhoneNumber(rtmpUrl);
							cp.setRtmpSendStream(playName);			// remote play name
							cp.setRtmpRecieveStream(publishName);	// remote publish name

							sendJingleAction("session-accept", cp, new JinglePayload(codecId, codecName, codecClock, null, null));
						}
					}


					if (remoteIP != null && remotePort != null)
					{
						JinglePayload jinglePayload = new JinglePayload(codecId, codecName, codecClock, remotePort, remoteIP);

						if ("session-initiate".equals(action) )
						{
							CallParticipant cp = new CallParticipant();
							String fromJID = from.toString();

							cp.setCallId(sid);
							cp.setPhoneNumber(fromJID);
							cp.setProtocol("JINGLE");
							cp.setConferenceId(from.getNode());
							cp.setConferenceDisplayName(conference);

							cp.setSsrc(ssrc);
							cp.setPassword(password);
							cp.setUsername(username);

							if (cryptoSuite != null && keyParams != null)
							{
								cp.setEncryptionKey(keyParams);
								cp.setEncryptionAlgorithm(cryptoSuite);
								cp.setEncryptionParams(sessionParams);
							}

							if (isJidInConf(conference, from.getNode()) == false)
							{
								Log.warn("handleJingle access denied " + from + " " + conference);

								sendJingleTerminate(cp);
								return;
							}

							// Accept Jingle RTP call

							new IncomingCallHandler(cp, jinglePayload);

						} else {

							CallHandler jingleHandler = CallHandler.findCall(sid);

							if (jingleHandler != null)
							{
								JingleOutgoingCallAgent jingleAgent = (JingleOutgoingCallAgent) jingleHandler.getCallSetupAgent();
								jingleAgent.callAccepted(jinglePayload);
							}
						}
					}
				}

			} else Log.warn("handleJingle missing transport");
		}

		if ("session-terminate".equals(action))
		{
			try {
				CallHandler jingleHandler = CallHandler.findCall(sid);

				if (jingleHandler != null)
				{
					CallSetupAgent jingleAgent = (CallSetupAgent) jingleHandler.getCallSetupAgent();
					jingleAgent.cancelRequest("call terminated");
				}
			} catch (Exception e) {}
		}
	}


	public boolean isJidInConf(String roomName, String participant)
	{
		if (roomName.indexOf(participant) > -1)
		{
			return true;	// private room just allow
		}

		if (roomName.indexOf("\\40") > -1)
		{
			return true;	// sip address for bridged call, allow
		}

		boolean found = true;	// you can enter any audio conf

		Log.info( "isJidInConf looking for " + roomName + " " + participant);

		if (XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").hasChatRoom(roomName))
		{
			MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").getChatRoom(roomName);

            if (room != null)
            {
				found = false;	// // you can only enter any group audio conf only if you are already in it

                for (MUCRole role : room.getOccupants())
                {
					Log.info( "isJidInConf checking " + role.getRoleAddress());

					if (participant.equals(role.getUserAddress().getNode()))
					{
						Log.info( "isJidInConf found memember " + participant);
						found = true;
						break;
					}
                }
            }
		}

		return found;
	}

	public void sendJingleTerminate(CallParticipant cp)
	{
		String sid = cp.getCallId();
		String to = cp.getPhoneNumber();
		String conference = cp.getConferenceDisplayName();

		IQ iq = new IQ(IQ.Type.set);
		iq.setFrom(conference + "@" + getComponentJID());
		iq.setTo(to);

		Element jingle = iq.setChildElement("jingle", JINGLE_NAMESPACE).addAttribute("sid", sid).addAttribute("action", "session-terminate");
		jingle.addElement("reason").addElement("sucess");

		sendPacket(iq);
	}

	/*
	<content creator='initiator' name='audio'>
		<description xmlns='urn:xmpp:jingle:apps:rtp:1' profile='RTP/SAVPF' media='audio'>
			<payload-type id='0' name='PCMU' clockrate='8000'/>
			<encryption required='1'>
				<crypto crypto-suite='AES_CM_128_HMAC_SHA1_32' key-params='inline:f1TD/wKRR8Jpya+YCL47ZChUrlvRLLDbJxiqqHGn' session-params='' tag='0'/>
				<crypto crypto-suite='AES_CM_128_HMAC_SHA1_80' key-params='inline:BxXbmQypg01h2fh7eHNabpzj2HWrTNryO8XEzyR7' session-params='' tag='1'/>
			</encryption>
			<streams>
				<stream cname='ZuBw/TRn2mPj0MHB' mslabel='fS2BDaJ3EvjpCScYfyMKKZRqrilk7eFj3xWh' label='fS2BDaJ3EvjpCScYfyMKKZRqrilk7eFj3xWh00'>
					<ssrc>954782039</ssrc>
				</stream>
			</streams>
		</description>
		<transport xmlns='urn:xmpp:jingle:transports:raw-udp:1'>
			<candidate ip='192.168.1.88' port='61032' generation='0'/>
		</transport>
		<transport xmlns='urn:xmpp:jingle:transports:ice-udp:1' pwd="sq+V1dtjWATOth4fPaZcVXEJ" ufrag="MnEBbD+P7snEQs7M">
			<candidate component='1' foundation='1001321590' protocol='udp' priority='2130714367' ip='192.168.1.88' port='61032' type='host' generation='0'/>
		</transport>

	</content>

	*/

   	public void sendJingleAction(String action, CallParticipant cp, JinglePayload payload)
   	{
		String sid = cp.getCallId();
		JID to = new JID(cp.getPhoneNumber());
		String conference = cp.getConferenceDisplayName();

		IQ iq = new IQ(IQ.Type.set);
		iq.setFrom(conference + "@" + getComponentJID());
		iq.setTo(to);

		Element jingle = iq.setChildElement("jingle", JINGLE_NAMESPACE).addAttribute("sid", sid).addAttribute("action", action);

		if ("session-initiate".equals(action))
			jingle.addAttribute("initiator", conference + "@" + getComponentJID());
		else
			jingle.addAttribute("responder", conference + "@" + getComponentJID());

		Element newContent = jingle.addElement("content");

		newContent.addAttribute("creator", conference + "@" + getComponentJID());
		newContent.addAttribute("name", conference);
		newContent.addAttribute("senders", "both");

		Element newDescription = newContent.addElement("description", RTP_AUDIO).addAttribute("media", "audio").addAttribute("profile", cp.getEncryptionAlgorithm() != null ? "RTP/SAVPF" : "RTP/AVPF");
		newDescription.addElement("payload-type").addAttribute("id", payload.codecId).addAttribute("name", payload.codecName).addAttribute("clockrate", payload.codecClock);

		if (cp.getEncryptionAlgorithm() != null)
		{
			Element crypto = newDescription.addElement("encryption").addAttribute("required", "1").addElement("crypto");

			crypto.addAttribute("crypto-suite", cp.getEncryptionAlgorithm());
			crypto.addAttribute("key-params", cp.getEncryptionKey());
			crypto.addAttribute("session-params", cp.getEncryptionParams());
			crypto.addAttribute("tag", "1");
		}

		if (cp.getSsrc() != null)
		{
			Element streams = newDescription.addElement("streams");
			Element stream = streams.addElement("stream").addAttribute("cname", "iirNX3Znb0iT+aow").addAttribute("mslabel", "fAy0FNrYIDVfeRwX5X0IK5TOCVTNJOXt4Cdb").addAttribute("label", "fAy0FNrYIDVfeRwX5X0IK5TOCVTNJOXt4Cdb00");
			stream.addElement("ssrc").setText(cp.getSsrc());
		}

		if ("RTMP".equals(cp.getProtocol()) || "RTMFP".equals(cp.getProtocol()))
		{
			Element newTransportRtmp= newContent.addElement("transport", "http://voxeo.com/gordon/transports/rtmp");
			newTransportRtmp.addElement("candidate").addAttribute("rtmpuri", cp.getFromPhoneNumber()).addAttribute("publishname", cp.getRtmpRecieveStream()).addAttribute("playname", cp.getRtmpSendStream());

		} else {

			Element newTransportRaw = newContent.addElement("transport", RAW_UDP_NAMESPACE).addAttribute("pwd", cp.getPassword()).addAttribute("ufrag", cp.getUsername());
			newTransportRaw.addElement("candidate").addAttribute("ip", payload.remoteIP).addAttribute("port", payload.remotePort).addAttribute("generation", "0").addAttribute("id", "1").addAttribute("component", "1").addAttribute("foundation", "1001321590").addAttribute("priority", "2130714367");
			newTransportRaw.addElement("webrtc").setText(getWebRtcSdp(cp, payload));
		}

		sendPacket(iq);

		// make outgoing SIP call

		makeOutgoingSipCall(to, cp);
	}


	private void makeOutgoingSipCall(JID from, CallParticipant cp)
	{
		String protocol 	= cp.getProtocol();
		String sid 			= cp.getCallId();
		String conference 	= cp.getConferenceDisplayName();
		String playName 	= cp.getRtmpSendStream();
		String publishName 	= cp.getRtmpRecieveStream();
		String rtmpUrl 		= cp.getFromPhoneNumber();

		List<Object[]> actionList = new ArrayList<Object[]>();

		actionList.add(new String[]{"Protocol", sid, protocol});

		if (playName != null)
			actionList.add(new String[]{"RtmpSendStream", sid, playName}); 			// remote play name

		if (publishName != null)
			actionList.add(new String[]{"RtmpRecieveStream", sid, publishName});	// remote publish name

		boolean isConf = false;
		String destination = conference;
		int pos = conference.indexOf("\\40");

		if (pos > - 1)										// sip address destination
		{
			destination = "sip:" + conference.substring(0, pos) + "@" + conference.substring(pos + 3);

		} else {											// check if destination is MUC

			isConf = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").hasChatRoom(destination);
		}

		Log.info("handleJingle call sip: " + destination);

		boolean secondleg = false;

		actionList.add(new String[]{"SetConference", sid, cp.getConferenceId()});

		if ("JINGLE".equals(protocol))					// Only SIP leg media needs to established. Jingle leg already established
		{
			if (isConf == false)
			{
				actionList.add(new String[]{"SetPhoneNo", sid, destination});
				secondleg = true;
			}

		} else {										// RTMP/RTMFP both media legs need to established

			actionList.add(new String[]{"SetPhoneNo", sid, rtmpUrl});

			if (isConf == false)
			{
				actionList.add(new String[]{"Set2ndPartyPhoneNo", sid, destination});
				secondleg = true;

			} else {

				if (isJidInConf(conference, from.getNode()))
				{
					secondleg = true;					// user must be in MUC already. Security check
				}
			}
		}

		if (secondleg)
		{
			actionList.add(new String[]{"MakeCall", sid, null});
			manageVoiceBridge(null, new JID(from.getNode() + "@" + getComponentJID()), actionList);

			callParticipants.put(sid, cp);

			if ("JINGLE".equals(protocol) == false)
			{
				callStreams.put(cp.getRtmpSendStream(), sid);
				callStreams.put(cp.getRtmpRecieveStream(), sid);
			}
		}
	}

    public void sendDigit(String stream, String digit)
    {
        Log.info("sendDigit " + stream + " " + digit );

		if (callStreams.containsKey(stream))
		{
			String sid = callStreams.get(stream);

        	Log.info("sendDigit found " + sid);

			CallHandler callHandler = CallHandler.findCall(sid);
			callHandler.dtmfKeys(digit);
		}
	}


	private String getToken(String sdp, String token, String delim)
	{
		int pos = sdp.indexOf(token);
		String para = null;

		if (pos > -1)
		{
			para = sdp.substring(pos + token.length());

			if (para.indexOf(delim) > -1)
			{
				para = para.substring(0, para.indexOf(delim));

			} else {

				para = para.substring(0, para.indexOf("\n"));
			}

			para = para.trim();
		}

		return para;
	}

	private String getWebRtcSdp(CallParticipant cp, JinglePayload payload)
	{
		String ssrc = cp.getSsrc();
		String ssrc_cname = "iirNX3Znb0iT+aow";
		String ssrc_mslabel = "fAy0FNrYIDVfeRwX5X0IK5TOCVTNJOXt4Cdb";
		String ssrc_label = "fAy0FNrYIDVfeRwX5X0IK5TOCVTNJOXt4Cdb00";

		String candidate_foundation = "1001321590";
		String candidate_priority = "2130714367";

		String ice_ufrag = "wZPq/BJNlo0K6ej5";
		String ice_pwd = "hLaFhH8Yfl+XeExexulHT42o";

		String payload_id = "0";
		String payload_name = "PCMU";
		String payload_clockrate = "8000";

		String sdp = "";

		sdp += "v=0\r\n";
		sdp += "o=- " + cp.getCallId() + " 1 IN IP4 127.0.0.1\r\n";
		sdp += "s=canary\r\n";
		sdp += "t=0 0\r\n";
		sdp += "a=group:BUNDLE audio\r\n";
		sdp += "m=audio " + payload.remotePort + " RTP/SAVPF " + payload_id + "\r\n";
		sdp += "c=IN IP4 " + payload.remoteIP + "\r\n";
		sdp += "a=rtcp:" + payload.remotePort + " IN IP4 " + payload.remoteIP + "\r\n";
		sdp += "a=candidate:" + candidate_foundation + " 1 udp " + candidate_priority + " " + payload.remoteIP + " " + payload.remotePort + " typ host generation 0\r\n";
		sdp += "a=ice-ufrag:" + ice_ufrag + "\r\n";
		sdp += "a=ice-pwd:" + ice_pwd + "\r\n";
		sdp += "a=sendrecv\r\n";
		sdp += "a=mid:audio\r\n";
		sdp += "a=rtcp-mux\r\n";
		sdp += "a=crypto:1 AES_CM_128_HMAC_SHA1_80 " + cp.getEncryptionKey() + "\r\n";
		sdp += "a=rtpmap:" + payload_id + " " + payload_name + "/" + payload_clockrate + "\r\n";
		sdp += "a=ssrc:" + ssrc + " cname:" + ssrc_cname + "\r\n";
		sdp += "a=ssrc:" + ssrc + " mslabel:" + ssrc_mslabel + "\r\n";
		sdp += "a=ssrc:" + ssrc + " label:" + ssrc_label + "\r\n";

		Log.info("getWebRtcSdp \n" + sdp);

		return sdp;
	}



	private void handleWebRtc(JID from, String sid, String conference, String sdp, String action)
	{
		Log.info("handleWebRtc \n" + sdp);

		String ipaddr 	= this.getToken(sdp, "c=IN IP4 ", "\n");
		String port 	= this.getToken(sdp, "m=audio ", " ");
		String ufrag 	= this.getToken(sdp, "a=ice-ufrag:", "\n");
		String password	= this.getToken(sdp, "a=ice-pwd:", "\n");
		String crypto1 	= this.getToken(sdp, "AES_CM_128_HMAC_SHA1_80 ", "\n");
		String crypto2	= this.getToken(sdp, "AES_CM_128_HMAC_SHA1_32 ", "\n");
		String ssrc  	= this.getToken(sdp, "a=ssrc:", " ");
		String cname 	= this.getToken(sdp, "a=ssrc:" + ssrc + " cname:", "\n");
		String mslabel 	= this.getToken(sdp, "a=ssrc:" + ssrc + " mslabel:", "\n");
		String label 	= this.getToken(sdp, "a=ssrc:" + ssrc + " label:", "\n");
		String fndtn 	= this.getToken(sdp, "a=candidate:", " ");
		String prior	= this.getToken(sdp, "a=candidate:" + fndtn + " 1 udp ", " ");

		String codecId = "0";
		String codecName = "PCMU";
		String codecClock = "8000";

		Log.info("sid = " + sid + " ip " + ipaddr + " port " + port + " ufrag " + ufrag + " passw " + password + " crypto " + crypto1 + " ssrc cname " + cname);

		JinglePayload jinglePayload = new JinglePayload(codecId, codecName, codecClock, port, ipaddr);

		if ("session-initiate".equals(action) )
		{
			CallParticipant cp = new CallParticipant();
			String fromJID = from.toString();

			cp.setCallId(sid);
			cp.setPhoneNumber(fromJID);
			cp.setProtocol("JINGLE");
			cp.setConferenceId(from.getNode());
			cp.setConferenceDisplayName(conference);

			cp.setSsrc(ssrc);
			cp.setPassword(password);
			cp.setUsername(ufrag);

			if (crypto1 != null)
			{
				cp.setEncryptionKey(crypto1);
				cp.setEncryptionAlgorithm("AES_CM_128_HMAC_SHA1_80");
				cp.setEncryptionParams("KDR=0");
			}

			if (crypto2 != null)
			{
				cp.setEncryptionKey(crypto2);
				cp.setEncryptionAlgorithm("AES_CM_128_HMAC_SHA1_32");
				cp.setEncryptionParams("KDR=0");
			}

			if (isJidInConf(conference, from.getNode()) == false)
			{
				Log.warn("handleJingle access denied " + from + " " + conference);

				sendJingleTerminate(cp);
				return;
			}

			// Accept Jingle RTP call

			new IncomingCallHandler(cp, jinglePayload);

		} else {

			CallHandler jingleHandler = CallHandler.findCall(sid);

			if (jingleHandler != null)
			{
				JingleOutgoingCallAgent jingleAgent = (JingleOutgoingCallAgent) jingleHandler.getCallSetupAgent();
				jingleAgent.callAccepted(jinglePayload);
			}
		}
	}

//-------------------------------------------------------
//
//
//
//-------------------------------------------------------


    public void incomingCallNotification(CallEvent callEvent)
    {
		Log.info("incomingCallNotification " + callEvent.toString());

    }


    public void outgoingCallNotification(CallEvent callEvent)
    {
		Log.info( "outgoingCallNotification " + callEvent.toString());

		try {

			if (callEvent.getCallState().equals(CallState.ENDED))
			{
				if (callParticipants.containsKey(callEvent.getCallId()))
				{
					Log.info( "outgoingCallNotification  found existing call. Terminating");

					CallParticipant cp = callParticipants.remove(callEvent.getCallId());

					sendJingleTerminate(cp);

					callStreams.remove(cp.getRtmpSendStream());
					callStreams.remove(cp.getRtmpRecieveStream());
				}
			}


		} catch (Exception e) {

			Log.error("Error in outgoingCallNotification " + e);
			e.printStackTrace();
		}
    }

//-------------------------------------------------------
//
// Openlink Commands
//
//-------------------------------------------------------


	public String manageVoiceBridge(Element newCommand, JID userJID, List<Object[]> actions)
	{
		Log.debug( "manageVoiceMessage " + userJID + " ");
		String errorMessage = "";
		List<String> actionList = new ArrayList<String>();

		try {

			if (application != null)
			{
				if (actions != null && actions.size() > 0)
				{
					Iterator it = actions.iterator();

					while( it.hasNext() )
					{
						Object[] action = (Object[])it.next();
						String name = (String) action[0];
						String value1 = (String) action[1];
						String value2 = (String) action[2];

						String thisErrorMessage = application.manageCallParticipant(userJID, value1, name, value2);

						if (thisErrorMessage == null)
						{
							if ("MakeCall".equalsIgnoreCase(name))
							{
								actionList.add(value1);
							}

						} else {

							errorMessage = errorMessage + thisErrorMessage + "; ";
						}

					}

					if (actionList.size() > 0)
					{
						application.handlePostBridge(actionList);
					}

				} else errorMessage = "Voice bridge actions are missing";

			} else errorMessage = "Voice bridge failure";

		}
		catch(Exception e) {
			Log.error("manageVoiceBridge " + e);
			e.printStackTrace();
			errorMessage = "Internal error - " + e.toString();
		}

        return errorMessage.length() == 0 ? null : errorMessage;
	}


	public String setFeature(Element newCommand, String profileID, String featureID, String value1, String value2)
	{
		Log.info( "setFeature " + profileID + " " + featureID + " " + value1 + " " + value2);
		String errorMessage = null;

		try {

			if (value1 != null && value1.length() > 0)
			{
				OpenlinkUser openlinkUser = getOpenlinkProfile(profileID);

				if (openlinkUser != null)
				{
					if ("hs_1".equals(featureID))
					{
						if (validateTrueFalse(value1))
							openlinkUser.setHandsetNo("true".equals(value1.toLowerCase()) ? "1" : "2");
						else
							errorMessage = "value1 is not true or false";

					}

					else if ("hs_2".equals(featureID))
					{
						if (validateTrueFalse(value1))
							openlinkUser.setHandsetNo("true".equals(value1.toLowerCase()) ? "2" : "1");
						else
							errorMessage = "value1 is not true or false";
					}

					else if ("priv_1".equals(featureID))
					{
						if (validateTrueFalse(value1))
							openlinkUser.setAutoPrivate("true".equals(value1.toLowerCase()));
						else
							errorMessage = "value1 is not true or false";
					}

					else if ("hold_1".equals(featureID))
					{
						if (validateTrueFalse(value1))
							openlinkUser.setAutoHold("true".equals(value1.toLowerCase()));
						else
							errorMessage = "value1 is not true or false";
					}

					else if ("callback_1".equals(featureID))
					{
						if (validateTrueFalse(value1))
						{
							if ("true".equals(value1.toLowerCase()))
							{
								if (value2 != null && !"".equals(value2))
								{
									String dialableNumber = makeDialableNumber(value2);

									if (dialableNumber != null && !"".equals(dialableNumber))
									{
										openlinkUser.setCallback(dialableNumber);
										OpenlinkCallback openlinkCallback = null; //openlinkLinkService.allocateCallback(openlinkUser);

										if (openlinkCallback == null)
											errorMessage = "unable to allocate a virtual turret";

									} else errorMessage = "value2 is not a dialable number";

								} else {

									if (openlinkUser.getCallback() != null)
									{
										OpenlinkCallback openlinkCallback = null; //openlinkLinkService.allocateCallback(openlinkUser);

										if (openlinkCallback == null)
											errorMessage = "unable to allocate a callback";

									} else errorMessage = "calback destination is missing";
								}

							} else  {

								//openlinkLinkService.freeCallback(openlinkUser.getUserNo());
								openlinkUser.setPhoneCallback(null);
							}
						}
						else errorMessage = "value1 is not true or false";
					}

					else if ("fwd_1".equals(featureID))	// call forward
					{
						if (openlinkInterests.containsKey(value1))	// value is interest id
						{
							OpenlinkUserInterest openlinkUserInterest = openlinkUserInterests.get(value1);

							if (openlinkUser.getUserNo().equals(openlinkUser.getUserNo()))
							{
								if ("D".equals(openlinkUserInterest.getInterest().getInterestType()))
								{
									String dialDigits = null;

									if (value2 == null || "".equals(value2))
									{
										dialDigits = value2;
										//errorMessage = doCallForward(dialDigits, openlinkUserInterest, newCommand);

										if (errorMessage == null)
										{
											Iterator<OpenlinkUserInterest> iter2 = openlinkUserInterest.getInterest().getUserInterests().values().iterator();

											while( iter2.hasNext() )
											{
												OpenlinkUserInterest theUserInterest = (OpenlinkUserInterest)iter2.next();
												theUserInterest.setCallFWD("false");
											}

											openlinkUser.setLastCallForward("");
										}

									} else {

										String dialableNumber = value2;

										if (dialableNumber != null && !"".equals(dialableNumber))
										{
											dialDigits = dialableNumber;
											//errorMessage = doCallForward(dialDigits, openlinkUserInterest, newCommand);

											if (errorMessage == null)
											{
												Iterator<OpenlinkUserInterest> iter2 = openlinkUserInterest.getInterest().getUserInterests().values().iterator();

												while( iter2.hasNext() )
												{
													OpenlinkUserInterest theUserInterest = (OpenlinkUserInterest)iter2.next();
													theUserInterest.setCallFWD("true");
													theUserInterest.setCallFWDDigits(value2);
												}

												openlinkUser.setLastCallForwardInterest(value1);
												openlinkUser.setLastCallForward(value2);
											}

										} else errorMessage = "value2 is not a dialable number";
									}

								} else errorMessage = "CallForward requires a directory number interest";

							} else errorMessage = "Interest does not belong to this profile";

						} else errorMessage = "Interest not found";
					}
					else errorMessage = "Feature not found";

				} else errorMessage = "Profile not found";

			} else errorMessage = "Input1 is missing";
		}
		catch(Exception e) {
			Log.error("setFeature " + e);
        	e.printStackTrace();
			errorMessage = "Internal error - " + e.toString();
		}

        return errorMessage;
	}

//-------------------------------------------------------
//
// Misc
//
//-------------------------------------------------------

	public List<OpenlinkUser> getOpenlinkProfiles(JID jid)
	{
		List<OpenlinkUser> openlinkUsers = new ArrayList();
		String userName = jid.getNode();

		if (jid.getDomain().indexOf(getDomain()) > -1)
		{
			Iterator<OpenlinkUser> it = openlinkUserTable.values().iterator();

			while( it.hasNext() )
			{
				OpenlinkUser openlinkUser = (OpenlinkUser)it.next();

				if (userName.equals(openlinkUser.getUserId()))
				{
					openlinkUsers.add(openlinkUser);
				}
			}
		}

		return openlinkUsers;
	}

	public OpenlinkUser getOpenlinkUser(JID jid)
	{
		return getOpenlinkUser(jid.getNode());
	}


	public OpenlinkUser getOpenlinkUser(String userName)
	{
		Iterator<OpenlinkUser> it = openlinkUserTable.values().iterator();

		while( it.hasNext() )
		{
			OpenlinkUser openlinkUser = (OpenlinkUser)it.next();

			if (userName.equals(openlinkUser.getUserId()) && !"0.0.0.0".equals(openlinkUser.getDeviceNo()))
			{
				return openlinkUser;
			}
		}
		return null;
	}


	public OpenlinkUser getOpenlinkProfile(String profileID)
	{
		OpenlinkUser openlinkUser = null;

		if (openlinkUserTable.containsKey(profileID))
		{
			openlinkUser = openlinkUserTable.get(profileID);
		}

		return openlinkUser;
	}

	public OpenlinkUserInterest getOpenlinkUserInterest(String userInterest)
	{
		OpenlinkUserInterest openlinkUserInterest = null;

		if (openlinkUserInterests.containsKey(userInterest))
		{
			openlinkUserInterest = openlinkUserInterests.get(userInterest);
		}

		return openlinkUserInterest;
	}

    public int getUserCount()
    {
        return openlinkUserTable.values().size();
    }

    public List<OpenlinkUser> getUsers(int startIndex, int numResults)
    {
		List<OpenlinkUser> profiles  = new ArrayList<OpenlinkUser>();
		Vector<OpenlinkUser> sortedProfiles =  new Vector<OpenlinkUser>();

		int counter = 0;

		if (startIndex == 0 || sortedProfiles.size() == 0)
		{
			sortedProfiles = new Vector<OpenlinkUser>(openlinkUserTable.values());
			Collections.sort(sortedProfiles);
		}

		Iterator it = sortedProfiles.iterator();

		while( it.hasNext() )
		{
			OpenlinkUser openlinkUser = (OpenlinkUser)it.next();

			if (counter > (startIndex + numResults))
			{
				break;
			}

			if (counter >= startIndex)
			{
				profiles.add(openlinkUser);
			}

			counter++;
		}

        return profiles;
    }
//-------------------------------------------------------
//
//
//
//-------------------------------------------------------


    public boolean validateTrueFalse(String value1)
    {
		boolean valid = false;
		String flag = value1.toLowerCase();

		if ("true".equals(flag) || "false".equals(flag))
		{
			valid = true;
		}
		return valid;
	}


    public String makeDialableNumber(String digits)
    {
		String dialableNumber = null;

		if (digits != null && !"".equals(digits))
		{
			dialableNumber = "+" + digits;
/*
			String cononicalNumber = formatCanonicalNumber(convertAlpha(digits));

			if (cononicalNumber != null && !"".equals(cononicalNumber))
			{
				dialableNumber = formatDialableNumber(cononicalNumber);
			}

			Log.info( "makeDialableNumber " + digits + "=>" + dialableNumber);
*/
		}

		return dialableNumber;
	}

	private String convertAlpha(String input)
	{
		int inputlength = input.length();
		input = input.toLowerCase();
		String phonenumber = "";

		for (int i = 0; i < inputlength; i++) {
			int character = input.charAt(i);

			switch(character) {
				case '+': phonenumber+="+";break;
				case '*': phonenumber+="*";break;
				case '#': phonenumber+="#";break;
				case '0': phonenumber+="0";break;
				case '1': phonenumber+="1";break;
				case '2': phonenumber+="2";break;
				case '3': phonenumber+="3";break;
				case '4': phonenumber+="4";break;
				case '5': phonenumber+="5";break;
				case '6': phonenumber+="6";break;
				case '7': phonenumber+="7";break;
				case '8': phonenumber+="8";break;
				case '9': phonenumber+="9";break;
				case  'a': case 'b': case 'c': phonenumber+="2";break;
				case  'd': case 'e': case 'f': phonenumber+="3";break;
				case  'g': case 'h': case 'i': phonenumber+="4";break;
				case  'j': case 'k': case 'l': phonenumber+="5";break;
				case  'm': case 'n': case 'o': phonenumber+="6";break;
				case  'p': case 'q': case 'r': case 's': phonenumber+="7";break;
				case  't': case 'u': case 'v': phonenumber+="8";break;
				case  'w': case 'x': case 'y': case 'z': phonenumber+="9";break;
		   }
		}

		return (phonenumber);
	}

	public boolean isComponent(JID jid) {
		final RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();

		if (routingTable != null)
		{
			return routingTable.hasComponentRoute(jid);
		}
		return false;
	}

	public void setRefreshCacheInterval()
	{
		Log.info( "setRefreshCacheInterval ");

		try {


		}
		catch (Exception e)
		{
			Log.error("setRefreshCacheInterval " + e);
		}
	}
//-------------------------------------------------------
//
//
//
//-------------------------------------------------------


	public void getUserProfiles()
	{
		Config config = Config.getInstance();

		try
		{
			Collection<User> users = userManager.getUsers();

            Iterator it = users.iterator();

			while( it.hasNext() )
			{
               	User user = (User)it.next();
				String userId = user.getUsername();

				Log.info( "getUserProfiles - user profile " + user.getUsername());

				OpenlinkUser openlinkUser = new OpenlinkUser();
				openlinkUser.setUserName(user.getName());
				openlinkUser.setUserId(userId);
				openlinkUser.setUserNo(userId);
				openlinkUser.setSiteName(getName());
				openlinkUser.setSiteID(1);
				openlinkUser.setHandsetNo("1");

				String deskPhone = null;
				String mobilePhone = null;

				Element vCard = VCardManager.getInstance().getVCard(userId);

				if (vCard != null)
				{
					Log.info( "getUserProfiles - vcard for " + userId + "\n" + vCard.asXML());

					deskPhone = getTelVoiceNumber(vCard, "WORK", "VOICE");
					mobilePhone = getTelVoiceNumber(vCard, "WORK", "CELL");

				}

				if (deskPhone != null && deskPhone != "")
				{
					openlinkUser.setPersonalDDI(deskPhone);
				}

				if (mobilePhone != null && mobilePhone != "")
				{
					openlinkUser.setCallback(mobilePhone);
				}

				//openlinkUser.setDeviceType("openlink");

				if (userProfiles.containsKey(userId) == false)
				{
					openlinkUser.setDefault("true");
					userProfiles.put(userId, openlinkUser);
				}

				openlinkUserTable.put(openlinkUser.getUserId(), openlinkUser);

				// find user SIP personal telephone no

				ProxyCredentials sip = config.getProxyCredentialsByUser(userId);

				if (sip != null)
				{
					openlinkUser.setPersonalDDI(sip.getUserName());
					String sipUrl = "sip:" + sip.getUserName() + "@" + sip.getHost();
					createInterest(openlinkUser, "D", sipUrl, sip.getUserDisplay(), true, sipUrl);

				} else {

					String sipUrl = "sip:" + openlinkUser.getUserId() + "@" + getDomain();
					createInterest(openlinkUser, "D", sipUrl, user.getName(), true, sipUrl);
				}

				Roster roster = rosterManager.getRoster(user.getUsername());

				List<RosterItem> rosterItems = new ArrayList<RosterItem>(roster.getRosterItems());
				Collections.sort(rosterItems, new RosterItemComparator());

				for (RosterItem rosterItem : rosterItems)
				{
					String interestNode = rosterItem.getJid().getNode();

					Log.info( "getUserProfiles - interest " + interestNode);

					// shared groups interests

					if (rosterItem.isShared())
					{
						for (Group group: rosterItem.getSharedGroups())
						{
							ProxyCredentials sip2 = config.getProxyCredentialsByUser(group.getName());

							if (sip2 != null)
							{
								String sipUrl = "sip:" + sip2.getUserName() + "@" + sip2.getHost();
								createInterest(openlinkUser, "D", sipUrl, group.getDescription(), false, sipUrl);

							} else {

								String sipUrl = "sip:" + group.getName() + "@" + getDomain();
								createInterest(openlinkUser, "D", sipUrl, group.getDescription(), false, sipUrl);
							}
						}

					} else {

						createInterest(openlinkUser, "L", rosterItem.getJid().toString(), rosterItem.getNickname(), false, rosterItem.getJid().toString());
					}

					// speed dial

					String deskPhone2 = null;
					String mobilePhone2 = null;

					Element vCard2 = VCardManager.getInstance().getVCard(interestNode);

					if (vCard2 != null)
					{
						Log.info( "getUserProfiles - interest for " + interestNode + "\n" + vCard2.asXML());

						deskPhone2 = getTelVoiceNumber(vCard2, "WORK", "VOICE");
						mobilePhone2 = getTelVoiceNumber(vCard2, "WORK", "CELL");


					}
				}
			}
		}
		catch (Exception e)
		{
	        Log.error( "" +  "Error in getProfiles " + e);
        	e.printStackTrace();
		}
	}

	private OpenlinkInterest createInterest(OpenlinkUser openlinkUser, String interestType, String interestNode, String interestLabel, boolean defaultInterest, String interestValue)
	{
		OpenlinkInterest openlinkInterest = null;

		if (openlinkInterests.containsKey(interestNode))
		{
			openlinkInterest = openlinkInterests.get(interestNode);

		} else {

			openlinkInterest = new OpenlinkInterest();
		}

		openlinkInterest.setInterestType(interestType);
		openlinkInterest.setSiteName(getName());
		openlinkInterest.setInterestLabel(interestLabel);
		openlinkInterest.setInterestValue(interestValue);

		openlinkInterests.put(interestNode, openlinkInterest);

		if (defaultInterest)
		{
			openlinkUser.setDefaultInterest(openlinkInterest);
		}

		OpenlinkUserInterest openlinkUserInterest = openlinkInterest.addUserInterest(openlinkUser, defaultInterest ? "true" : "false");
		openlinkUser.addInterest(openlinkInterest);

		openlinkUserInterests.put(interestNode + openlinkUser.getUserNo(), openlinkUserInterest);

		createPubsubNode(openlinkInterest.getInterestId() + openlinkUser.getUserNo());
		getInterestSubscriptions(openlinkInterest, openlinkUser.getUserNo());

		return openlinkInterest;
	}

	private String getUserNo(String email, String userId)
	{
		if (email != null)
		{
			int pos = email.indexOf("@");

			if ( pos > -1)
			{
				return email.substring(0, pos);

			} else {

				return email;
			}

		} else return userId;
	}

	private String getTelVoiceNumber(Element vCard, String work, String voice)
	{
		String telVoiceNumber = null;

		for ( Iterator i = vCard.elementIterator( "TEL" ); i.hasNext(); )
		{
			Element tel = (Element) i.next();
			//Log.info( "getTelVoiceNumber - tel " + tel.asXML());

			if (tel.element(work) != null && tel.element(voice) != null)
			{
				Element number = tel.element("NUMBER");

				if (number != null)
				{
					//Log.info( "getTelVoiceNumber - number " + number.getText());
					telVoiceNumber = number.getText();
					break;
				}
			}
		}

		return telVoiceNumber;
	}

	private void createPubsubNode(String interestNode)
	{
		Log.info("createPubsubNode - " + interestNode);

		String domain = getDomain();

		IQ iq1 = new IQ(IQ.Type.set);
		iq1.setFrom(getName() + "." + domain);
		iq1.setTo("pubsub." + domain);
		Element pubsub1 = iq1.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
		Element create = pubsub1.addElement("create").addAttribute("node", interestNode);

		Element configure = pubsub1.addElement("configure");
		Element x = configure.addElement("x", "jabber:x:data").addAttribute("type", "submit");

		Element field1 = x.addElement("field");
		field1.addAttribute("var", "FORM_TYPE");
		field1.addAttribute("type", "hidden");
		field1.addElement("value").setText("http://jabber.org/protocol/pubsub#node_config");

		//Element field2 = x.addElement("field");
		//field2.addAttribute("var", "pubsub#persist_items");
		//field2.addElement("value").setText("1");

		Element field3 = x.addElement("field");
		field3.addAttribute("var", "pubsub#max_items");
		field3.addElement("value").setText("1");

		Log.info("createPubsubNode " + iq1.toString());
		sendPacket(iq1);
	}

	public void getInterestSubscriptions(OpenlinkInterest openlinkInterest, String userNo)
	{
		String interestNode = openlinkInterest.getInterestId() + userNo;
		String domain = getDomain();

		Log.info("getInterestSubscriptions  - " + interestNode);

		IQ iq2 = new IQ(IQ.Type.get);
		iq2.setFrom(getName() + "." + domain);
		iq2.setTo("pubsub." + domain);
		Element pubsub2 = iq2.setChildElement("pubsub", "http://jabber.org/protocol/pubsub#owner");
		Element subscriptions = pubsub2.addElement("subscriptions").addAttribute("node", interestNode);

		Log.info("subscriptions " + iq2.toString());
		sendPacket(iq2);
	}

    public void setSubscriberDetails(JID jid, OpenlinkSubscriber openlinkSubscriber)
    {
		if (userManager.isRegisteredUser(jid.getNode()))
		{
			User user = null;

			try {
				user = userManager.getUser(jid.getNode());
			}
			catch(Exception e) { }

			if (user != null)
			{
				openlinkSubscriber.setOnline(presenceManager.isAvailable(user));
				openlinkSubscriber.setName(user.getName());
				openlinkSubscriber.setJID(jid); 				// we need the full JID including resource to get session object
			}
		}
	}

    public void callEventNotification(CallEvent callEvent)
    {
 		Log.info( "VoiceBridge callEventNotification " + callEvent.toString());
    }

    class RosterItemComparator implements Comparator<RosterItem>
    {
        public int compare(RosterItem itemA, RosterItem itemB)
        {
            return itemA.getJid().toBareJID().compareTo(itemB.getJid().toBareJID());
        }
    }
}

