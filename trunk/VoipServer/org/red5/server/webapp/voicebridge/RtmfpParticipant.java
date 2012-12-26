package org.red5.server.webapp.voicebridge;

import java.io.*;
import java.util.*;

import java.nio.*;
import com.sun.voip.server.MemberReceiver;
import com.sun.voip.AudioConversion;
import com.jcumulus.server.rtmfp.application.Publication;
import com.jcumulus.server.rtmfp.packet.AudioPacket;
import com.jcumulus.server.rtmfp.d.E;

public class RtmfpParticipant extends RtmpParticipant {

	public static Map<String, Publication> publishHandlers = Collections.synchronizedMap( new HashMap<String, Publication>());
	public static Map<String, RtmfpParticipant> playHandlers = Collections.synchronizedMap( new HashMap<String, RtmfpParticipant>());

    public RtmfpParticipant(MemberReceiver memberReceiver)
    {
		super(memberReceiver);
	}

    // ------------------------------------------------------------------------
    //
    // Overide
    //
    // ------------------------------------------------------------------------


    public void push ( byte[] stream )
	{

		try {

			if (memberReceiver != null && stream.length > 0)
			{
				int[] l16Buffer = new int[stream.length - 1];
				AudioConversion.ulawToLinear(stream, 1, stream.length - 1, l16Buffer);

				l16Buffer = normalize(l16Buffer);

				memberReceiver.handleRTMPMedia(l16Buffer, counter++);

				if ( kt2 < 10 )
				{
					loggerdebug( "**** RtmfpParticipant.push() - dataRecieved -> length = " + stream.length + " " + playName);
				}

			}
		}
		catch ( Exception e ) {
			loggererror( "RtmfpParticipant => push error " + e );
			e.printStackTrace();
		}

		kt2++;
	}


    @Override public void startStream(String publishName, String playName) {

        System.out.println( "RtmfpParticipant startStream" );

		if (publishName == null || playName == null)
		{
			loggererror( "RtmfpParticipant startStream stream names invalid " + publishName + " " + playName);

		} else {

			this.publishName = publishName;
			this.playName = playName;

			playHandlers.put(playName, this);

			kt = 0;
			kt2 = 0;
			counter = 0;
         	startTime = System.currentTimeMillis();

		}
    }


    @Override public void stopStream()
    {

        System.out.println( "RtmfpParticipant stopStream" );

        try {
			playHandlers.remove(playName);
			publishHandlers.remove(publishName);
        }
        catch ( Exception e ) {
            loggererror( "RtmfpParticipant stopStream exception " + e );
        }

    }



	@Override public void pushAudio(int[] pcmBuffer)
	{
		if (pcmBuffer.length < 160) return;

		try {
			pcmBuffer = normalize(pcmBuffer);

			int ts = (int)(System.currentTimeMillis() - startTime);

			byte[] packetBody = new byte[pcmBuffer.length + 1];
			packetBody[0] = (byte) ULAW_CODEC_ID;

			AudioConversion.linearToUlaw(pcmBuffer, packetBody, 1);

			if (RtmfpParticipant.publishHandlers.containsKey(publishName) )
			{
				if ( kt < 10 )
				{
					loggerdebug( "++++ RtmfpParticipant.pushAudio() - dataSent -> length = " + pcmBuffer.length + " " + publishName);
				}

				RtmfpParticipant.publishHandlers.get(publishName).B(ts, new AudioPacket(packetBody,  packetBody.length), 0);
			}

		} catch (Exception e) {

			loggererror( "RtmfpParticipant pushAudio exception " + e );
		}

		kt++;
	}
}
