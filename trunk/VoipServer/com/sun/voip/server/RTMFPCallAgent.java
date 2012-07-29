/*
 * Copyright 2007 Sun Microsystems, Inc.
 *
 * This file is part of jVoiceBridge.
 *
 * jVoiceBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation and distributed hereunder
 * to you.
 *
 * jVoiceBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied this
 * code.
 */

package com.sun.voip.server;

import com.sun.voip.CallParticipant;
import com.sun.voip.CallState;
import com.sun.voip.Logger;
import com.sun.voip.MediaInfo;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.red5.server.webapp.voicebridge.RtmfpParticipant;
import org.red5.server.webapp.voicebridge.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RTMFPCallAgent extends CallSetupAgent
{
    private SipUtil sipUtil;
    private CallParticipant cp;
    private MediaInfo mixerMediaPreference;
    private MemberReceiver memberReceiver;
    private MemberSender memberSender;
    private RtmfpParticipant rtmfpParticipant;
    private long conferenceStartTime;

	public static Map< String, RtmfpParticipant > rtmfpReceivers = new ConcurrentHashMap< String, RtmfpParticipant >();

    public RTMFPCallAgent(CallHandler callHandler)
    {

		super(callHandler);
		cp = callHandler.getCallParticipant();
		mixerMediaPreference = callHandler.getConferenceManager().getMediaInfo();
		conferenceStartTime = callHandler.getConferenceManager().getConferenceStartTime();

		memberSender = callHandler.getMemberSender();
		memberReceiver = callHandler.getMemberReceiver();

		rtmfpParticipant = new RtmfpParticipant(memberReceiver);
		rtmfpReceivers.put(cp.getRtmpRecieveStream(), rtmfpParticipant);
		rtmfpReceivers.put(cp.getRtmpSendStream(), rtmfpParticipant);

		memberSender.setRtmpParticipant(rtmfpParticipant);

		callHandler.setEndpointAddress(null, (byte)0, (byte)0, (byte)0);

	}

	public void initiateCall() throws IOException
	{
		Logger.println("RTMFPCallAgent initiateCall " + cp.getRtmpSendStream() + " " + cp.getRtmpRecieveStream());
		try {
			rtmfpParticipant.startStream(cp.getRtmpSendStream(), cp.getRtmpRecieveStream());

			setState(CallState.ESTABLISHED);

		} catch (Exception e) {

			Logger.println("Call " + cp + ":  RTMFPCallAgent: initiateCall exception " + e);
		}
	}

	public String getSdp()
	{
		return null;
    }

    public void setRemoteMediaInfo(String sdp)
    {
		return;
    }

    public void terminateCall()
    {
		rtmfpParticipant.stopStream();
		rtmfpReceivers.remove(cp.getRtmpRecieveStream());
		rtmfpReceivers.remove(cp.getRtmpSendStream());
    }

}
