package com.ifsoft.cti;

public class JinglePayload
{
	public String codecId;
	public String codecName;
	public String codecClock;
	public String remotePort;
	public String remoteIP;

	public JinglePayload(String codecId, String codecName, String codecClock, String remotePort, String remoteIP)
	{
		this.codecId = codecId;
		this.codecName = codecName;
		this.codecClock = codecClock;
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
	}
}
