package com.ifsoft.cti;

import java.io.Serializable;
import java.util.*;
import org.xmpp.packet.JID;

import org.apache.log4j.Logger;


public class OpenlinkUserInterest
{

    private OpenlinkUser openlinkUser;
    private OpenlinkInterest openlinkInterest;
    private String defaultInterest;
	private String callFWD = "false";
	private String callFWDDigits = "";
    private Map<String, OpenlinkCall> openlinkCalls;
	private Map<String, OpenlinkSubscriber> openlinkSubscribers;
	private int maxNumCalls = 0;

    protected Logger Log = Logger.getLogger(getClass().getName());


    public OpenlinkUserInterest()
    {
        defaultInterest = "false";
        openlinkCalls = Collections.synchronizedMap( new HashMap<String, OpenlinkCall>());
        openlinkSubscribers = Collections.synchronizedMap( new HashMap<String, OpenlinkSubscriber>());
    }

	public String getInterestName() {
		return openlinkInterest.getInterestId() + openlinkUser.getUserNo();
	}

    public void handleCallInfo(String sCallNo, String sOpenlinkLineNo, String sOpenlinkLineName, String sNewLineState, String speakerCount, String handsetCount, String direction, String sPrivacyOn, String sRealDDI, String lineType, String sELC)
    {
        OpenlinkCall openlinkCall = createCallById(sCallNo);
        openlinkCall.start();
        openlinkCall.line = sOpenlinkLineNo;
        openlinkCall.label = sOpenlinkLineName;
		openlinkCall.console = getUser().getDeviceNo();
		openlinkCall.handset = getUser().getHandsetNo();
		openlinkCall.direction = "I".equals(direction) ? "Incoming" : "Outgoing";
		openlinkCall.setPrivacy(sPrivacyOn);

		if ("I".equals(sNewLineState))
		{
			openlinkCall.setState("ConnectionCleared");
		}

		if ("R".equals(sNewLineState))
		{
			openlinkCall.setState("CallDelivered");
        	openlinkCall.direction = "Incoming";
		}

		if ("C".equals(sNewLineState) || "A".equals(sNewLineState))
		{
			openlinkCall.setState("CallEstablished");
		}

		if ("H".equals(sNewLineState))
		{
			openlinkCall.setState("CallHeld");
		}

		if ("F".equals(sNewLineState))
		{
			openlinkCall.setState("CallConferenced");
		}

		openlinkCall.setValidActions();
	}

    public void handleCallELC(String sCallNo, String sOpenlinkLineNo, String sOpenlinkLineName, String sOpenlinkConsoleNo, String sOpenlinkUserNo, String sHandsetNo, String sELC, String sConnectOrDisconnect)
    {
        OpenlinkCall openlinkCall = createCallById(sCallNo);
        openlinkCall.line = sOpenlinkLineNo;

		Log.info("handleCallELC " + openlinkCall.callid + " "  + sConnectOrDisconnect);

        if("C".equals(sConnectOrDisconnect))
        {
            openlinkCall.localConferenced = true;
        	openlinkCall.setState("CallConferenced");

        } else {
            openlinkCall.localConferenced = false;
 			openlinkCall.setState("CallEstablished");
		}

		openlinkCall.setValidActions();

    }

    public void handleBusyLine(String sCallNo, String sOpenlinkLineNo, String sOpenlinkLineName, String sOpenlinkConsoleNo, String sOpenlinkUserNo, String sOldLineState, String sNewLineState,
            String sHandsetOrSpeaker, String sSpeakerNo, String sHandsetNo, String sConnectOrDisconnect)
	{

    }

    public void handleConnectionCleared(String sCallNo)
    {
        OpenlinkCall openlinkCall = createCallById(sCallNo);
        openlinkCall.setState("ConnectionCleared");
        openlinkCall.setValidActions();
        openlinkCall.participation = "Inactive";
        openlinkCall.endDuration();

        openlinkCall.clear();

        getUser().setIntercom(false);
    }

    public void handleCallOutgoing(String state, String sCallNo, String sCLI, String sOpenlinkLabel)
    {
        OpenlinkCall openlinkCall = createCallById(sCallNo);
		openlinkCall.setCLI(sCLI);
		openlinkCall.setCLILabel(sCLI);
        openlinkCall.label = sOpenlinkLabel;
        openlinkCall.direction = "Outgoing";
        openlinkCall.connectState = state;
        openlinkCall.setState(state);
		openlinkCall.setValidActions();
        openlinkCall.participation = "Active";
    }

    public void handleCallIncoming(String sCallNo, String from, String to)
    {
        OpenlinkCall openlinkCall = createCallById(sCallNo);
        openlinkCall.start();
        openlinkCall.line = sCallNo;

        openlinkCall.ddi = to;
        openlinkCall.ddiLabel = to;
		openlinkCall.setCLI(from);
		openlinkCall.setCLILabel(from);

        openlinkCall.label = getInterest().getInterestLabel();
        openlinkCall.direction = "Incoming";
        openlinkCall.connectState = "CallEstablished";
        openlinkCall.setState("CallDelivered");
		openlinkCall.setValidActions();
        openlinkCall.participation = "Inactive";
    }

    public void handleCallConnected(String sCallNo)
    {
		Log.debug("setCurrentCall " + sCallNo);

        OpenlinkCall openlinkCall = createCallById(sCallNo);
        openlinkCall.start();
        openlinkCall.label = getInterest().getInterestLabel();

		setCurrentCall(openlinkCall);

        openlinkCall.setState(openlinkCall.connectState);
        openlinkCall.setValidActions();
        openlinkCall.startDuration();
        openlinkCall.participation = "Active";
		openlinkCall.startParticipation();
    }


    public void handleCallPrivate(String sCallNo, String sOpenlinkLineNo, String sOpenlinkConsoleNo, String sOpenlinkUserNo, String sHandsetNo, String sPrivacyOn)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			openlinkCall.setPrivacy(sPrivacyOn);
			openlinkCall.setValidActions();
		}
    }


    public void handleCallPrivateElsewhere(String sCallNo, String sOpenlinkLineNo, String sOpenlinkConsoleNo, String sOpenlinkUserNo, String sHandsetNo, String sPrivacyOn)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			openlinkCall.setPrivacy(sPrivacyOn);
			openlinkCall.setValidActions();
		}
    }


    public void handleCallAbandoned(String sCallNo)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			openlinkCall.setState("CallMissed");
			openlinkCall.setValidActions();
			openlinkCall.clear();
		}
    }

    public void handleCallConferenced(String sCallNo)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			setCurrentCall(openlinkCall);

			openlinkCall.setState("CallConferenced");
			openlinkCall.setValidActions();
			openlinkCall.startDuration();
			openlinkCall.participation = "Active";
			openlinkCall.startParticipation();
		}
    }



	public OpenlinkCall getCurrentCall(String handset)
	{
		OpenlinkCall openlinkCall = null;

		OpenlinkCall intercomCall = getUser().getCurrentICMCall();
		OpenlinkCall openlinkCall1 = getUser().getCurrentHS1Call();
		OpenlinkCall openlinkCall2 = getUser().getCurrentHS2Call();

		if ("1".equals(handset))
			openlinkCall = openlinkCall1;

		else if ("2".equals(handset))
			openlinkCall = openlinkCall2;

		else if ("0".equals(handset))
			openlinkCall = intercomCall;

		else if ("3".equals(handset))
			openlinkCall = openlinkCall1;

        return openlinkCall;
	}

	private void setCurrentCall(OpenlinkCall openlinkCall)
	{
		Log.debug("setCurrentCall " + openlinkCall.getCallID() + " " + openlinkCall.handset + " " + openlinkCall.speaker);

        openlinkCall.console = getUser().getDeviceNo();

        if ("1".equals(openlinkCall.handset))
        	getUser().setCurrentHS1Call(openlinkCall);

        else if ("2".equals(openlinkCall.handset))
        	getUser().setCurrentHS2Call(openlinkCall);

        else if ("65".equals(openlinkCall.speaker))
        	getUser().setCurrentICMCall(openlinkCall);

        else if ("3".equals(openlinkCall.handset))
        	getUser().setCurrentHS1Call(openlinkCall);
	}

    public void handleCallBusy(String sCallNo)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			openlinkCall.start();
			openlinkCall.setState("CallBusy");
			openlinkCall.delivered = true;
			openlinkCall.setValidActions();
			openlinkCall.participation = "Inactive";
		}
    }


    public void handleCallFailed(String sCallNo, String sOpenlinkLineNo, String sOpenlinkLineName, String sOpenlinkConsoleNo, String sOpenlinkUserNo, String sOldLineState, String sNewLineState,
            String sHandsetOrSpeaker, String sSpeakerNo, String sHandsetNo, String sConnectOrDisconnect)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			openlinkCall.clear();

			openlinkCall.line = sOpenlinkLineNo;
			openlinkCall.label = sOpenlinkLineName;
			openlinkCall.setState("CallFailed");
			openlinkCall.setValidActions();
			openlinkCall.participation = "Inactive";
		}
    }

    public void handleCallHeld(String sCallNo)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			openlinkCall.setState("CallHeld");
			openlinkCall.setValidActions();
			openlinkCall.participation = "Inactive";
			openlinkCall.endDuration();
		}
    }

    public void handleTransfer(String sCallNo, String sOpenlinkLineNo, String sOpenlinkConsoleNo, String sOpenlinkUserNo, String sTransferUserNo)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			openlinkCall.openlinkTransferFlag = true;
		}
    }

    public void handleIntercom(String sCallNo)
    {
        OpenlinkCall openlinkCall = createCallById(sCallNo);
        openlinkCall.platformIntercom = true;
        openlinkCall.setState("CallEstablished");
        openlinkCall.setValidActions();

        getUser().setIntercom(true);
    }

    public void handleCallProgress(String sCallNo, String sOpenlinkLineNo, String sChannelNo, String sOpenlinkFlag)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			openlinkCall.setCallProgress(sOpenlinkFlag);

			if ("Outgoing".equals(openlinkCall.direction))
			{
				if("CallOriginated".equals(openlinkCall.getState()))
				{
					if("0".equals(sOpenlinkFlag))
					{
						if("D".equals(getInterest().getInterestType()))
						{
							openlinkCall.setState("CallDelivered");

						} else
							openlinkCall.setState("CallEstablished");

					} else if("4".equals(sOpenlinkFlag) || "1".equals(sOpenlinkFlag)) {

						openlinkCall.setState("CallEstablished");

					} else
						openlinkCall.setState("CallFailed");
				} else

				if("CallDelivered".equals(openlinkCall.getState()))
				{
					if("2".equals(sOpenlinkFlag))
					{
						openlinkCall.setState("CallEstablished");

					} else if("4".equals(sOpenlinkFlag) || "1".equals(sOpenlinkFlag)) {

						openlinkCall.setState("CallEstablished");

					} else
						openlinkCall.setState("CallFailed");
				}

				openlinkCall.setValidActions();
			}

			if("CallEstablished".equals(openlinkCall.getState()))
			{
				openlinkCall.startDuration();
			}
		}
    }

    public void handleCallProceeding(OpenlinkComponent component, String sCallNo, String sOpenlinkLineNo, String sDigits, String sEndFlag)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			openlinkCall.proceedingDigitsBuffer = (new StringBuilder()).append(openlinkCall.proceedingDigitsBuffer).append(sDigits.trim()).toString();

			if("Y".equals(sEndFlag) && !"".equals(openlinkCall.proceedingDigitsBuffer))
			{
				openlinkCall.proceedingDigits = openlinkCall.proceedingDigitsBuffer;
				openlinkCall.proceedingDigitsBuffer = "";
				openlinkCall.proceedingDigitsLabel = openlinkCall.proceedingDigits;

				String cononicalNumber = openlinkCall.proceedingDigits;

				try
				{
					//cononicalNumber = component.formatCanonicalNumber(openlinkCall.proceedingDigits);
				}
				catch(Exception e) { }

				//if(component.openlinkLdapService.cliLookupTable.containsKey(cononicalNumber))
				//	openlinkCall.proceedingDigitsLabel = (String)component.openlinkLdapService.cliLookupTable.get(cononicalNumber);
			}
		}
    }


    public void handleCallMoved(String sCallNo, String sOpenlinkLineNo, String sOpenlinkLineNo2)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
        	openlinkCall.line = sOpenlinkLineNo2;
		}
    }

    public void handleRecallTransfer(String sCallNo, String sOpenlinkLineNo, String sOpenlinkLineNo2, String transferStatusFlag)
    {
        OpenlinkCall openlinkCall = getCallById(sCallNo);

        if (openlinkCall != null)
        {
			if("0".equals(transferStatusFlag))
			{
				openlinkCall.setState("CallTransferring");
				openlinkCall.transferFlag = false;

			} else if("1".equals(transferStatusFlag)) {

				openlinkCall.setState("CallEstablished");
				openlinkCall.transferFlag = false;

				if (openlinkCall.previousCalledNumber != null)
				{
					Iterator<OpenlinkUserInterest> it3 = getInterest().getUserInterests().values().iterator();

					while( it3.hasNext() )
					{
						OpenlinkUserInterest theUserInterest = (OpenlinkUserInterest)it3.next();
						OpenlinkCall theCall = theUserInterest.getCallByLine(openlinkCall.getLine());

						if (theCall != null)
						{
							theCall.proceedingDigits = openlinkCall.previousCalledNumber;
							theCall.proceedingDigitsLabel = openlinkCall.previousCalledLabel;
						}
					}
				}

			} else if("2".equals(transferStatusFlag)) {

				openlinkCall.setState("CallTransferred");

			}

			openlinkCall.setValidActions();
		}
    }

    public String getDefault()
    {
        return defaultInterest;
    }

    public void setDefault(String defaultInterest)
    {
        this.defaultInterest = defaultInterest;
    }

    public OpenlinkUser getUser()
    {
        return openlinkUser;
    }

    public void setUser(OpenlinkUser openlinkUser)
    {
        this.openlinkUser = openlinkUser;
    }

    public Map<String, OpenlinkSubscriber> getSubscribers()
    {
        return openlinkSubscribers;
    }

    public void setSubscribers(Map<String, OpenlinkSubscriber> openlinkSubscribers)
    {
        this.openlinkSubscribers = openlinkSubscribers;
    }

    public boolean isSubscribed(JID subscriber)
    {
        return openlinkSubscribers.containsKey(subscriber.getNode());
    }


    public OpenlinkSubscriber getSubscriber(JID subscriber)
    {
        OpenlinkSubscriber openlinkSubscriber = null;

        if (openlinkSubscribers.containsKey(subscriber.getNode()))
        {
            openlinkSubscriber = (OpenlinkSubscriber)openlinkSubscribers.get(subscriber.getNode());
        } else
        {
            openlinkSubscriber = new OpenlinkSubscriber();
            openlinkSubscriber.setJID(subscriber);
            openlinkSubscribers.put(subscriber.getNode(), openlinkSubscriber);
        }
        return openlinkSubscriber;
    }

    public void removeSubscriber(JID subscriber)
    {
		openlinkSubscribers.remove(subscriber.getNode());
	}

    public boolean canPublish(OpenlinkComponent component)
    {
		if (openlinkSubscribers.size() == 0)
		{
			return false;
		}

		boolean anySubscriberOnline = false;

		Iterator<OpenlinkSubscriber> iter = openlinkSubscribers.values().iterator();

		while( iter.hasNext() )
		{
			OpenlinkSubscriber subscriber = (OpenlinkSubscriber)iter.next();

			if (subscriber.getOnline() || component.isComponent(subscriber.getJID()))
			{
				anySubscriberOnline = true;
				break;
			}
		}

		return anySubscriberOnline;
    }

    public OpenlinkInterest getInterest()
    {
        return openlinkInterest;
    }

    public void setInterest(OpenlinkInterest openlinkInterest)
    {
        this.openlinkInterest = openlinkInterest;
    }


    public synchronized OpenlinkCall createCallById(String callID)
    {
		Log.debug("createCallById " + callID);

        OpenlinkCall openlinkCall = null;

        if(openlinkCalls.containsKey(callID))
        {
            openlinkCall = (OpenlinkCall)openlinkCalls.get(callID);

        } else {

            openlinkCall = new OpenlinkCall();
            openlinkCall.callid = callID;
            openlinkCall.setOpenlinkUserInterest(this);

			if("D".equals(getInterest().getInterestType()))	// set default caller ID for directory numbers
			{
				openlinkCall.ddi = getInterest().getInterestValue();
				openlinkCall.ddiLabel = getInterest().getInterestLabel();
			}

            openlinkCall.initialiseDuration();

            openlinkCalls.put(callID, openlinkCall);
        }
        return openlinkCall;
    }


    public OpenlinkCall getCallById(String callID)
    {
        OpenlinkCall openlinkCall = null;

        if(openlinkCalls.containsKey(callID))
        {
            openlinkCall = (OpenlinkCall)openlinkCalls.get(callID);
		}
        return openlinkCall;
	}

	public OpenlinkCall getCallByLine(String line)
	{
        OpenlinkCall lineCall = null;

		Iterator it2 = openlinkCalls.values().iterator();

		while( it2.hasNext() )
		{
			OpenlinkCall openlinkCall = (OpenlinkCall)it2.next();

			if (line.equals(openlinkCall.line))
			{
				lineCall = openlinkCall;
				break;
			}
		}

		return  lineCall;
	}

    public void removeCallById(String callID)
    {
        if(openlinkCalls.containsKey(callID))
        {
            OpenlinkCall openlinkCall = (OpenlinkCall)openlinkCalls.get(callID);
            openlinkCall = null;
            openlinkCalls.remove(callID);
        }
	}

    public Map<String, OpenlinkCall> getCalls()
    {
        return openlinkCalls;
    }


	public boolean isLineActive(String line)
	{
		OpenlinkCall openlinkCall1 = getUser().getCurrentHS1Call();
		OpenlinkCall openlinkCall2 = getUser().getCurrentHS2Call();

		boolean active = false;

		if (openlinkCall1 != null && !"".equals(openlinkCall1.line))
		{
			if (line.equals(String.valueOf(Long.parseLong(openlinkCall1.line))))
				active = true;
		}

		if (openlinkCall2 != null && !"".equals(openlinkCall2.line))
		{
			if (line.equals(String.valueOf(Long.parseLong(openlinkCall2.line))))
				active = true;
		}

		return active;
	}

	public boolean getHandsetBusyStatus()
	{
		OpenlinkCall openlinkCall1 = getUser().getCurrentHS1Call();
		OpenlinkCall openlinkCall2 = getUser().getCurrentHS2Call();

		if (openlinkCall1 == null && openlinkCall2 == null)
			return false;

		Iterator it2 = openlinkCalls.values().iterator();
		boolean busy1 = false;
		boolean busy2 = false;

		while( it2.hasNext() )
		{
			OpenlinkCall openlinkCall = (OpenlinkCall)it2.next();

			if (openlinkCall1 != null && openlinkCall.getCallID().equals(openlinkCall1.getCallID()))
			{
				busy1 = true;
			}

			if (openlinkCall2 != null && openlinkCall.getCallID().equals(openlinkCall2.getCallID()))
			{
				busy2 = true;
			}
		}

		return  busy1 && busy2;
	}

	public int getActiveCalls()
	{
		Iterator it2 = openlinkCalls.values().iterator();
		int calls = 0;

		while( it2.hasNext() )
		{
			OpenlinkCall openlinkCall = (OpenlinkCall)it2.next();

			if (! "ConnectionCleared".equals(openlinkCall.getState()))
			{
				calls++;
			}
		}

		return calls;
	}

	public boolean getBusyStatus()
	{
		return (getActiveCalls() >= getMaxNumCalls());
	}

	public String getCallFWD() {
		return callFWD;
	}

	public void setCallFWD(String callFWD) {
		this.callFWD = callFWD;
	}

	public String getCallFWDDigits() {
		return callFWDDigits;
	}

	public void setCallFWDDigits(String callFWDDigits) {
		this.callFWDDigits = callFWDDigits;
	}

	public void setMaxNumCalls(int maxNumCalls) {
		this.maxNumCalls = maxNumCalls;
	}

	public int getMaxNumCalls() {
		return maxNumCalls;
	}

    public void logCall(OpenlinkCall openlinkCall, String domain, long site)
    {
		String callId =  openlinkCall.getCallID();
		String tscId = "openlink" + site + "." + domain;

		if ("CallMissed".equals(openlinkCall.getState()) || "ConnectionCleared".equals(openlinkCall.getState()))
		{
			Log.debug("writing call record " + callId);

			//CallLogger.getLogger().logCall(tscId, callId, getUser().getProfileName(), getInterestName(), openlinkCall.getState(), openlinkCall.direction, openlinkCall.creationTimeStamp, openlinkCall.getDuration(), openlinkCall.getCallerNumber(getInterest().getInterestType()), openlinkCall.getCallerName(getInterest().getInterestType()), openlinkCall.getCalledNumber(getInterest().getInterestType()), openlinkCall.getCalledName(getInterest().getInterestType()));

			Iterator it3 = getInterest().getUserInterests().values().iterator();

			while( it3.hasNext() )
			{
				OpenlinkUserInterest openlinkParticipant = (OpenlinkUserInterest)it3.next();

				OpenlinkCall participantCall = openlinkParticipant.getCallByLine(openlinkCall.getLine());

				if (participantCall != null)
				{
					Log.debug("writing call participant record " + callId + " " + openlinkParticipant.getUser().getUserId());

					if (("Active".equals(participantCall.firstParticipation) && "ConnectionCleared".equals(participantCall.getState())))
					{
						//CallLogger.getLogger().logParticipant(tscId, callId, openlinkParticipant.getUser().getUserId() + "@" + openlinkParticipant.getUser().getUserNo() + "." + domain, participantCall.direction, participantCall.firstParticipation, participantCall.firstTimeStamp, participantCall.getDuration());
					}

					if ("CallMissed".equals(participantCall.getState()))
					{
						//CallLogger.getLogger().logParticipant(tscId, callId, openlinkParticipant.getUser().getUserId() + "@" + openlinkParticipant.getUser().getUserNo() + "." + domain, participantCall.direction, participantCall.firstParticipation, participantCall.creationTimeStamp, participantCall.getRingDuration());
					}

				}
			}
		}
    }
}
