package org.red5.server.webapp.voicebridge;

import java.io.*;
import java.util.*;
import com.milgra.server.*;

import java.nio.*;
import com.sun.voip.server.MemberReceiver;
import com.sun.voip.AudioConversion;

public class RtmpParticipant extends ThirdParty {

    public MemberReceiver memberReceiver;
    protected String publishName;
    protected String playName;
    protected int kt = 0;
    protected int kt2 = 0;
    protected short counter = 0;

    protected static final int ULAW_CODEC_ID = 130;

	protected long startTime = 0;
	protected StreamRouter router = null;
	protected StreamPlayer player = null;

	private Red5AudioHandler rtmpHandler = null;
	public static Red5Container handler = null;

    public RtmpParticipant(MemberReceiver memberReceiver)
    {
		this.memberReceiver = memberReceiver;

		if (handler != null)
		{
			this.rtmpHandler = handler.createRed5AudioHandler();
			this.rtmpHandler.setRtmpParticipant(this);
		}

	}

    // ------------------------------------------------------------------------
    //
    // Overide
    //
    // ------------------------------------------------------------------------


    @Override public void push ( byte[] stream )
	{
		try {

			if (memberReceiver != null && stream.length > 0)
			{
				int[] l16Buffer = new int[stream.length - 1];
				AudioConversion.ulawToLinear(stream, 1, stream.length - 1, l16Buffer);

				l16Buffer = normalize(l16Buffer);
				counter++;

				memberReceiver.handleRTMPMedia(l16Buffer, counter);

				if ( kt2 < 10 )
				{
					loggerdebug( "**** RtmpParticipant.push() - dataRecieved -> length = " + stream.length);
				}

			}
		}
		catch ( Exception e ) {
			loggererror( "RtmpParticipant => push error " + e );
			e.printStackTrace();
		}

		kt2++;
	}

    // ------------------------------------------------------------------------
    //
    // Public
    //
    // ------------------------------------------------------------------------

     @Override public void startStream(String publishName, String playName) {

        System.out.println( "RtmpParticipant startStream" );

		if (publishName == null || playName == null)
		{
			loggererror( "RtmpParticipant startStream stream names invalid " + publishName + " " + playName);

		} else {

			this.publishName = publishName;
			this.playName = playName;

			kt = 0;
			kt2 = 0;
			counter = 0;
         	startTime = 0;

			try {

				if (rtmpHandler != null)
				{
					rtmpHandler.startStream(publishName, playName);

				} else {

					router = new StreamRouter(-1, -1, publishName, "live", null);
					router.enable( );
					Server.registerRouter( router );
					Server.connectRouter( router , publishName);
					router.publishNotify();

					player = new StreamPlayer(-1, -1, -1, -1, playName,  null );
					player.thirdParty = this;
					player.enable( );

					Server.registerPlayer( player );
					Server.connectPlayer( player , playName );
				}

			}
			catch ( Exception e ) {
				loggererror( "RtmpParticipant startStream exception " + e );
			}
		}
    }


    @Override public void stopStream()
    {

        System.out.println( "RtmpParticipant stopStream" );

        startTime = 0;

        try {

			if (rtmpHandler != null)
			{
				rtmpHandler.stopStream();

			} else {
				router.unPublishNotify();
				router.disable( );
				router.close( );
				router = null;

				player.thirdParty = null;
				player.disable( );
				player.close( );
				player = null;
			}
        }
        catch ( Exception e ) {
            loggererror( "RtmpParticipant stopStream exception " + e );
        }

    }

    // ------------------------------------------------------------------------
    //
    // Implementations
    //
    // ------------------------------------------------------------------------



	@Override public void pushAudio(int[] pcmBuffer)
	{
		if ( kt < 10 )
		{
			loggerdebug( "++++ RtmpParticipant.pushAudio() - dataSent -> length = " + pcmBuffer.length);
		}

		try {
			pcmBuffer = normalize(pcmBuffer);

			byte[] packetBody = new byte[pcmBuffer.length + 1];
			packetBody[0] = (byte) ULAW_CODEC_ID;

			AudioConversion.linearToUlaw(pcmBuffer, packetBody, 1);

			if (startTime == 0) startTime = System.currentTimeMillis();
			int ts = (int)(System.currentTimeMillis() - startTime);

			if (rtmpHandler != null)
			{
				rtmpHandler.pushAudio(packetBody, (kt == 0) ? 0: ts);

			} else {

				if (router == null || pcmBuffer.length < 160) return;

				RtmpPacket packet = new RtmpPacket();
				packet.bodyType = 0x08;
				packet.flvStamp = (kt == 0) ? 0: ts;
				packet.first = (kt == 0);
				packet.bodySize = pcmBuffer.length + 1;
				packet.body = packetBody;

				router.take(packet);
			}

		} catch (Exception e) {

			loggererror( "RtmpParticipant pushAudio exception " + e );
		}

		kt++;
	}


    protected void loggerdebug( String s ) {

        System.out.println( s );
    }

    protected void loggererror( String s ) {

        System.err.println( "[ERROR] " + s );
    }

   	protected int[] normalize(int[] audio)
   	{
	    // Scan for max peak value here
	    float peak = 0;
		for (int n = 0; n < audio.length; n++)
		{
			int val = Math.abs(audio[n]);
			if (val > peak)
			{
				peak = val;
			}
		}

		// Peak is now the loudest point, calculate ratio
		float r1 = 32768 / peak;

		// Don't increase by over 500% to prevent loud background noise, and normalize to 75%
		float ratio = Math.min(r1, 5) * .75f;

		for (int n = 0; n < audio.length; n++)
		{
			audio[n] *= ratio;
		}

		return audio;
   	}

}
