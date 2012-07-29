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

import org.red5.server.webapp.voicebridge.Config;
import org.red5.server.webapp.voicebridge.Application;

import com.ifsoft.cti.*;

public class JingleOutgoingCallAgent extends CallSetupAgent
{
    private SipUtil sipUtil;
    private CallParticipant cp;
    private MediaInfo mixerMediaPreference;
    private MemberReceiver memberReceiver;
    private MemberSender memberSender;
    private OpenlinkComponent component = null;

    public JingleOutgoingCallAgent(CallHandler callHandler)
    {
		super(callHandler);
		cp = callHandler.getCallParticipant();

        MediaInfo mixerMediaPreference = callHandler.getConferenceManager().getMediaInfo();

		Logger.println("JingleIncomingCallAgent:  media preference " + mixerMediaPreference);

		memberSender = callHandler.getMemberSender();
		memberReceiver = callHandler.getMemberReceiver();
	}

	public void initiateCall() throws IOException
	{
		Logger.println("JingleOutgoingCallAgent initiateCall " + cp);

		try {

			component = (OpenlinkComponent) Application.component;

			if (component != null)
			{
				InetSocketAddress isaLocal = callHandler.getReceiveAddress();

				JinglePayload localPayload = new JinglePayload("0", "PCMU", "8000", String.valueOf(isaLocal.getPort()), isaLocal.getHostName());
				component.sendJingleAction("session-invite", cp, localPayload);

				setState(CallState.INVITED);
			}


		} catch (Exception e) {

			Logger.println("Call " + cp + ":  JingleOutgoingCallAgent: initiateCall exception " + e);
		}
	}


	public void callAccepted(JinglePayload jinglePayload)
	{
		String remoteHost = jinglePayload.remoteIP;
		int remotePort = Integer.parseInt(jinglePayload.remotePort);
		byte codec = Byte.parseByte(jinglePayload.codecId);

		Logger.println("JingleOutgoingCallAgent callAccepted " + cp + ": remote socket " + remoteHost + " " + remotePort);

		InetSocketAddress isaRemote = new InetSocketAddress(remoteHost, remotePort);
		setEndpointAddress(isaRemote, codec, codec, (byte)0);

		setState(CallState.ANSWERED);

		setState(CallState.ESTABLISHED);
    }


    public void terminateCall()
    {
		Logger.println("JingleOutgoingCallAgent terminateCall " + cp);

		if (component != null)
		{
			component.sendJingleTerminate(cp);
		}
    }

}
