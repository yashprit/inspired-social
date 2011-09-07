package org.red5.server.webapp.voicebridge;

import java.util.LinkedList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FilePermission;
import java.util.PropertyPermission;

public class CRJitterManager extends Thread
{
	//Impl
	public static int DEFAULT_MIN_JITTER_BUFFER_SIZE = 3; // packets
	public static int DEFAULT_MAX_JITTER_BUFFER_SIZE = 9;

	private int minJitterBufferSize = DEFAULT_MIN_JITTER_BUFFER_SIZE;
    private int maxJitterBufferSize = DEFAULT_MAX_JITTER_BUFFER_SIZE;

    private int jitter;
    private int maxJitter;

    private int elapsed;
    private String id;

    private int adaptionRate = 2;
    //private int initialBufferSize = 6;
    private int calldroplimit = 5;


	private RtmpParticipant rtmpUser;

	public boolean running;

	private LinkedList<JitterObject> dataBuffer;

   	private long timeADD_CurrentPacket;
   	private long timeADD_PreviousPacket;
   	private long timeADD_Interval;

   	private long timeSND_CurrentPacket;

   	private long timeSND_Interval_i;
   	private long timeSND_Interval_Min_i;
   	private long timeSND_Interval_Max_i;
   	private float timeSND_Interval_f;

   	private long TOSS_packetCounter;
   	private long ADD_packetCounter;
   	private long SND_packetCounter;
   	private long BFR_PacketCounter;
   	private long BLK_PacketCounter;

   	private boolean firstPacket;

   	private int RTPPACKETPERIOD = 20;

   	private long processedSequence = 0;

   	private long latestSequence = 0;

   	private JitterObject silence;

   	//Methods
   	public CRJitterManager( RtmpParticipant rtmpUser)
    {
        init( rtmpUser);
    }

    private void init( RtmpParticipant rtmpUser )
    {
        this.rtmpUser = rtmpUser;
        InitializeJitterBuffering();
    }

    public void InitializeJitterBuffering()
    {
    	this.running = false;
    	this.firstPacket = true;
        this.timeADD_CurrentPacket = 0;
        this.timeADD_PreviousPacket = 0;
        this.timeADD_Interval = 0;

        this.timeSND_CurrentPacket = 0;
        this.timeSND_Interval_i = 0;
        this.timeSND_Interval_f = 0;

        this.timeSND_Interval_Min_i = Long.MAX_VALUE;
        this.timeSND_Interval_Max_i = Long.MIN_VALUE;

        this.TOSS_packetCounter = 0;
        this.ADD_packetCounter = 0;
        this.SND_packetCounter = 0;
        this.BFR_PacketCounter = 0;
        this.BLK_PacketCounter = 0;

        this.dataBuffer = new LinkedList<JitterObject>();
    }

    int packetcount = 0;
	long totalInterval = 0;
	private void updatePacketInterval(long Interval)
	{
		if(Interval < 0)
		{
			//Bad interval
		}
		else
		{
			packetcount++;
			totalInterval += Interval;
			if(packetcount == 5)
			{
				Interval = Math.round(((float)totalInterval/5.0f));
				packetcount = 0;
				totalInterval = 0;

				float diff = (float)Interval - timeSND_Interval_f;
				timeSND_Interval_f += (diff/(float)adaptionRate);
				timeSND_Interval_i = Math.round(timeSND_Interval_f);
				timeSND_Interval_Min_i = Math.min(timeSND_Interval_Min_i, timeSND_Interval_i);
				timeSND_Interval_Max_i = Math.max(timeSND_Interval_Max_i, timeSND_Interval_i);
			}
		}
	}

	boolean commented = false;

	public void addPacket(byte[] encodedstream, long sequenceNumber)
	{
		try
		{
			if(sequenceNumber > 0)
			{
				if(processedSequence > sequenceNumber)
				{
					//Packet before this sent, ignore current packet
					TOSS_packetCounter++;
				}
				else
				{
					if(latestSequence < sequenceNumber)
					{
						//println("%\r\n"+"Adding packet " + sequenceNumber + " to end of buffer");

						//new packet to the end
						JitterObject jitterObject = new JitterObject(encodedstream, sequenceNumber, System.currentTimeMillis());
						synchronized (dataBuffer)
						{
							if(dataBuffer.size() >= maxJitterBufferSize)
							{
								dataBuffer.remove(0);
							}
							dataBuffer.add(jitterObject);
							latestSequence = sequenceNumber;

						}
						BFR_PacketCounter++;
					}
					else
					{
						//println("%\r\n"+"Adding packet " + sequenceNumber + " to mid of buffer");

						//packet needs to be inserted in between
						JitterObject jitterObject = new JitterObject(encodedstream, sequenceNumber, System.currentTimeMillis());
						synchronized (dataBuffer)
						{
							for(int i=0;i<dataBuffer.size();i++)
							{
								if(sequenceNumber < dataBuffer.get(i).packetnumber)
								{
									if(dataBuffer.size() >= maxJitterBufferSize)
									{
										dataBuffer.remove(0);
									}
									dataBuffer.add(i, jitterObject);
									BFR_PacketCounter++;
									break;
								}
							}
						}

					}

					//Other updates
					long oldinterval_i= 0;
					float oldinterval_f= 0;

					timeADD_CurrentPacket = System.currentTimeMillis();

					if(firstPacket)
					{
						timeADD_Interval = 0;
						timeSND_Interval_i = 0;
						timeSND_Interval_f = 0;
						firstPacket = false;
						if(commented)
						{
							println("%\r\n"+ "Buffer Initialisation--" + ADD_packetCounter + " - Packet Received" + "  Time Interval-" + timeADD_Interval +"\r\n%");
						}
					}
					else if(!running)
					{
						timeADD_Interval = timeADD_CurrentPacket - timeADD_PreviousPacket;
						timeSND_Interval_f += timeADD_Interval;
						if(commented)
						{
							println("%\r\n"+ "Buffer Initialisation--" + ADD_packetCounter + " - Packet Received" + "  Time Interval-" + timeADD_Interval +"\r\n%");
						}
					}
					else
					{
						oldinterval_i = timeSND_Interval_i;
						oldinterval_f = timeSND_Interval_f;
						timeADD_Interval = timeADD_CurrentPacket - timeADD_PreviousPacket;
						updatePacketInterval(timeADD_Interval);
						if(commented)
						{
							println("%\r\n"+
												"--------------------Adding Packet Start--------------------" +"\r\n"+
												"Added Packet No = "+ ADD_packetCounter + "\r\n" +
												"Time taken for packet to arrive = "+ timeADD_Interval + "\r\n" +
												"Previous time interval int = "+ oldinterval_i +"\r\n" +
												"Previous time interval float = "+ oldinterval_f +"\r\n" +
												"Current time interval int = "+ timeSND_Interval_i +"\r\n" +
												"Current time interval float = "+ timeSND_Interval_f +"\r\n" +
												"--------------------Adding Packet End--------------------" +"\r\n%");
						}
					}
					timeADD_PreviousPacket = timeADD_CurrentPacket;
					if(!running && BFR_PacketCounter == minJitterBufferSize)
					{
						this.running = true;
						float temptotal = timeSND_Interval_f;
						timeSND_Interval_f = (timeSND_Interval_f / (float)(minJitterBufferSize - 1));
						timeSND_Interval_i =  Math.round(timeSND_Interval_f);
						if(commented)
						{
							println("%\r\n"+
												"Total Time Interval of 20 Packets = " + temptotal + "\r\n" +
												"Initial Buffer Size = " + minJitterBufferSize + "\r\n" +
												"Current time interval int = "+ timeSND_Interval_i +"\r\n" +
												"Current time interval float = "+ timeSND_Interval_f +"\r\n" +
												"Buffer Initialisation Commanded to Initialized" + "\r\n"+"\r\n%");
						}
						start();
					}
				}
			}
		}
		catch(Exception e)
		{
			println( "$JitterManager - ", " Unable to start audio pushing thread $" );
		}
	}

    public void run()
	{
		try
		{
			long currentthread = System.currentTimeMillis();
			println("&\r\n"+
								"---------------------------------------------------------------" +"\r\n"+
								"---------------------------------------------------------------" +"\r\n"+
								"---------------------------------------------------------------" +"\r\n"+"\r\n"+
								"Jiiter Manager started in Second Thread"+ "\r\n" + "\r\n"+
								"---------------------------------------------------------------" +"\r\n"+
								"---------------------------------------------------------------" +"\r\n"+
								"---------------------------------------------------------------" +"\r\n"+"\r\n&");


			long timeConsumed = 0;
			long timeRemaining = 0;
		    long SendingTimestamp = 0;
		    long startTime = System.currentTimeMillis();
		    long bufferdelay = 0;
		    int conspacketDrop = 0;
		    boolean notempty;
			while (running)
			{
				try
				{
					JitterObject jitterobj = new JitterObject();

					timeSND_CurrentPacket = System.currentTimeMillis();

					notempty = false;

					SND_packetCounter++;

					synchronized (dataBuffer)
					{
						if(!dataBuffer.isEmpty())
						{
							jitterobj = dataBuffer.removeFirst();
							notempty = true;
						}
					}

					if(notempty)
					{
						//println("%\r\n"+"Playing packet " + jitterobj.packetnumber);
						//Play audio
						SendingTimestamp = timeSND_CurrentPacket - startTime;

						rtmpUser.sendAudio(jitterobj.data, SendingTimestamp);
						processedSequence = jitterobj.packetnumber;
						bufferdelay = timeSND_CurrentPacket - jitterobj.receptionTime;
						BFR_PacketCounter--;
						conspacketDrop = 0;

						timeConsumed = System.currentTimeMillis() - timeSND_CurrentPacket;
						timeRemaining = timeSND_Interval_i - timeConsumed;
						if(timeRemaining >0)
						{
							try
							{
								Thread.sleep(timeRemaining);
							}
							catch(Exception e)
							{
								println( "run", "threading error" );
							}
						}
					}
					else
					{
						//Play silence
						//SendingTimestamp = timeSND_CurrentPacket - startTime;

						//rtmpUser.pushAudio(NELLYMOSER_ENCODED_PACKET_SIZE, null, SendingTimestamp, 82);

						//here
						processedSequence++;
						BLK_PacketCounter++;
						conspacketDrop++;
						//loggingThread = new LoggingThread(0,SND_packetCounter);
						//loggingThread.start();
						if(commented)
						{
							println("&\r\n"+"\r\n"+
													"--------------------Sending Packet Start--------------------"  +"\r\n" +"\r\n"+
													"No packet to sent Buffer Empty"  +"\r\n"+
													"Received Packet Count = "+ ADD_packetCounter  +"\r\n"+
													"Sent Packet Count = "+ SND_packetCounter  +"\r\n"+
													"Lost Packet Count = "+ BLK_PacketCounter  +"\r\n"+
													"Buffer Packet Count = "+ BFR_PacketCounter  +"\r\n"+
													"Time slept after Processing = "+ timeSND_Interval_i +"\r\n"+ "\r\n" +
													"--------------------Sending Packet End--------------------"  +"\r\n" +"\r\n&");

						}
						/*if(conspacketDrop > calldroplimit)
						{
							this.running = false;
							println("&\r\nCall Ended Due To Continuous Packet Loss&");
						}
						try
						{
							Thread.sleep(timeSND_Interval_i);
						}
						catch(Exception e)
						{
							println( "run", "threading error" );
						}*/
					}
				}
				catch(Exception e)
				{
					println( "CR Jitter While Exception", "***********CR Jitter Manager While Exception:" + e.getMessage() + "///////////" + e.getStackTrace());
				}
			}
		}
		catch(Exception e)
		{
			println( "CR JitterException", "***********CR Jitter Manager Exception:" + e.getMessage() + "///////////" + e.getStackTrace());
		}
	}

	public void halt()
	{
		running = false;
		long currentthread = Thread.currentThread().getId();
        println("%\r\n"+
                "-- From Thread No ="+ currentthread +" ----No of RTP Packets Added-----------------------" +"\r\n" +
             	"No of RTP Packets Added = "+ ADD_packetCounter  +"\r\n" +
             	"Packet Add Interval Current = "+ timeSND_Interval_i  +"\r\n" +
             	"Packet Add Interval Maximum = "+ timeSND_Interval_Max_i  +"\r\n" +
             	"Packet Add Interval Minimum = "+ timeSND_Interval_Min_i  +"\r\n" +
                "--------------------------No of RTP Packets Added--------------------------------" +"\r\n%");

        println("&\r\n"+
        		"-- From Thread No ="+ currentthread +" ----No of RTP Packets Sent-------------------------------" +"\r\n" +
             	"No of RTP Packets Sent = "+ SND_packetCounter  +"\r\n" +
             	"No of RTP Packets Blank = "+ BLK_PacketCounter  +"\r\n" +
             	"No of RTP Packets In Buffer = "+ BFR_PacketCounter  +"\r\n" +
                "--------------------------No of RTP Packets Sent--------------------------------" +"\r\n&");

        try
        {
        	println(Debugdata,"");
        }
        catch(Exception e)
        {
        	println("Debug Write Error:", e.toString());
        }

		firstPacket = true;
		ADD_packetCounter = 0;
		SND_packetCounter = 0;
		BFR_PacketCounter = 0;
		Debugdata = "";
	}




	String Debugdata = "";

	public void println( String method)
	{
		try
        {
			this.Debugdata += method;

			if(this.Debugdata.length() > 1000)
			{
				println(this.Debugdata, "Testing in between");
				this.Debugdata = "";
			}
        }
        catch(Exception e)
        {
        	println("Debug Add Error:", e.toString());
        }
	}

    private static void println( String method, String message )
    {
        System.out.println( "JitterManager - " + method + " -> " + message );
    }
}
