package org.red5.server.webapp.voicebridge;


public interface Red5AudioHandler {

    public void pushAudio(byte[] audioData, int timestamp);
    public void startStream(String stream1, String stream2);
    public void stopStream();
    public void setRtmpParticipant(RtmpParticipant rtmpParticipant);
}
