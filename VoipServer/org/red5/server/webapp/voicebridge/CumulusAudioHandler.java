package org.red5.server.webapp.voicebridge;


public interface CumulusAudioHandler {

    void handleAudioData(String stream, byte[] audioData, int timestamp);
    void startPublisher(String stream);
    void stopPublisher(String stream);
}
