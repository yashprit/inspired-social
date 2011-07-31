package org.red5.server.webapp.voicebridge;

import java.io.*;
import java.util.*;
import com.milgra.server.*;

import java.nio.*;
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
	private boolean sentMetadata = false;
   	private float[] senderEncoderMap = new float[64];
   	private float[] recieverEncoderMap = new float[64];

	private final byte[] fakeMetadata = new byte[] {
		0x02, 0x00, 0x0a, 0x6f, 0x6e, 0x4d, 0x65, 0x74, 0x61, 0x44, 0x61, 0x74, 0x61, 0x08, 0x00, 0x00,
		0x00, 0x06, 0x00, 0x08, 0x64, 0x75, 0x72, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x00, 0x40, 0x31, (byte)0xaf,
		0x5c, 0x28, (byte)0xf5, (byte)0xc2, (byte)0x8f, 0x00, 0x0f, 0x61, 0x75, 0x64, 0x69, 0x6f, 0x73, 0x61, 0x6d, 0x70,
		0x6c, 0x65, 0x72, 0x61, 0x74, 0x65, 0x00, 0x40, (byte)0xe5, (byte)0x88, (byte)0x80, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x0f, 0x61, 0x75, 0x64, 0x69, 0x6f, 0x73, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x73, 0x69, 0x7a, 0x65,
		0x00, 0x40, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x73, 0x74, 0x65, 0x72, 0x65,
		0x6f, 0x01, 0x00, 0x00, 0x0c, 0x61, 0x75, 0x64, 0x69, 0x6f, 0x63, 0x6f, 0x64, 0x65, 0x63, 0x69,
		0x64, 0x00, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x66, 0x69, 0x6c, 0x65,
		(byte)0xc8, 0x73, 0x69, 0x7a, 0x65, 0x00, 0x40, (byte)0xf3, (byte)0xf5, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
	};

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

	private long startTime = 0;

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
        	sentMetadata = false;
        	startTime = 0;

			recieverEncoderMap = new float[64];
			senderEncoderMap = new float[64];

       		l16AudioSender.clear();
       		l16AudioRecv.clear();

			viewBufferSender = l16AudioSender.asReadOnlyBuffer();
			viewBufferSender.clear();

			viewBufferRecv = l16AudioRecv.asReadOnlyBuffer();
			viewBufferRecv.clear();

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

		kt = 0;
		kt2 = 0;

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
		if ( kt < 10 ) {
			System.out.println( "++++ RtmpParticipant.pushAudio() - dataRecieved -> length = " + pcmBuffer.length);
		}

		kt++;

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

                //byte[] aux = ResampleUtils.resample((float) ( 8.0 / 11.025 ), tempNellyBuffer );

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

					if (startTime == 0)
					{
						startTime = System.currentTimeMillis();
					}

					publish(NELLY_AUDIO_LENGTH, nellyBytes, NELLYMOSER_CODEC_ID);

					//publish(aux.length, aux,  6);
				}
			}

		} catch (Exception e) {

            loggererror( "RtmpParticipant pushAudio exception " + e );
		}

        if (l16AudioSender.position() >= l16AudioSender.capacity()) {
        	// We've processed 8 L16 packets (5 Nelly packets), reset the buffers.
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

    private void publish(int len, byte[] audio, int codec)
    {
		int ts = (int)(System.currentTimeMillis() - startTime);

		sendFakeMetadata(ts);

		byte[] bytes = new byte[len + 1];
		bytes[0] = (byte) codec;
		System.arraycopy(audio, 0, bytes, 1, len);

		//ByteBuffer bytesWriteTemp = ByteBuffer.allocate(len + 1);
		//ByteBuffer bytesReadTemp = bytesWriteTemp.asReadOnlyBuffer();

		//bytesWriteTemp.put((byte) codec);
		//bytesWriteTemp.put(audio);
		//bytesReadTemp.get(bytes);

		RtmpPacket packet = new RtmpPacket();
		packet.bodyType = 0x08;
		packet.rtmpChannel = 0x05;
		packet.flvChannel = 0;
		packet.flvStamp = ts;
		packet.body = bytes;

		router.take(packet);
	}


	private void sendFakeMetadata(int timestamp)
	{
		if (!sentMetadata)
		{
			/*
			 * Flash Player 10.1 requires us to send metadata for it to play audio.
			 */

			RtmpPacket packet = new RtmpPacket();
			packet.bodyType = 0x12;
			packet.rtmpChannel = 0x05;
			packet.flvChannel = 0;
			packet.flvStamp = timestamp;
			packet.body = fakeMetadata;

			router.take(packet);

			sentMetadata = true;
		}
	}

    private void loggerdebug( String s ) {

        System.out.println( s );
    }

    private void loggererror( String s ) {

        System.err.println( "[ERROR] " + s );
    }


	public void dispatchEvent(byte[] asaoIn)
	{
		int[] tempL16Buffer = new int[L16_AUDIO_LENGTH];
		int[] l16Buffer = new int[ULAW_AUDIO_LENGTH];

		byte[] asaoInput = new byte[asaoIn.length - 1];
		System.arraycopy(asaoIn, 1, asaoInput, 0, asaoIn.length - 1);

		//ByteBuffer asaoWriteTemp = ByteBuffer.allocate(asaoIn.length);
		//ByteBuffer asaoReadTemp = asaoWriteTemp.asReadOnlyBuffer();

		//asaoWriteTemp.put(asaoIn);
		//asaoReadTemp.position(1);
		//asaoReadTemp.get(asaoInput);

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
