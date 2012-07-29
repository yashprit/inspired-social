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

public class JingleIncomingCallAgent extends CallSetupAgent
{
    private CallParticipant cp;
    private MediaInfo mixerMediaPreference;
    private MemberReceiver memberReceiver;
    private MemberSender memberSender;
    private JinglePayload jinglePayload;
    private OpenlinkComponent component = null;

	// codecId, remotePort, remoteIP

    public JingleIncomingCallAgent(CallHandler callHandler, Object jingle)
    {
		super(callHandler);

        MediaInfo mixerMediaPreference = callHandler.getConferenceManager().getMediaInfo();

		Logger.println("JingleIncomingCallAgent:  media preference " + mixerMediaPreference);

		jinglePayload = (JinglePayload) jingle;
		component = (OpenlinkComponent) Application.component;

		if (component != null)
		{
			cp = callHandler.getCallParticipant();

			Logger.println("JingleIncomingCallAgent:  call participant " + cp);

			memberSender = callHandler.getMemberSender();
			memberReceiver = callHandler.getMemberReceiver();

			setState(CallState.ANSWERED);

			try {
				//Thread.sleep(500);

				String remoteHost = jinglePayload.remoteIP;
				int remotePort = Integer.parseInt(jinglePayload.remotePort);
				byte codec = Byte.parseByte(jinglePayload.codecId);

				Logger.println("Call " + cp + ":  JingleIncomingCallAgent:  remote socket " + remoteHost + " " + remotePort);

				InetSocketAddress isaRemote = new InetSocketAddress(remoteHost, remotePort);
				setEndpointAddress(isaRemote, codec, codec, (byte)0);

				InetSocketAddress isaLocal = callHandler.getReceiveAddress();

				JinglePayload localPayload = new JinglePayload(jinglePayload.codecId, jinglePayload.codecName, jinglePayload.codecClock, String.valueOf(isaLocal.getPort()), isaLocal.getAddress().getHostAddress());

				setState(CallState.ESTABLISHED);

				component.sendJingleAction("session-accept", cp, localPayload);


			} catch (Exception e) {

				Logger.println("JingleIncomingCallAgent exception " + e);
				e.printStackTrace();
				return;
			}
		}
	}


    public void terminateCall()
    {
		Logger.println("JingleIncomingCallAgent terminateCall " + cp);

		if (component != null)
		{
			component.sendJingleTerminate(cp);
		}
    }

}
