package org.red5.server.webapp.voicebridge;

public class JitterObject
{
	public byte[] data;
	public long packetnumber;
	public long receptionTime;
	public JitterObject(byte[] data,long packetnumber,long receptionTime)
	{
		this.data = data;
		this.packetnumber = packetnumber;
		this.receptionTime = receptionTime;
	}
	public JitterObject()
	{

	}
}
