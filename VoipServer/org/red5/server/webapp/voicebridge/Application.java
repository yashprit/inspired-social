package org.red5.server.webapp.voicebridge;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.component.Component;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.*;

import java.text.ParseException;
import java.net.*;
import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.sun.voip.server.*;
import com.sun.voip.client.*;
import com.sun.voip.*;

import org.jivesoftware.openfire.Connection;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.StreamID;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.net.VirtualConnection;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.auth.AuthToken;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.SessionPacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.database.SequenceManager;

import org.jivesoftware.util.*;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.muc.spi.*;
import org.jivesoftware.openfire.forms.spi.*;
import org.jivesoftware.openfire.forms.*;
import org.jivesoftware.openfire.component.InternalComponentManager;

import org.xmpp.packet.*;

import org.dom4j.*;

import com.ifsoft.cti.OpenlinkComponent;

import com.jcumulus.server.rtmfp.ServerPipelineFactory;
import com.jcumulus.server.rtmfp.Sessions;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import org.apache.log4j.Logger;


public class Application implements CallEventListener  {

	private Logger Log = Logger.getLogger(getClass().getName());

 	private String version = "0.0.0.1";
	private Config config;
	private String domainName;
	private static Site site;
	private static Map< String, BridgeParticipant > bridgeParticipants 	= new ConcurrentHashMap< String, BridgeParticipant >();

    private static Map<String, String> pubsubNodes = new ConcurrentHashMap<String, String>();

	public static OpenlinkComponent component;

    private static ConnectionlessBootstrap bootstrap = null;
    public static Channel channel = null;
    private static Sessions sessions;


    // ------------------------------------------------------------------------
    //
    // Overide
    //
    // ------------------------------------------------------------------------


    public boolean appStart(OpenlinkComponent component)
    {
		try{
			this.component = component;

			Log.info(String.format("Voip Engine version %s", version));
			domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());

			Site site = new Site();
			site.setSiteID(SequenceManager.nextID(site));
			site.setName(domainName);

			InetAddress inetAddress = InetAddress.getByName(domainName);

			site.setPrivateHost(JiveGlobals.getProperty("voicebridge.default.private.host", inetAddress.getHostAddress()));
			site.setPublicHost(JiveGlobals.getProperty("voicebridge.default.public.host", inetAddress.getHostAddress()));
			site.setDefaultProxy(JiveGlobals.getProperty("voicebridge.default.proxy.name",null));
			site.setDefaultExten(JiveGlobals.getProperty("voicebridge.default.conf.exten", "default"));

			config = Config.getInstance();
			config.initialise(site);

			String appPath = System.getProperty("user.dir");
			String logDir = appPath + File.separator + ".." + File.separator + "logs" + File.separator;

			Properties properties = new Properties();

			System.setProperty("com.sun.voip.server.FIRST_RTP_PORT", JiveGlobals.getProperty("voicebridge.rtp.start.port", "3200"));
			System.setProperty("com.sun.voip.server.LAST_RTP_PORT", JiveGlobals.getProperty("voicebridge.rtp.end.port", "3299"));
			System.setProperty("com.sun.voip.server.Bridge.logDirectory", logDir);
			System.setProperty("com.sun.voip.server.BRIDGE_LOG", "voicebridge.log");
			System.setProperty("com.sun.voip.server.LOGLEVEL", JiveGlobals.getProperty("voicebridge.server.log.level", "1"));
			System.setProperty("com.sun.voip.server.PUBLIC_IP_ADDRESS", config.getPublicHost());
			System.setProperty("gov.nist.jainsip.stack.enableUDP", JiveGlobals.getProperty("voicebridge.sip.port", "5060"));

			properties.setProperty("javax.sip.STACK_NAME", "JAIN SIP 1.1");
			properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "on");
			properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", JiveGlobals.getProperty("voicebridge.sip.log.level", "1"));
			properties.setProperty("gov.nist.javax.sip.SERVER_LOG", logDir + "sip_server.log");
			properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", logDir + "sip_debug.log");
			properties.setProperty("javax.sip.IP_ADDRESS",config.getPrivateHost());

			Bridge.setPublicHost(config.getPublicHost());
			Bridge.setPrivateHost(config.getPrivateHost());
			Bridge.setBridgeLocation("VB" + site.getSiteID());

			com.sun.voip.Logger.init(logDir + "voicebridge.log", false);

			Log.info("Starting jVoiceBridge....");

			new SipServer(config, properties);

			Log.info("Starting jCumulus.....");

			sessions = new Sessions();
			ExecutorService executorservice = Executors.newCachedThreadPool();
			NioDatagramChannelFactory niodatagramchannelfactory = new NioDatagramChannelFactory(executorservice);
			bootstrap = new ConnectionlessBootstrap(niodatagramchannelfactory);
			OrderedMemoryAwareThreadPoolExecutor orderedmemoryawarethreadpoolexecutor = new OrderedMemoryAwareThreadPoolExecutor(10, 0x100000L, 0x40000000L, 100L, TimeUnit.MILLISECONDS, Executors.defaultThreadFactory());

			bootstrap.setPipelineFactory(new ServerPipelineFactory(sessions, orderedmemoryawarethreadpoolexecutor));
			bootstrap.setOption("reuseAddress", Boolean.valueOf(true));
			bootstrap.setOption("sendBufferSize", Integer.valueOf(1215));
			bootstrap.setOption("receiveBufferSize", Integer.valueOf(2048));
			bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(2048));

			InetSocketAddress inetsocketaddress = new InetSocketAddress(JiveGlobals.getIntProperty("voicebridge.rtmfp.port", 1935));

			Log.info("Listening on " + inetsocketaddress.getPort() + " port");

			channel = bootstrap.bind(inetsocketaddress);

		} catch (Exception e) {

			e.printStackTrace();
		}
        return true;
    }


    public void appStop()
    {
        Log.info( "jVoiceBridge stopping...");

		CallHandler.shutdown();
		config.terminate();

		ArrayList conferenceList = ConferenceManager.getConferenceList();

        for (int i = 0; i < conferenceList.size(); i++)
        {
			try {
				ConferenceManager conferenceManager = (ConferenceManager) conferenceList.get(i);
				ConferenceManager.endConference(conferenceManager.getId());

			} catch (Exception e) {}
		}

		Log.info("jCumulus stopping...");

		channel.close();
		bootstrap.releaseExternalResources();
    }



    // ------------------------------------------------------------------------
    //
    // VoiceBridge
    //
    // ------------------------------------------------------------------------

	public void createRoom(String roomName, boolean tempRoom)
	{
		Log.info( "createRoom " + roomName);

		try
		{
			if (XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").hasChatRoom(roomName) == false)
			{
				MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").getChatRoom(roomName);

				if (room == null)
				{
					room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").getChatRoom(roomName, new JID("admin@"+domainName));

					if (room != null)
					{
						configureRoom(room, tempRoom);
					}
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}


	private void configureRoom(MUCRoom room, boolean tempRoom)
	{
		Log.info( "configureRoom " + room.getID());

		FormField field;
		XDataFormImpl dataForm = new XDataFormImpl(DataForm.TYPE_SUBMIT);

/*
		field = new XFormFieldImpl("muc#roomconfig_roomdesc");
		field.setType(FormField.TYPE_TEXT_SINGLE);

		String desc = room.getDescription();
		desc = desc == null ? "" : desc;
		field.addValue(desc);
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_roomname");
		field.setType(FormField.TYPE_TEXT_SINGLE);
		field.addValue(room.getName());
		dataForm.addField(field);

		field = new XFormFieldImpl("FORM_TYPE");
		field.setType(FormField.TYPE_HIDDEN);
		field.addValue("http://jabber.org/protocol/muc#roomconfig");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_changesubject");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_maxusers");
		field.addValue("30");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_presencebroadcast");
		field.addValue("moderator");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_publicroom");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_moderatedroom");
		field.addValue("0");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_membersonly");
		field.addValue("0");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_allowinvites");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_passwordprotectedroom");
		field.addValue("0");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_whois");
		field.addValue("moderator");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_enablelogging");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("x-muc#roomconfig_canchangenick");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("x-muc#roomconfig_registration");
		field.addValue("1");
		dataForm.addField(field);

		// Keep the existing list of admins
		field = new XFormFieldImpl("muc#roomconfig_roomadmins");
		for (String jid : room.getAdmins()) {
			field.addValue(jid);
		}
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_roomowners");
		field.addValue("admin@"+domainName);
		dataForm.addField(field);
*/

		if (tempRoom == false)
		{
			field = new XFormFieldImpl("muc#roomconfig_persistentroom");
			field.addValue(tempRoom ? "0": "1");
			dataForm.addField(field);
		}

		// Create an IQ packet and set the dataform as the main fragment
		IQ iq = new IQ(IQ.Type.set);
		Element element = iq.setChildElement("query", "http://jabber.org/protocol/muc#owner");
		element.add(dataForm.asXMLElement());

		try
		{
			// Send the IQ packet that will modify the room's configuration
			room.getIQOwnerHandler().handleIQ(iq, room.getRole());

		} catch (Exception e) {
			Log.error("configureRoom exception " + e);
		}
	}


	private void connectToMUC(final BridgeParticipant bp)
	{
		final JID jid = bp.participantJID;
		final CallParticipant cp = bp.callParticipant;

    	Log.info("VoiceBridge connectToMUC " + jid + " " + cp.getConferenceId() + " " + cp.getCallId() + " " + cp.getProtocol());

		try {

			final String conferenceId = cp.getConferenceId();

			createRoom(conferenceId, true);

			IQ iq = new IQ(IQ.Type.set);
			iq.setFrom(conferenceId + "@" + component.getName() + "." + domainName);
			iq.setTo(jid.toString());

			Element openlink = iq.setChildElement("collaboration", "http://inspiredfutures.co.uk/protocol/collaboration");

			Element invite = openlink.addElement("invite");
			invite.addAttribute("from", bp.userJID.toString());
			invite.addAttribute("room", conferenceId + "@conference." + domainName);

			if ("NS".equals(cp.getProtocol()) == false)
				invite.addElement("audio");

			if (cp.getConferenceDisplayName() != null)
			{
				invite.addElement("reason").setText(cp.getConferenceDisplayName());
			}

			sendPacket(iq);

			Log.info("VoiceBridge connectToMUC outgoing message \n" + iq);

		} catch (Exception e) {

			Log.error("connectToMUC " + e);
		}
	}


	private void disconnectFromMUC(BridgeParticipant bp)
	{
		JID jid = bp.participantJID;
		CallParticipant cp = bp.callParticipant;

    	Log.info("VoiceBridge disconnectFromMUC " + jid + " " + cp.getConferenceId() + " " + cp.getCallId());

		String conferenceId = cp.getConferenceId();

		// <presence type="unavailable" to="1111@conference.btg199251/Kimme"/>

		try {

			Presence presence = new Presence(Presence.Type.unavailable);
			presence.setTo(conferenceId + "@conference." + domainName + "/" + jid.getNode());
			presence.addChildElement("x", "http://jabber.org/protocol/muc");
			presence.setFrom(jid);

			sendPacket(presence);

			Log.info("VoiceBridge disconnectFromMUC outgoing presence \n" + presence);

		} catch (Exception e) {

			Log.error("disconnectFromMUC " + e);
		}

	}

    public String manageCallParticipant(JID userJID, String uid, String parameter, String value)
    {
		String response = null;

		try {
			Log.info("VoiceBridge manageParticipant " + userJID + " " + uid + " " + parameter + " " + value);

			if (bridgeParticipants.containsKey(uid) == false)
			{
				CallParticipant cp = new CallParticipant();
				cp.setCallId(uid);

				BridgeParticipant bp = new BridgeParticipant();
				bp.userJID = userJID;
				bp.callParticipant = cp;

				bridgeParticipants.put(uid, bp);
			}

			response = parseCallParameters(parameter, value, bridgeParticipants.get(uid), uid);

			if ("CancelCall".equalsIgnoreCase(parameter))
			{
				try {
					BridgeParticipant bp = bridgeParticipants.remove(uid);
					CallParticipant cp = bp.callParticipant;

					if (bp.participantJID != null)
					{
						disconnectFromMUC(bp);
					}

					bp = null;

				} catch (Exception e) {

					response = e.toString();
				}
			}

		} catch (Exception e) {

			response = (e.toString());
		}

		return response;
	}


	public void handlePostBridge(List<String> uids)
	{
		for (String uid : uids)
		{
    		Log.info("VoiceBridge handlePostBridge " + uid);

			BridgeParticipant bp = bridgeParticipants.get(uid);

			if (bp.participantJID != null)
			{
				CallParticipant cp = bp.callParticipant;

			}
		}
	}


    public String manageVoiceBridge(String parameter, String value)
    {
		String response = null;
    	Log.info("VoiceBridge manageConference");

		try {

			parseBridgeParameters(parameter, value);

		} catch (Exception e) {

			response = (e.toString());
		}
		return response;
	}


    private String makeOutgoingCall(BridgeParticipant bp, String uid)
    {
		CallParticipant cp = bp.callParticipant;

		String response = null;
    	Log.info("VoiceBridge makeOutgoingCall " + uid + " " + cp.getCallId() + " " + bp.participantJID);

		try {

			response = validateAndAdjustParameters(bp);

			if (response == null)
			{
				if (cp.getSecondPartyNumber() == null)
				{
					if (cp.getPhoneNumber() != null)
					{
						OutgoingCallHandler outgoingCallHandler = new OutgoingCallHandler(this, cp);
						outgoingCallHandler.start();

						bp.callHandler = outgoingCallHandler;
					}

					if (bp.participantJID != null)
					{
						connectToMUC(bp);
					}

				} else {

					TwoPartyCallHandler twoPartyCallHandler = new TwoPartyCallHandler(this, cp);
					twoPartyCallHandler.start();

					bp.callHandler = twoPartyCallHandler;
				}

			}

		} catch (Exception e) {

			response = (e.toString());
		}

		return response;
	}

    private String migrateCall(BridgeParticipant bp)
    {
		CallParticipant cp = bp.callParticipant;
		String response = null;
    	Log.info("VoiceBridge migrateCall");

		try {

			response = validateAndAdjustParameters(bp);

			if (response == null)
			{
				if (cp.migrateCall())
				{
					new CallMigrator(this, cp).start();

				} else {

					response = ("Call participant is not configured for migration");
				}
			}

		} catch (Exception e) {

			response = (e.toString());
		}

		return response;
	}


    private String parseCallParameters(String parameter, String value, BridgeParticipant bp, String uid)
    {
		CallParticipant cp = bp.callParticipant;
		String response = null;

		if ("SetJID".equalsIgnoreCase(parameter))
		{
			try {
				bp.participantJID = new JID(value);

			} catch (Exception e) {
				response = e.toString();
			}

			return response;
		}

		if ("Set2ndPartyJID".equalsIgnoreCase(parameter))
		{
			try {
				bp.secondPartyJID = new JID(value);

			} catch (Exception e) {
				response = e.toString();
			}

			return response;
		}

		if ("SetRtmpVideoStream".equalsIgnoreCase(parameter))
		{
			try {
				cp.setRtmpVideoStream(value);

			} catch (Exception e) {
				response = e.toString();
			}

			return response;
		}

		if ("SetEmailAddress".equalsIgnoreCase(parameter))
		{
			try {
				cp.setEmailAddress(value);

			} catch (Exception e) {
				response = e.toString();
			}

			return response;
		}

		if ("MakeCall".equalsIgnoreCase(parameter))
		{
			try {
				response = makeOutgoingCall(bp, uid);
			} catch (Exception e) {
				Log.error(e.toString());
			}
			return response;
		}

		if ("migrateCall".equalsIgnoreCase(parameter))
		{
			try {
				migrateCall(bp);
			} catch (Exception e) {
				response = (e.toString());
			}
			return response;
		}

		if ("cancelCall".equalsIgnoreCase(parameter))
		{
			CallHandler.hangup(cp.getCallId(), "User requested call termination");
			return response;
		}

		if ("sendDtmfKey".equalsIgnoreCase(parameter))
		{
			try {
				CallHandler callHandler = CallHandler.findCall(cp.getCallId());
				callHandler.getMemberSender().setDtmfKeyToSend(value);;

			} catch (NoSuchElementException e) {
				response = ("Invalid callId specified:  " + cp.getCallId());
			}

			return response;
		}

		if ("sendDtmf".equalsIgnoreCase(parameter))
		{
			try {
				CallHandler callHandler = CallHandler.findCall(cp.getCallId());
				callHandler.dtmfKeys(value);

			} catch (NoSuchElementException e) {
				response = ("Invalid callId specified:  " + cp.getCallId());
			}

			return response;
		}


		if ("transferCall".equalsIgnoreCase(parameter))
		{
			try {
				IncomingCallHandler.transferCall(cp.getCallId(), value);

			} catch (NoSuchElementException e) {
				response = ("Invalid callId specified:  " + value);

			} catch (Exception e) {
				response = ("Internal error:  " + e);

			}

			return response;
		}


		if ("conferenceJoinTreatment".equalsIgnoreCase(parameter))
		{
			cp.setConferenceJoinTreatment(value);
			return response;
		}

		if ("conferenceLeaveTreatment".equalsIgnoreCase(parameter))
		{
			cp.setConferenceLeaveTreatment(value);
			return response;
		}

		if ("callAnsweredTreatment".equalsIgnoreCase(parameter))
		{
			 cp.setCallAnsweredTreatment(value);
			return response;
		}

		if ("callanswertimeout".equalsIgnoreCase(parameter))
		{
			try {
				cp.setCallAnswerTimeout(getInteger(value));
			} catch (Exception e) {
				response = ("callAnswerTimeout " + value + " is not numeric" );
			}
			return response;
		}

		if ("calltimeout".equalsIgnoreCase(parameter))
		{
			try {
				cp.setCallTimeout(getInteger(value) * 1000);
			} catch (Exception e) {
				response = ("callTimeout " + value + " is not numeric" );
			}
			return response;
		}

		if ("callendtreatment".equalsIgnoreCase(parameter))
		{
			cp.setCallEndTreatment(value);
			return response;
		}

		if ("callestablishedtreatment".equalsIgnoreCase(parameter))
		{
			cp.setCallEstablishedTreatment(value);
			return response;
		}

		if ("callid".equalsIgnoreCase(parameter))
		{
			cp.setCallId(value);
			return response;
		}

		if ("SetConference".equalsIgnoreCase(parameter))
		{
			try {
				String[] tokens = value.split(":");

				cp.setConferenceId(tokens[0].trim());

				if (tokens.length > 1) {
					cp.setConferenceDisplayName(tokens[1]);
				}

				if (tokens.length > 2) {
					cp.setMediaPreference(tokens[2]);
				}

			} catch (Exception e) {
				response = ("conferenceId " + value + " is invalid" );
			}
			return response;
		}

		if ("displayname".equalsIgnoreCase(parameter))
		{
			cp.setDisplayName(value);
			return response;
		}

		if ("distributedbridge".equalsIgnoreCase(parameter))
		{
			try {
				cp.setDistributedBridge(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("donotrecord".equalsIgnoreCase(parameter))
		{
			try {
				cp.setDoNotRecord(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("dtmfdetection".equalsIgnoreCase(parameter))
		{
			try {
				cp.setDtmfDetection(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("dtmfsuppression".equalsIgnoreCase(parameter))
		{
			try {
				cp.setDtmfSuppression(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("forwarddatafrom".equalsIgnoreCase(parameter))
		{
			cp.setForwardingCallId(value);
			return response;
		}

		if ("ignoretelephoneevents".equalsIgnoreCase(parameter))
		{
			try {
				cp.setIgnoreTelephoneEvents(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("inputtreatment".equalsIgnoreCase(parameter))
		{
			cp.setInputTreatment(value);
			return response;
		}

		if ("encryptkey".equalsIgnoreCase(parameter))
		{
			cp.setEncryptionKey(value);
			return response;
		}

		if ("encryptalgorithm".equalsIgnoreCase(parameter))
		{
			cp.setEncryptionAlgorithm(value);
			return response;
		}

		if ("firstConferenceMemberTreatment".equalsIgnoreCase(parameter))
		{
			cp.setFirstConferenceMemberTreatment(value);
			return response;
		}

		if ("handlesessionprogress".equalsIgnoreCase(parameter))
		{
			try {
				cp.setHandleSessionProgress(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("joinconfirmationkey".equalsIgnoreCase(parameter))
		{
			try {
				MemberReceiver.setJoinConfirmationKey(value);

			} catch (Exception e) {
				response = (e.toString());
			}
			return response;
		}

		if ("joinconfirmationtimeout".equalsIgnoreCase(parameter))
		{
			try {
				cp.setJoinConfirmationTimeout(getInteger(value));
			} catch (Exception e) {
				response = ("callAnswerTimeout " + value + " is not numeric" );
			}
			return response;
		}

		if ("mediapreference".equalsIgnoreCase(parameter))
		{
			cp.setMediaPreference(value);
			return response;
		}

		if ("migrate".equalsIgnoreCase(parameter))
		{
            String callId = getFirstString(value);
            cp.setCallId(callId);

			/*
			 * The second party number may be a sip address
			 * with colons.  So we treat everything after the
			 * first colon as the second party number.
			 */

			int ix;

			if ((ix = value.indexOf(":")) < 0) {

				response = ("secondPartyNumber must be specified:  " + value);
				return response;
			}

            String secondPartyNumber = value.substring(ix + 1);

            if (secondPartyNumber == null)
            {
                response = ("secondPartyNumber must be specified:  " + value);
				return response;
            }

			try {
				cp.setSecondPartyNumber(secondPartyNumber);
				cp.setMigrateCall(true);

			} catch (Exception e) {
				response = (e.toString());
			}

            return response;
		}

		if ("MuteCall".equalsIgnoreCase(parameter))
		{
			try {
				cp.setMuted(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("mutewhispergroup".equalsIgnoreCase(parameter))
		{
			try {
				cp.setMuteWhisperGroup(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("muteconference".equalsIgnoreCase(parameter))
		{
			try {
				cp.setConferenceMuted(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("name".equalsIgnoreCase(parameter))
		{
			cp.setName(value);
			return response;
		}

		if ("SetPhoneNo".equalsIgnoreCase(parameter))
		{
			cp.setPhoneNumber(value);
			return response;
		}

		if ("phonenumberlocation".equalsIgnoreCase(parameter))
		{
			cp.setPhoneNumberLocation(value);
		}

		if ("protocol".equalsIgnoreCase(parameter))
		{
			if (value.equalsIgnoreCase("SIP") == false && value.equalsIgnoreCase("NS") == false &&
				value.equalsIgnoreCase("RTMP") == false && value.equalsIgnoreCase("RTMFP") == false &&
				value.equalsIgnoreCase("JINGLE") == false)
			{
				response = ("Invalid protocol:  " + value);
				return response;
			}

			cp.setProtocol(value);
			return response;
		}

		if ("recorder".equalsIgnoreCase(parameter))
		{
			try {
				cp.setRecorder(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}


		if ("recorddirectory".equalsIgnoreCase(parameter))
		{
			cp.setRecordDirectory(value);
			return response;
		}

		if ("remotecallid".equalsIgnoreCase(parameter))
		{
			cp.setRemoteCallId(value);
			return response;
		}

		if ("voiceDetectionWhileMuted".equalsIgnoreCase(parameter))
		{
			try {
				cp.setVoiceDetectionWhileMuted(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("useConferenceReceiverThread".equalsIgnoreCase(parameter))
		{
			try {
				cp.setUseConferenceReceiverThread(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("voiceDetection".equalsIgnoreCase(parameter))
		{
			try {
				cp.setVoiceDetection(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("whisperGroup".equalsIgnoreCase(parameter))
		{
			cp.setWhisperGroupId(value);
			return response;
		}
		if ("voipGateway".equalsIgnoreCase(parameter))
		{
			cp.setVoIPGateway(value);
			return response;
		}

		if ("secondPartyCallId".equalsIgnoreCase(parameter))
		{
			cp.setSecondPartyCallId(value);
			return response;
		}
		if ("sipProxy".equalsIgnoreCase(parameter))
		{
			cp.setSipProxy(value);
			return response;
		}

		if ("secondPartyCallEndTreatment".equalsIgnoreCase(parameter))
		{
			cp.setSecondPartyCallEndTreatment(value);
			return response;
		}

		if ("secondPartyName".equalsIgnoreCase(parameter))
		{
			cp.setSecondPartyName(value);
			return response;
		}

		if ("Set2ndPartyPhoneNo".equalsIgnoreCase(parameter))
		{
			cp.setSecondPartyNumber(value);
			return response;
		}

		if ("secondPartyTreatment".equalsIgnoreCase(parameter))
		{
			cp.setSecondPartyTreatment(value);
			return response;
		}

		if ("secondpartyVoiceDetection".equalsIgnoreCase(parameter))
		{
			try {
				cp.setSecondPartyVoiceDetection(stringToBoolean(value));
			} catch (ParseException e) {
				response = (e.toString());
			}
			return response;
		}

		if ("secondPartyTimeout".equalsIgnoreCase(parameter))
		{
			try {
				cp.setSecondPartyTimeout(getInteger(value));
			} catch (Exception e) {
				response = ("setSecondPartyTimeout " + value + " is not numeric" );
			}
			return response;
		}

		if ("RtmpSendStream".equalsIgnoreCase(parameter))
		{
			cp.setRtmpSendStream(value);
			return response;
		}

		if ("RtmpRecieveStream".equalsIgnoreCase(parameter))
		{
			cp.setRtmpRecieveStream(value);
			return response;
		}


		return response;
    }

    private String getString(String value)
    {
		int n;

		if ((n = value.lastIndexOf(":")) > 0) {
			value = value.substring(0, n);
		}

		return value;
    }

    private double[] getVolume(String value) throws ParseException
    {
		String v = new String(value);

		int n;

			if ((n = v.indexOf(":volume=")) < 0) {
			return null;
		}

		v = v.substring(n + 8);

		String[] tokens = v.split(":");

		double[] volume = new double[tokens.length];

		for (int i = 0; i < volume.length; i++)
		{
			try {
				volume[i] = Double.parseDouble(tokens[i]);

			} catch (NumberFormatException e) {

			throw new ParseException("Invalid floating point value: "	+ tokens[i], 0);
			}
		}

		return volume;
    }

    private String getTreatment(String value)
    {
		String v = new String(value);

		int n;

		if ((n = v.indexOf(":volume=")) >= 0)
		{
			v = v.substring(0, n);
		}

		if ((n = v.lastIndexOf(":")) < 0) {
			/*
			 * There's no ":", so the whole string is the treatment
			 */
			return v;
		}

		String s = v.substring(0, n);

		if (s.equalsIgnoreCase("f") || s.equalsIgnoreCase("file")
			   || s.equalsIgnoreCase("d") || s.equalsIgnoreCase("dtmf")
			   || s.equalsIgnoreCase("t") || s.equalsIgnoreCase("tts")) {

			/*
			 * The only ":" is preceded by the type of treatment.
			 * The whole string is the treatment.
			 */
			return v;
		}

		/*
		 * The treatment is the string up to the last ":".
		 */
		return s;
    }

    private boolean getBoolean(String value) throws ParseException
    {
		int n;

		if ((n = value.indexOf(":")) > 0) {
			value = value.substring(0, n);
		}

		return stringToBoolean(value);
    }


    private boolean stringToBoolean(String value) throws ParseException
    {
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t")) {
			return true;
		}

		if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("F")) {
			return false;
		}

        throw new ParseException("Invalid boolean value, must be true or false:  " + value, 0);
    }

    private int getInteger(String value) throws ParseException
    {
		int n;

		if ((n = value.indexOf(":")) > 0) {
			value = value.substring(0, n);
		}

		int i = 0;

		try {
				i = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid integer value: " + value, 0);
		}

		return i;
    }

    private String getQualifierString(String value)
    {
		int n;

		if (value == null) {
			return null;
		}

		if ((n = value.lastIndexOf(":")) >= 0) {
			return value.substring(n+1);
		}

		return null;
    }

    private double getDouble(String value) throws ParseException
    {
		int n;

		if ((n = value.indexOf(":")) > 0) {
			value = value.substring(0, n);
		}

		double f = 0.0;

		try {
				f = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid double value: " + value, 0);
		}

		return f;
    }

    private String getFirstString(String value)
    {
		int ix;

		if ((ix = value.indexOf(":")) < 0) {
			return value;
		}

		return value.substring(0, ix);
    }

    private String validateAndAdjustParameters(BridgeParticipant bp) throws ParseException
    {
		CallParticipant cp = bp.callParticipant;
		String response = null;
		String callId = cp.getCallId();

		if (callId == null) {
			cp.setCallId(CallHandler.getNewCallId());

		} else {

			if (callId.equals("0"))
			{
				response = ("Zero is an invalid callId");
				return response;
			}

			if (cp.migrateCall() == false)
			{
				CallHandler callHandler = CallHandler.findCall(callId);

				if (callHandler != null)
				{
					if (callHandler.isCallEnding() == false)
					{
						response = ("CallId " + callId + " is already in use");
						return response;

					} else {
						response = ("Reusing callId for ending call " + callId);
					}
				}
			}
		}

        handleCallAttendant(cp);

		cp.setSecondPartyNumber(config.formatPhoneNumber(cp.getSecondPartyNumber(), cp.getPhoneNumberLocation()));

		if (cp.migrateCall() == false)
		{
			if (cp.getProtocol() == null || "SIP".equalsIgnoreCase(cp.getProtocol()) || "JINGLE".equalsIgnoreCase(cp.getProtocol()) || "NS".equalsIgnoreCase(cp.getProtocol()))
			{
				if (cp.getPhoneNumber() == null)
				{
					if (cp.getInputTreatment() == null)
					{
						if (bp.participantJID == null)
						{
							response = ("You must specify a phone number or an IM address to bridge");
							return response;
						}

					} else {

						if (cp.getInputTreatment().equals("null"))
						{
							cp.setInputTreatment("");
						}

						cp.setPhoneNumber(cp.getInputTreatment());
						cp.setProtocol("NS");
					}

				} else 	{

					if (cp.getPhoneNumber().startsWith("jingle:"))
					{
						cp.setProtocol("JINGLE");
						cp.setPhoneNumber(cp.getPhoneNumber().substring(7));

					} else if ("JINGLE".equalsIgnoreCase(cp.getProtocol()) ) {

						if (bp.participantJID == null)
						{
							response = ("You must specify an IM address to bridge with Jingle");
							return response;
						}

						cp.setPhoneNumber(bp.participantJID.toString());

					} else { // PSTN number

						cp.setPhoneNumber(config.formatPhoneNumber(cp.getPhoneNumber(), cp.getPhoneNumberLocation()));
					}
				}

			} else if ("JINGLE".equalsIgnoreCase(cp.getProtocol()) ) {

				if (bp.participantJID == null)
				{
					response = ("You must specify an IM address to bridge with Jingle");
					return response;
				}

				cp.setPhoneNumber("jingle:" + bp.participantJID.toString());

			} else if ("RTMP".equalsIgnoreCase(cp.getProtocol()) || "RTMFP".equalsIgnoreCase(cp.getProtocol())) {

				cp.setPhoneNumber("voicebridge@rtmp:/" + cp.getRtmpRecieveStream() + "/" + cp.getRtmpSendStream());

			} else response = "Unsupported bridge protocol";
		}

        if (cp.getName() == null || cp.getName().equals(""))
        {
            cp.setName("Anonymous");
		}

		if (cp.migrateCall() == true)
		{
	    	if (cp.getCallId() == null || cp.getSecondPartyNumber() == null)
	    	{
				response = ("You must specify old and new phone numbers to migrate a call");
				return response;
	    	}
		}

		if (cp.getEmailAddress() != null &&	cp.getEmailAddress().indexOf("@") == -1)
		{
			response = ("Email address is not valid");
			return response;
		}

		if (cp.getConferenceId() == null &&	cp.getSecondPartyNumber() == null)
		{
			response = ("You must specify a conference Id");
			return response;
		}

		if (cp.getDisplayName() == null)
		{
			if (cp.getSecondPartyNumber() == null)
			{
				cp.setDisplayName(cp.getConferenceId());

			} else {

				if (cp.getSecondPartyName() != null)
				{
					cp.setDisplayName(cp.getSecondPartyName());
				} else {
					cp.setDisplayName(cp.getSecondPartyNumber());
				}
			}
		}

		/*
		 * For two party calls.
		 */

		if (cp.getConferenceId() != null)
		{
			if (cp.getSecondPartyTreatment() != null)
			{
				cp.setConferenceJoinTreatment(cp.getSecondPartyTreatment());
			}

			if (cp.getSecondPartyCallEndTreatment() != null)
			{
				cp.setConferenceLeaveTreatment(
				cp.getSecondPartyCallEndTreatment());
			}
		}

		if (cp.getSecondPartyNumber() != null)
		{
			if (cp.getConferenceId() == null)
			{
				cp.setConferenceId(cp.getPhoneNumber());
			}
		}

		// If SIP Plugin used, get sip registration

		String userId =  bp.userJID.getNode();
		ProxyCredentials sip = config.getProxyCredentialsByUser(userId);

		if (sip != null)
		{
			Log.info("Found SIP Mapping user " + userId);

			cp.setSipProxy(userId);		// use proxy
			cp.setVoIPGateway(userId);	// use proxy as single gateway
			cp.setFromPhoneNumber(sip.getAuthUserName());
			cp.setName(sip.getUserDisplay());

		} else {

			userId = JiveGlobals.getProperty("voicebridge.default.proxy.username", "admin");
			sip = config.getProxyCredentialsByUser(userId);

			if (sip != null)
			{
				Log.info("Found SIP Mapping proxy user " + userId);

				cp.setSipProxy(userId);		// use proxy
				cp.setVoIPGateway(userId);	// use proxy as single gateway
				cp.setFromPhoneNumber(sip.getAuthUserName());
				cp.setName(sip.getUserDisplay());
			}
		}

		return response;
    }


    /*
     * Some Sun sites such as China have an automated call attendent
     * which asks the user to enter the extension again.
     *
     * If a phone number has ">" in it, we replace the phone number
     * to call with everything before the ">" and set the call
     * answer treatment to be dtmf keys of everything after the ">".
     */

    private void handleCallAttendant(CallParticipant cp)
    {
        String phoneNumber = cp.getPhoneNumber();

        int ix;

        if (phoneNumber != null && phoneNumber.indexOf("sip:") < 0 && phoneNumber.indexOf("@") < 0)
        {
            if ((ix = phoneNumber.indexOf(">")) > 0)
            {
                /*
                 * Must have 5 digit extension
                 */
                if (phoneNumber.length() >= ix + 1 + 5) {
                    cp.setCallAnsweredTreatment("dtmf:" +
                        phoneNumber.substring(ix + 1));

                    cp.setPhoneNumber(phoneNumber.substring(0, ix));
                }
            }
        }

        phoneNumber = cp.getSecondPartyNumber();

        if (phoneNumber != null &&
                phoneNumber.indexOf("sip:") < 0 &&
                phoneNumber.indexOf("@") < 0) {

            if ((ix = phoneNumber.indexOf(">")) > 0) {
                /*
                 * Must have 5 digit extension
                 */
                if (phoneNumber.length() >= ix + 1 + 5) {
                    cp.setSecondPartyTreatment("dtmf:" +
                        phoneNumber.substring(ix + 1));

                    cp.setSecondPartyNumber(phoneNumber.substring(0, ix));
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    //
    // Event management
    //
    // ------------------------------------------------------------------------

    public void handleMessage(Message received)
    {

    }

	public void interceptMessage(Message received)
	{
		//Log.info("VoiceBridge interceptMessage incoming\n" + received);
	}


    public void callEventNotification(CallEvent callEvent)
    {
 		Log.info( "VoiceBridge callEventNotification " + callEvent.toString());

		reportCallEventNotification(callEvent, "monitorCallStatus");
    }


    private static void reportCallEventNotification(CallEvent callEvent, String monitorName)
    {
 		System.out.println( "VoiceBridge reportCallEventNotification " + monitorName + " " + callEvent.toString());

		if ( bridgeParticipants.containsKey(callEvent.getCallId()))
		{
			BridgeParticipant bp = bridgeParticipants.get(callEvent.getCallId());

			sendCallEventNotification(callEvent, monitorName, bp.userJID.toBareJID(), bp);

		} else {	// incoming, send to every one interested

			if (Config.getInstance().getMeetingCode(callEvent.getConferenceId()) != null)
			{
				sendCallEventNotification(callEvent, monitorName, "admin@" + JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname()), null);
			}
		}

    }

    private static void sendCallEventNotification(CallEvent callEvent, String monitorName, String jid, BridgeParticipant bp)
    {
 		System.out.println( "VoiceBridge sendCallEventNotification " + monitorName + " " + jid + " " + callEvent.getConferenceId());

		try {

			String myEvent = CallEvent.getEventString(callEvent.getEvent());
			String callState = callEvent.getCallState().toString();

			String info = callEvent.getInfo() == null ? "" : callEvent.getInfo();
			String dtmf = callEvent.getDtmfKey() == null ? "" : callEvent.getDtmfKey();
			String treatmentdId = callEvent.getTreatmentId() == null ? "" : callEvent.getTreatmentId();
			String callId = callEvent.getCallId() == null ? "" : callEvent.getCallId();
			String confId = callEvent.getConferenceId() == null ? "" : callEvent.getConferenceId();
			String callInfo = callEvent.getCallInfo() == null ? "" : callEvent.getCallInfo();
			int noOfCalls = ConferenceManager.getNumberOfMembers(confId);
			String domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());

			String mucRoom =  Config.getInstance().getMeetingCode(confId);
			String pubsubNode = confId;

			if (mucRoom == null) {

					mucRoom = confId;
			} else {

					pubsubNode = mucRoom;
			}


			if (mucRoom != null)
			{
				if (callEvent.getCallState().equals(CallState.ENDING) || callEvent.getCallState().equals(CallState.ESTABLISHED))
				{
					Presence presence;

					if (callEvent.getCallState().equals(CallState.ENDING))
					{
						presence = new Presence(Presence.Type.unavailable);

					} else {

						presence = new Presence();
					}

					presence.setTo(mucRoom + "@conference." + domainName + "/" + callId);
					presence.addChildElement("x", "http://jabber.org/protocol/muc");
					presence.setFrom(callId + "@openlink." + domainName);

					sendPacket(presence);
				}
			}

			if (pubsubNode != null)
			{
/*
				if (pubsubNodes.containsKey(pubsubNode) == false)
				{
 					System.out.println( "VoiceBridge creating pubsub node " + pubsubNode);

					createPubsubNode(pubsubNode);
					Thread.sleep(1000);
					pubsubNodes.put(pubsubNode, pubsubNode);
				}

 				System.out.println( "VoiceBridge publishing event to pubsub node " + pubsubNode);

				IQ iq = new IQ(IQ.Type.set);
				iq.setFrom(component.getName() + "." + JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname()));
				iq.setTo("pubsub." + domainName);
				Element pubsub = iq.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
				Element publish = pubsub.addElement("publish").addAttribute("node", pubsubNode);
*/

				Message message = new Message();
				message.setFrom(component.getName() + "." + JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname()));
				message.setTo(jid);

				Element event = message.addChildElement("event", "http://jabber.org/protocol/pubsub#event");
				Element items = event.addElement("items").addAttribute("node", pubsubNode);
				Element item = items.addElement("item").addAttribute("id", pubsubNode);

				Element voicebridge = item.addElement("voicebridge", "http://xmpp.org/protocol/openlink:01:00:00/features#voice-bridge");

				voicebridge.addElement("jid").setText(jid);
				voicebridge.addElement("source").setText(monitorName);
				voicebridge.addElement("eventtype").setText(myEvent);
				voicebridge.addElement("dtmf").setText(dtmf);
				voicebridge.addElement("participants").setText(String.valueOf(noOfCalls));
				voicebridge.addElement("callstate").setText(callState);
				voicebridge.addElement("conference").setText(confId);
				voicebridge.addElement("participant").setText(callId);
				voicebridge.addElement("callinfo").setText(callInfo);
				voicebridge.addElement("eventinfo").setText(info);

				if (bp != null)
				{
					if (bp.callParticipant.getRtmpSendStream() != null)
					{
						voicebridge.addElement("sendstream").setText(bp.callParticipant.getRtmpSendStream());
					}

					if (bp.callParticipant.getRtmpRecieveStream() != null)
					{
						voicebridge.addElement("recievestream").setText(bp.callParticipant.getRtmpRecieveStream());
					}

					if (bp.callParticipant.getRtmpVideoStream() != null)
					{
						voicebridge.addElement("videostream").setText(bp.callParticipant.getRtmpVideoStream());
					}
				}

				//sendPacket(iq);
				sendPacket(message);
			}

		} catch (Exception e) {

			System.out.println("sendCallEventNotification " + e);
		}

    }

	public static void createPubsubNode(String interestNode)
	{
		System.out.println("VoiceBridge sendCallEventNotification createPubsubNode - " + interestNode);

		try {
			String domain = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());

			IQ iq1 = new IQ(IQ.Type.set);
			iq1.setFrom(component.getName() + "." + domain);
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

			System.out.println("createPubsubNode " + iq1.toString());
			sendPacket(iq1);

		} catch (Exception e) {

			System.out.println("createPubsubNode " + e);
		}
	}

    public static void logSIPCalls(CallEvent callEvent, boolean incomingCall)
    {
		System.out.println("logSIPCalls " + callEvent);

		try {

			String username = JiveGlobals.getProperty("voicebridge.default.proxy.sipusername", "admin");
			String domain = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());

			IQ iq1 = new IQ(IQ.Type.set);
			iq1.setFrom(username + "@" + component.getName() + "." + domain);
			iq1.setTo("Log." + domain);

			Element log = iq1.setChildElement("log", "http://www.jivesoftware.com/protocol/log");
			Element callLog = log.addElement("callLog");

			callLog.addElement("duration").setText("0");
			callLog.addElement("datetime").setText((new Date()).toString());

			if (incomingCall)
			{
				callLog.addElement("numA").setText(callEvent.getCallInfo());
				callLog.addElement("numB").setText(callEvent.getConferenceId());
				callLog.addElement("type").setText("received");

			} else {

				callLog.addElement("numB").setText(callEvent.getCallInfo());
				callLog.addElement("numA").setText(callEvent.getConferenceId());
				callLog.addElement("type").setText("dialed");
			}

			System.out.println("logSIPCalls " + iq1.toString());
			sendPacket(iq1);

		} catch (Exception e) {

			System.out.println("logSIPCalls " + e);
		}
    }

	public static void sendPacket(Packet packet)
	{
		try {
			ComponentManagerFactory.getComponentManager().sendPacket(component, packet);
		} catch (Exception e) {

			System.out.println("sendPacket " + e);
		}
	}


    public static void registerNotification(String status, ProxyCredentials credentials)
    {
		System.out.println("registerNotification " + status + " " + credentials.getXmppUserName());

		try {
			Config.updateStatus(credentials.getXmppUserName(), status);

		} catch (Exception e) {

			System.out.println("registerNotification " + e);
		}
    }

    public static void incomingCallNotification(CallEvent callEvent)
    {
		System.out.println("incomingCallNotification " + callEvent.toString());
		reportCallEventNotification(callEvent, "incomingCallNotification");

		if (callEvent.getEvent() == callEvent.TREATMENT_DONE && callEvent.getTreatmentId().indexOf("you-are-caller-number.au") > -1)
		{
			if (Config.sipPlugin) logSIPCalls(callEvent, true);
		}

		if (component != null)
		{
			component.incomingCallNotification(callEvent);
		}

    }

    public static void outgoingCallNotification(CallEvent callEvent)
    {
		System.out.println("outgoingCallNotification " + callEvent.toString());
		reportCallEventNotification(callEvent, "outgoingCallNotification");

		if (callEvent.getCallState().equals(CallState.ESTABLISHED) && callEvent.getEvent() == callEvent.STATE_CHANGED && callEvent.getCallInfo().startsWith("voicebridge") == false)
		{
			if (Config.sipPlugin) logSIPCalls(callEvent, false);
		}

		if (component != null)
		{
			component.outgoingCallNotification(callEvent);
		}
    }

    public static void notifyConferenceMonitors(CallEvent callEvent)
    {
		System.out.println("notifyConferenceMonitors " + callEvent.toString());
		reportCallEventNotification(callEvent, "notifyConferenceMonitors");
    }

    // ------------------------------------------------------------------------
    //
    // ConferenceMonitor
    //
    // ------------------------------------------------------------------------

    class ConferenceMonitor {

        private Object service;
        private String conferenceId;

        public ConferenceMonitor(Object service, String conferenceId) {
            this.service = service;
            this.conferenceId = conferenceId;
        }

        public Object getService() {
            return service;
        }

        public String getConferenceId() {
            return conferenceId;
        }

    }


    // ------------------------------------------------------------------------
    //
    // BridgeParticipant
    //
    // ------------------------------------------------------------------------

    class BridgeParticipant {

        public JID userJID;
        public JID participantJID = null;
        public JID secondPartyJID = null;
        public CallParticipant callParticipant;
		public Object callHandler;
		public Object service;
    }


   private void parseBridgeParameters(String parameter, String value)
    {
		if ("nAvg".equalsIgnoreCase(parameter))
		{
			try {
				LowPassFilter.setNAvg(getInteger(value));
			} catch (Exception e) {
				Log.error("parseBridgeParameters : nAvg " + value + " is not numeric" );
			}
			return;
		}

		if ("lpfv".equalsIgnoreCase(parameter))
		{
			try {
				LowPassFilter.setLpfVolumeAdjustment(getDouble(value));
			} catch (Exception e) {
				Log.error("parseBridgeParameters : lpfv " + value + " is not numeric" );
			}
			return;
		}

		if ("forceGatewayError".equalsIgnoreCase(parameter))
		{
			try {
				SipTPCCallAgent.forceGatewayError(stringToBoolean(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("addCallToWhisperGroup".equalsIgnoreCase(parameter))
		{
			String[] tokens = value.split(":");

			if (tokens.length != 2)
			{
				Log.error("You must specify both a whisperGroupId and a callId");
				return;
			}

			String whisperGroupId = tokens[0];
			String callId = tokens[1];

			CallHandler callHandler = CallHandler.findCall(callId);

			if (callHandler == null)
			{
				Log.error("Invalid callId:  " + callId);
				return;
			}

			try {
				callHandler.getMember().addCall(whisperGroupId);
			} catch (ParseException e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("allowShortNames".equalsIgnoreCase(parameter))
		{
			try {
				ConferenceManager.setAllowShortNames(stringToBoolean(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("bridgeLocation".equalsIgnoreCase(parameter))
		{
			Bridge.setBridgeLocation(value);
			return;
		}

		if ("cancelCall".equalsIgnoreCase(parameter))
		{
			CallHandler.hangup(value, "User requested call termination");
			return;
		}

		if ("cancelMigration".equalsIgnoreCase(parameter))
		{
			CallMigrator.hangup(value, "User requested call termination");
			return;
		}

		if ("cnThresh".equalsIgnoreCase(parameter))
		{
			try {
				CallHandler.setCnThresh(getQualifierString(value), getInteger(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
            return;
		}

		if ("comfortNoiseType".equalsIgnoreCase(parameter))
		{
			try {
				MemberSender.setComfortNoiseType(getInteger(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
            return;
		}

		if ("comfortNoiseLevel".equalsIgnoreCase(parameter))
		{
			try {
				RtpPacket.setDefaultComfortNoiseLevel((byte) getInteger(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
            return;
		}

		if ("commonMixDefault".equalsIgnoreCase(parameter))
		{
			try {
				WhisperGroup.setCommonMixDefault(stringToBoolean(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("conferenceInfoShort".equalsIgnoreCase(parameter))
		{
			Log.info(ConferenceManager.getAbbreviatedConferenceInfo());
			return;
		}

		if ("conferenceInfo".equalsIgnoreCase(parameter))
		{
			Log.info(ConferenceManager.getDetailedConferenceInfo());
			return;
		}

		if ("createConference".equalsIgnoreCase(parameter))
		{
			String[] tokens = value.split(":");

			if (tokens.length < 2)
			{
				Log.error("Missing parameters");
				return;
			}

			String conferenceId = tokens[0];

			if (tokens[1].indexOf("PCM") != 0 && tokens[1].indexOf("SPEEX") != 0)
			{
				Log.error("invalid media specification");
				return;
			}

			String mediaPreference = tokens[1];

			String displayName = null;

			if (tokens.length > 2)
			{
				displayName = tokens[2];
			}

			try {
				ConferenceManager.createConference(conferenceId, mediaPreference, displayName);
			} catch (ParseException e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("createWhisperGroup".equalsIgnoreCase(parameter))
		{
			String[] tokens = value.split(":");

			if (tokens.length < 2)
			{
				Log.error("You must specify both a conferenceId and a whisperGroupId");
				return;
			}

			try {
				String conferenceId = tokens[0];
				String whisperGroupId = tokens[1];

				double attenuation = WhisperGroup.getDefaultAttenuation();

				if (tokens.length == 3)
				{
					attenuation = getDouble(tokens[2]);
				}

				ConferenceManager.createWhisperGroup(conferenceId,	whisperGroupId, attenuation);

			} catch (ParseException e) {

				Log.error("Can't create Whisper group " + tokens[1] + " " + e.getMessage());
			}
			return;
		}

		if ("deferMixing".equalsIgnoreCase(parameter))
		{
			try {
				MemberReceiver.deferMixing(getBoolean(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("destroyWhisperGroup".equalsIgnoreCase(parameter))
		{
			String[] tokens = value.split(":");

			if (tokens.length != 2)
			{
				Log.error("You must specify both a conferenceId and a whisperGroupId");
				return;
			}

			try {
				ConferenceManager.destroyWhisperGroup(tokens[0], tokens[1]);
			} catch (ParseException e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("callAnswerTimeout".equalsIgnoreCase(parameter))
		{
			try {
				CallSetupAgent.setDefaultCallAnswerTimeout(getInteger(value));
			} catch (Exception e) {
				Log.error("callAnswerTimeout " + value + " is not numeric" );
			}
			return;
		}

		if ("doNotRecord".equalsIgnoreCase(parameter))
		{
			try {
				boolean booleanValue = getBoolean(value);
				String callId = getQualifierString(value);

				if (callId == null)
				{
					Log.error("Call id is missing");
					return;
				}

				try {

					CallHandler.setDoNotRecord(callId, booleanValue);

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("dtmfSuppression".equalsIgnoreCase(parameter))
		{
			try {
				boolean booleanValue = getBoolean(value);
				String callId = getQualifierString(value);

				if (callId == null)
				{
					Log.error("Call id is missing");
					return;
				}

				try {

					CallHandler.setDtmfSuppression(callId, booleanValue);

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("drop".equalsIgnoreCase(parameter))
		{
			try {
				int integerValue = getInteger(value);
				String callId = getQualifierString(value);

				if (callId == null)
				{
					Log.error("Call id is missing");
					return;
				}

				try {

					CallHandler.setDropPackets(callId, integerValue);

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("duplicateCallLimit".equalsIgnoreCase(parameter))
		{
			try {
				CallHandler.setDuplicateCallLimit(getInteger(value));
			} catch (Exception e) {
				Log.error("duplicateCallLimit " + value + " is not numeric" );
			}
			return;
		}


		if ("directConferencing".equalsIgnoreCase(parameter))
		{
			try {
				IncomingCallHandler.setDirectConferencing(getBoolean(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("distributedConferenceInfo".equalsIgnoreCase(parameter))
		{
			Log.info(ConferenceManager.getDistributedConferenceInfo());
			return;
		}

		if ("dropDb".equalsIgnoreCase(parameter))
		{
			ConferenceManager.dropDb();
			return;
		}

		if ("enablePSTNCalls".equalsIgnoreCase(parameter))
		{
			try {
				CallHandler.enablePSTNCalls(getBoolean(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("endConference".equalsIgnoreCase(parameter))
		{
			try {
				ConferenceManager.endConference(value);

			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("firstRtpPort".equalsIgnoreCase(parameter))
		{
			try {
				ConferenceMember.setFirstRtpPort(getInteger(value));
			} catch (Exception e) {
				Log.error("firstRtpPort " + value + " is not numeric" );
			}
			return;
		}

		if ("setForcePrivateMix".equalsIgnoreCase(parameter))
		{
			try {
				MixManager.setForcePrivateMix(getBoolean(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("forwardData".equalsIgnoreCase(parameter))
		{
			try {
				String[] tokens = value.split(":");

				if (tokens.length < 2)
				{
					Log.error("Missing parameters:  " + value);
					return;
				}

				CallHandler dest = CallHandler.findCall(tokens[0]);

				if (dest == null)
				{
					Log.error("Invalid callId:  " + tokens[0]);
					return;
				}

				CallHandler src = CallHandler.findCall(tokens[1]);

				if (src == null)
				{
					Log.error("Invalid callId:  " + tokens[1]);
					return;
				}

				src.getMember().getMemberReceiver().addForwardMember(dest.getMember().getMemberSender());

			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}


		if ("forwardDtmfKeys".equalsIgnoreCase(parameter))
		{
			try {
				MemberReceiver.setForwardDtmfKeys(getBoolean(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("gc".equalsIgnoreCase(parameter))
		{
			System.gc();
			return;
		}

		if ("gcs".equalsIgnoreCase(parameter))
		{
			Log.info(CallHandler.getCallStateForAllCalls());
			return;
		}

		if ("getCallState".equalsIgnoreCase(parameter))
		{
			String callId = getString(value);

			CallHandler callHandler = CallHandler.findCall(callId);

			if (callHandler == null)
			{
				Log.error("Invalid callId:  " + callId);
				return;
			}

			Log.info(callHandler.getCallState());

			return;
		}

		if ("getAllAbbreviatedMixDescriptors".equalsIgnoreCase(parameter))
		{
			Log.info(CallHandler.getAllAbbreviatedMixDescriptors());
			return;
		}

		if ("getAbbreviatedMixDescriptors".equalsIgnoreCase(parameter))
		{
			String callId = getString(value);

			CallHandler callHandler = CallHandler.findCall(callId);

			if (callHandler == null)
			{
				Log.error("Invalid callId:  " + callId);
				return;
			}

	        Log.info(callHandler.getMember().getAbbreviatedMixDescriptors());
			return;
		}

		if ("getAllMixDescriptors".equalsIgnoreCase(parameter))
		{
			Log.info(CallHandler.getAllMixDescriptors());
			return;
		}

		if ("getMixDescriptors".equalsIgnoreCase(parameter))
		{
			String callId = getString(value);

			CallHandler callHandler = CallHandler.findCall(callId);

			if (callHandler == null)
			{
				Log.error("Invalid callId:  " + callId);
				return;
			}

	        Log.info(callHandler.getMember().getMixDescriptors());
			return;
		}

		if ("getStatistics".equalsIgnoreCase(parameter))
		{

/*
				(  service ).invoke( "statisticsNotification", new Object[] { String.valueOf(ConferenceManager.getNumberOfConferences()),
																			  String.valueOf(ConferenceManager.getTotalMembers()),
																			  String.valueOf(CallHandler.getTotalSpeaking()),
																			  String.valueOf(Math.round(ConferenceSender.getTimeBetweenSends() * 10000) / 10000.),
																			  String.valueOf(Math.round(ConferenceSender.getAverageSendTime() * 10000) / 10000.),
																			  String.valueOf(Math.round(ConferenceSender.getMaxSendTime() * 10000) / 10000.)

																			});
*/

		}

		if ("getBriefConferenceInfo".equalsIgnoreCase(parameter))
		{
			Log.info(ConferenceManager.getBriefConferenceInfo());
			return;
		}

		if ("gc".equalsIgnoreCase(parameter))
		{
			System.gc();
			return;
		}

		if ("incomingCallTreatment".equalsIgnoreCase(parameter))
		{
			IncomingCallHandler.setIncomingCallTreatment(value);
			return;
		}

		if ("incomingCallVoiceDetection".equalsIgnoreCase(parameter))
		{
			try {
				IncomingCallHandler.setIncomingCallVoiceDetection(getBoolean(value));
			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("internationalPrefix".equalsIgnoreCase(parameter))
		{
			if (value.equals("\"\"") || value.equals("''"))
			{
				value = "";
			}

			config.setInternationalPrefix(value);
			return;
		}

		if ("internalExtenLength".equalsIgnoreCase(parameter))
		{
			try {
				config.setInternalExtenLength(getInteger(value));
			} catch (Exception e) {
				Log.error("internalExtenLength " + value + " is not numeric" );
			}
		}

		if ("conferenceJoinTreatment".equalsIgnoreCase(parameter))
		{
			try {
				String[] tokens = value.split(":");

				if (tokens.length != 2)
				{
				   Log.error("conferenceJoinTreatment requires two inputs");
				   return;
				}

				ConferenceManager.setConferenceJoinTreatment(tokens[1], tokens[0]);

			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("conferenceLeaveTreatment".equalsIgnoreCase(parameter))
		{
			try {
				String[] tokens = value.split(":");

				if (tokens.length != 2)
				{
				   Log.error("conferenceLeaveTreatment requires two inputs");
				   return;
				}

				ConferenceManager.setConferenceLeaveTreatment(tokens[1], tokens[0]);

			} catch (ParseException e) {
				Log.error(e.toString());
			}
			return;
		}


		if ("lastRtpPort".equalsIgnoreCase(parameter))
		{
			try {
				ConferenceMember.setLastRtpPort(getInteger(value));
			} catch (Exception e) {
				Log.error("lastRtpPort " + value + " is not numeric" );
			}
			return;
		}


		if ("loneReceiverPort".equalsIgnoreCase(parameter))
		{
			try {
				ConferenceManager.setLoneReceiverPort(getInteger(value));
			} catch (Exception e) {
				Log.error("loneReceiverPort " + value + " is not numeric" );
			}
			return;
		}

		if ("longDistancePrefix".equalsIgnoreCase(parameter))
		{
			if (value.equals("\"\"") || value.equals("''"))
			{
				value = "";
			}

			config.setLongDistancePrefix(value);
			return;
		}

		if ("migrateToBridge".equalsIgnoreCase(parameter))
		{
			try {
				String[] tokens = value.split(":");

				if (tokens.length != 3)
				{
				   Log.error("migrateToBridge requires three inputs");
				   return;
				}

				String bridge = tokens[0];
				String port = tokens[1];
				String callId = tokens[2];

				CallHandler callHandler = CallHandler.findCall(callId);

				if (callHandler == null)
				{
					Log.error("Invalid callId: " + callId);
					return;
				}

				CallParticipant cp = callHandler.getCallParticipant();

				if (cp.getInputTreatment() != null)
				{
					cp.setPhoneNumber(null);
				}
/*
				BridgeConnector bridgeConnector;

				int serverPort;

				try {
					serverPort = Integer.parseInt(port);

				} catch (NumberFormatException e) {

					Log.error("Invalid bridge server port:  " + port);
					return;
				}

				try {
					bridgeConnector = new BridgeConnector(bridge, serverPort, 5000);

				} catch (IOException e) {
					Log.error("Unable to connect to bridge " + bridge	+ " " + e.getMessage());
					return;
				}

				callHandler.suppressStatus(true);

				try {
					String s = cp.getCallSetupRequest();
					s = s.substring(0, s.length() - 1);  // get rid of last new line
					bridgeConnector.sendCommand(s);

				} catch (IOException e) {

					Log.error("Unable to send command to bridge:  " + e.getMessage());
					return;
				}

				bridgeConnector.addCallEventListener(requestHandler);

				// XXX need to figure out how to deal with Private Mixes now that the call has moved!
				//TODO convert to RTMP or Openlink
*/
			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("senderThreads".equalsIgnoreCase(parameter))
		{
			try {
				ConferenceSender.setSenderThreads(getInteger(value));
			} catch (Exception e) {
				Log.error("senderThreads " + value + " is not numeric" );
			}
			return;
		}

		if ("minJitterBufferSize".equalsIgnoreCase(parameter))
		{
			try {
				String[] tokens = value.split(":");

				if (tokens.length != 2)
				{
				   Log.error("minJitterBufferSize requires two inputs");
				   return;
				}

				int minJitterBufferSize = getInteger(tokens[0]);
				CallHandler callHandler = CallHandler.findCall(tokens[1]);

				if (callHandler == null)
				{
					Log.error("Invalid callId:  " + tokens[1]);
					return;
				}

				callHandler.getMember().getMemberReceiver().setMinJitterBufferSize(minJitterBufferSize);

			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("maxJitterBufferSize".equalsIgnoreCase(parameter))
		{
			try {
				String[] tokens = value.split(":");

				if (tokens.length != 2)
				{
				   Log.error("maxJitterBufferSize requires two inputs");
				   return;
				}

				int minJitterBufferSize = getInteger(tokens[0]);
				CallHandler callHandler = CallHandler.findCall(tokens[1]);

				if (callHandler == null)
				{
					Log.error("Invalid callId:  " + tokens[1]);
					return;
				}

				callHandler.getMember().getMemberReceiver().setMaxJitterBufferSize(minJitterBufferSize);

			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("monitorCallStatus".equalsIgnoreCase(parameter))
		{
			try {
				boolean booleanValue = getBoolean(value);
				String callId = getQualifierString(value);

				if (callId == null)
				{
					Log.error("Call id is missing");
					return;
				}

				try {
					CallHandler callHandler = CallHandler.findCall(callId);

					if (callHandler == null)
					{
						Log.error("No such callId:  " + callId);
						return;
					}

					if (booleanValue == true)
					{
						callHandler.addCallEventListener(this);

					} else {

						callHandler.removeCallEventListener(this);
					}

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}

			return;
		}


		if ("muteCall".equalsIgnoreCase(parameter))
		{
			try {
				boolean booleanValue = getBoolean(value);
				String callId = getQualifierString(value);

				if (callId == null)
				{
					Log.error("Call id is missing");
					return;
				}

				try {
					CallHandler callHandler = CallHandler.findCall(callId);

					if (callHandler == null)
					{
						Log.error("No such callId:  " + callId);
						return;
					}

	    			CallHandler.setMuted(callId, booleanValue);

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("muteWhisperGroup".equalsIgnoreCase(parameter))
		{
			try {
				boolean booleanValue = getBoolean(value);
				String callId = getQualifierString(value);

				if (callId == null)
				{
					Log.error("Call id is missing");
					return;
				}

				try {
					CallHandler callHandler = CallHandler.findCall(callId);

					if (callHandler == null)
					{
						Log.error("No such callId:  " + callId);
						return;
					}

	    			CallHandler.setMuteWhisperGroup(callId, booleanValue);

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("muteConference".equalsIgnoreCase(parameter))
		{
			try {
				boolean booleanValue = getBoolean(value);
				String callId = getQualifierString(value);

				if (callId == null)
				{
					Log.error("Call id is missing");
					return;
				}

				try {
					CallHandler callHandler = CallHandler.findCall(callId);

					if (callHandler == null)
					{
						Log.error("No such callId:  " + callId);
						return;
					}

	    			CallHandler.setConferenceMuted(callId, booleanValue);

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("numberOfCalls".equalsIgnoreCase(parameter))
		{
			try {
				String[] tokens = value.split(":");

				if (tokens.length != 1)
				{
					Log.error("You must specify a conference id");
					return;
				}

				CallEvent event = new CallEvent(CallEvent.NUMBER_OF_CALLS);
				event.setNumberOfCalls(ConferenceManager.getNumberOfMembers(tokens[0]));
				callEventNotification(event);

			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("outsideLinePrefix".equalsIgnoreCase(parameter))
		{
			if (value.equals("\"\"") || value.equals("''"))
			{
				value = "";
			}

			config.setOutsideLinePrefix(value);
			return;
		}

		if ("receiverPause".equalsIgnoreCase(parameter))
		{
			try {
				ConferenceReceiver.setReceiverPause(getInteger(value));
			} catch (Exception e) {
				Log.error("pause " + value + " is not numeric" );
			}
			return;
		}

		if ("pauseTreatmentToCall".equalsIgnoreCase(parameter))
		{
			try {
				String[] tokens = value.split(":");

				CallHandler callHandler = CallHandler.findCall(tokens[0]);

				if (callHandler == null)
				{
					Log.error("Invalid callId:  " + tokens[0]);
					return;
				}

				String treatmentId = null;

				if (tokens.length > 1)
				{
					treatmentId = tokens[1];
				}

           	 	callHandler.getMember().pauseTreatment(treatmentId, true);

			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("pauseTreatmentToConference".equalsIgnoreCase(parameter))
		{
			try {
				String treatment = getTreatment(value);
				value = value.substring(treatment.length());
				String conferenceId = getQualifierString(value);

				if (conferenceId == null)
				{
					Log.error("conferenceId must be specified:  " + value);
					return;
				}

            	ConferenceManager.pauseTreatment(conferenceId, treatment, true);

			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("playTreatmentToCall".equalsIgnoreCase(parameter))
		{
			try {
				String treatment = getTreatment(value);
				double volume[] = getVolume(value);

				value = value.substring(treatment.length());
				String callId = getQualifierString(value);

				if (callId == null)
				{
					Log.error("callId must be specified:  " + value);
					return;
				}

				try {
					CallHandler.playTreatmentToCall(callId, treatment);

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
					return;

				} catch (IOException e) {
					Log.error("Unable to read treatment file " + treatment + " " + e.getMessage());
					return;
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("playTreatmentToConference".equalsIgnoreCase(parameter))
		{
			try {
				String treatment = getTreatment(value);
				double volume[] = getVolume(value);

				value = value.substring(treatment.length());
				String conferenceId = getQualifierString(value);

				if (conferenceId == null)
				{
					Log.error("conference Id must be specified:  " + value);
					return;
				}

				try {
					 ConferenceManager.playTreatment(conferenceId, treatment);

				} catch (NoSuchElementException e) {
					Log.error("Invalid conference Id specified:  " + value);
					return;
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("playTreatmentToAllConferences".equalsIgnoreCase(parameter))
		{
			try {
				String treatment = getTreatment(value);
				double volume[] = getVolume(value);
				value = value.substring(treatment.length());

				try {
					 ConferenceManager.playTreatmentToAllConferences(treatment);

				} catch (Exception e) {
					Log.error("Error playing treatment :  " + value);
					return;
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}
			return;
		}

		if ("transferCall".equalsIgnoreCase(parameter))
		{
			try {
				String callId = getString(value);

				if (callId == null)
				{
					Log.error("Call id is missing");
					return;
				}

				try {
					CallHandler callHandler = CallHandler.findCall(callId);

					if (callHandler == null)
					{
						Log.error("No such callId:  " + callId);
						return;
					}

					String conferenceId = getQualifierString(value);
					IncomingCallHandler.transferCall(callId, conferenceId);

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}

			return;
		}

		if ("sendDtmfKey".equalsIgnoreCase(parameter))
		{
			try {
				String callId = getString(value);

				if (callId == null)
				{
					Log.error("Call id is missing");
					return;
				}

				try {
					CallHandler callHandler = CallHandler.findCall(callId);

					if (callHandler == null)
					{
						Log.error("No such callId:  " + callId);
						return;
					}

					String dtmfKey = getQualifierString(value);
					callHandler.dtmfKeys(dtmfKey);

				} catch (NoSuchElementException e) {
					Log.error("Invalid callId specified:  " + value);
				}

			} catch (Exception e) {
				Log.error(e.toString());
			}

			return;
		}
	}


}
