package org.red5.server.webapp.voicebridge;

import java.io.*;
import java.util.*;
import com.milgra.server.*;

import java.nio.IntBuffer;
import com.sun.voip.server.MemberReceiver;
import org.red5.codecs.asao.CodecImpl;

public class RtmpParticipant extends ThirdParty {

    public boolean createdPlayStream = false;
    public boolean startPublish = false;
    public Integer playStreamId;
    public Integer publishStreamId;
    public MemberReceiver memberReceiver;
    private String publishName;
    private String playName;
    private int kt = 0;
    private short kt2 = 0;

   	private float[] senderEncoderMap = new float[64];
   	private float[] recieverEncoderMap = new float[64];

    private static final int NELLYMOSER_CODEC_ID = 82;
    private static final int L16_AUDIO_LENGTH = 256;
    private static final int NELLY_AUDIO_LENGTH = 64;
    private static final int ULAW_AUDIO_LENGTH = 160;
    private static final int MAX_BUFFER_LENGTH = 1280;

    private final IntBuffer l16AudioSender = IntBuffer.allocate(MAX_BUFFER_LENGTH);
    private final IntBuffer l16AudioRecv = IntBuffer.allocate(MAX_BUFFER_LENGTH);

    private IntBuffer viewBufferSender;
    private IntBuffer viewBufferRecv;

    private int[] tempNellyBuffer = new int[L16_AUDIO_LENGTH];
    private final byte[] nellyBytes = new byte[NELLY_AUDIO_LENGTH];

	private long startTime = System.currentTimeMillis();

	private StreamRouter router;
	private StreamPlayer player;


    public RtmpParticipant(MemberReceiver memberReceiver)
    {
		this.memberReceiver = memberReceiver;
	}

    // ------------------------------------------------------------------------
    //
    // Overide
    //
    // ------------------------------------------------------------------------


    @Override
	public void push ( byte[] stream )
	{
		dispatchEvent(stream);
	}

    // ------------------------------------------------------------------------
    //
    // Public
    //
    // ------------------------------------------------------------------------

    public void startStream(String publishName, String playName) {

        System.out.println( "RtmpParticipant startStream" );

		if (publishName == null || playName == null)
		{
			loggererror( "RtmpParticipant startStream stream names invalid " + publishName + " " + playName);

		} else {

			this.publishName = publishName;
			this.playName = playName;

			createdPlayStream = false;
			startPublish = false;

			kt = 0;
			kt2 = 0;

			recieverEncoderMap = new float[64];
			senderEncoderMap = new float[64];

			viewBufferSender = l16AudioSender.asReadOnlyBuffer();
			viewBufferRecv = l16AudioRecv.asReadOnlyBuffer();

			try {

				router = new StreamRouter( -1 , -1 , publishName , "live" , null );
				router.enable( );
				Server.registerRouter( router );
				Server.connectRouter( router , publishName);

				player = new StreamPlayer( -1, -1, -1, -1, playName,  null );
				player.thirdParty = this;
				player.enable( );
				Server.registerPlayer( player );
				Server.connectPlayer( player , playName );

			}
			catch ( Exception e ) {
				loggererror( "RtmpParticipant startStream exception " + e );
			}
		}
    }


    public void stopStream() {

        System.out.println( "RtmpParticipant stopStream" );

        try {
			router.disable( );
			router.close( );

			player.thirdParty = null;
			player.disable( );
			player.close( );
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



	public void pushAudio(int[] pcmBuffer)
	{
		int timeStamp = 0;

		try {
			l16AudioSender.put(pcmBuffer);

			if ((l16AudioSender.position() - viewBufferSender.position()) >= L16_AUDIO_LENGTH)
			{
				// We have enough L16 audio to generate a Nelly audio.
				// Get some L16 audio

				viewBufferSender.get(tempNellyBuffer);

				// adjust volume
				//normalize(tempNellyBuffer);

				// Convert it into Nelly

				CodecImpl.encode(senderEncoderMap, tempNellyBuffer, nellyBytes);

				// Having done all of that, we now see if we need to send the audio or drop it.
				// We have to encode to build the encoderMap so that data from previous audio packet
				// will be used for the next packet.

				boolean sendPacket = true;
/*
				IConnection conn = Red5.getConnectionLocal();

				if (conn instanceof RTMPMinaConnection) {
					long pendingMessages = ((RTMPMinaConnection)conn).getPendingMessages();

					if (pendingMessages > 25) {
						// Message backed up probably due to slow connection to client (25 messages * 20ms ptime = 500ms audio)
						sendPacket = false;
						System.out.println(String.format("Dropping packet. Connection %s congested with %s pending messages (~500ms worth of audio) .", conn.getClient().getId(), pendingMessages));
					}
				}
*/
				if (sendPacket) {

					if (kt == 0)
					{
						startTime = System.currentTimeMillis();
						timeStamp = 0;

					} else {

						timeStamp = (int)(System.currentTimeMillis() - startTime);
					}

					publish(NELLY_AUDIO_LENGTH, nellyBytes, timeStamp, NELLYMOSER_CODEC_ID);

					if ( kt < 10 ) {
						System.out.println( "++++ RtmpParticipant.pushAudio() - dataToSend -> length = " + nellyBytes.length);
					}

					kt++;
				}
			}

		} catch (Exception e) {

            loggererror( "RtmpParticipant pushAudio exception " + e );
		}

        if (l16AudioSender.position() == l16AudioSender.capacity()) {
        	// We've processed 8 Ulaw packets (5 Nelly packets), reset the buffers.
        	l16AudioSender.clear();
        	viewBufferSender.clear();
        }
	}


   	public int[] normalize(int[] audio)
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

    // ------------------------------------------------------------------------
    //
    // Privates
    //
    // ------------------------------------------------------------------------

    private void publish(int len, byte[] audio, int ts, int codec)
    {
		byte[] bytes = new byte[len + 1];
		bytes[0] = (byte) codec;
		System.arraycopy(audio, 0, bytes, 1, len);

		RtmpPacket packet = new RtmpPacket();
		packet.bodyType = 0x08;
		packet.rtmpChannel = 0x05;
		packet.flvChannel = 0;
		packet.flvStamp = ts;
		packet.body = bytes;
		router.take(packet);

	}

    private void loggerdebug( String s ) {

        System.out.println( s );
    }

    private void loggererror( String s ) {

        System.err.println( "[ERROR] " + s );
    }


	private void dispatchEvent(byte[] asaoInput)
	{
		int[] tempL16Buffer = new int[L16_AUDIO_LENGTH];
		int[] l16Buffer = new int[ULAW_AUDIO_LENGTH];

		CodecImpl.decode(recieverEncoderMap, asaoInput, tempL16Buffer);

		l16AudioRecv.put(tempL16Buffer);	// Store the L16 audio into the buffer

		viewBufferRecv.get(l16Buffer);		// Read 160-int worth of audio
		sendToBridge(l16Buffer);

		if (l16AudioRecv.position() == l16AudioRecv.capacity())
		{
			/**
			 *  This means we already processed 5 Nelly packets and sent 5 Ulaw packets.
			 *  However, we have 3 extra Ulaw packets.
			 *  Fire them off to the bridge. We don't want to discard them as it will
			 *  result in choppy audio.
			 */

			for (int i=0; i<3; i++)
			{
				viewBufferRecv.get(l16Buffer);
				sendToBridge(l16Buffer);
			}

			// Reset the buffer's position back to zero and start over.

			l16AudioRecv.clear();
			viewBufferRecv.clear();
		}

	}

	private void sendToBridge(int[] encodingBuffer)
	{
		try {

			if (memberReceiver != null ) {

				memberReceiver.handleRTMPMedia(encodingBuffer, kt2);

				if ( kt2 < 10 ) {
					loggerdebug( "*** " + encodingBuffer.length );
				}

				kt2++;
			}
		}
		catch ( Exception e ) {
			loggererror( "RtmpParticipant => sendToBridge error " + e );
			e.printStackTrace();
		}
	}
}
