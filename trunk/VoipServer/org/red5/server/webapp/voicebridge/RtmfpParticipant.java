package org.red5.server.webapp.voicebridge;

import java.io.*;
import java.util.*;

import java.nio.*;
import com.sun.voip.server.MemberReceiver;
import com.sun.voip.AudioConversion;

public class RtmfpParticipant extends RtmpParticipant {

	public static CumulusAudioHandler handler = null;


    public RtmfpParticipant(MemberReceiver memberReceiver)
    {
		super(memberReceiver);
	}
    // ------------------------------------------------------------------------
    //
    // Overide
    //
    // ------------------------------------------------------------------------


    public void push ( byte[] stream, short timestamp )
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
					loggerdebug( "**** RtmfpParticipant.push() - dataRecieved -> length = " + stream.length);
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

			kt = 0;
			kt2 = 0;
			counter = 0;
         	startTime = System.currentTimeMillis();

			try {

				if (handler != null)
				{
					handler.startPublisher(publishName);
				}

			}
			catch ( Exception e ) {
				loggererror( "RtmfpParticipant startStream exception " + e );
			}
		}
    }


    @Override public void stopStream()
    {

        System.out.println( "RtmfpParticipant stopStream" );

        try {
			if (handler != null)
			{
				handler.stopPublisher(publishName);
			}
        }
        catch ( Exception e ) {
            loggererror( "RtmfpParticipant stopStream exception " + e );
        }

    }



	@Override public void pushAudio(int[] pcmBuffer)
	{
		if (pcmBuffer.length < 160) return;

		if ( kt < 10 )
		{
			loggerdebug( "++++ RtmfpParticipant.pushAudio() - dataSent -> length = " + pcmBuffer.length);
		}

		try {
			pcmBuffer = normalize(pcmBuffer);

			int ts = (int)(System.currentTimeMillis() - startTime);

			byte[] packetBody = new byte[pcmBuffer.length + 1];
			packetBody[0] = (byte) ULAW_CODEC_ID;

			AudioConversion.linearToUlaw(pcmBuffer, packetBody, 1);

			if (handler != null)
			{
				handler.handleAudioData(publishName, packetBody, ts);
			}

		} catch (Exception e) {

			loggererror( "RtmfpParticipant pushAudio exception " + e );
		}

		kt++;
	}
}
