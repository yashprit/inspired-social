/*
 * Copyright 2007 Sun Microsystems, Inc.
 *
 * This file is part of jVoiceBridge.
 *
 * jVoiceBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation and distributed hereunder
 * to you.
 *
 * jVoiceBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied this
 * code.
 */

package com.sun.stun;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.mc_cubed.icedjava.stun.StunPacketImpl;
import net.mc_cubed.icedjava.stun.StunReplyImpl;

import com.sun.voip.CallParticipant;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.packet.attribute.*;
import net.mc_cubed.icedjava.packet.header.*;


public class StunServerImpl implements StunServer {

    private static final Logger logger = Logger.getLogger(StunServerImpl.class.getName());
    private DatagramSocket socket;
    private static int stunServerPort = StunServer.STUN_SERVER_PORT;

	private StunUdpListener stunUdpListenerRTP;
	private StunTcpListener stunTcpListenerRTP;
	private StunUdpListener stunUdpListenerRTCP;
	private StunTcpListener stunTcpListenerRTCP;

	private long tieBreaker;
	private boolean firstTime = true;

    static {
	String s = System.getProperty(
	    "gov.nist.javax.sip.stack.STUN_SERVER_PORT");

	if (s != null) {
	    try {
		stunServerPort = Integer.parseInt(s);
	    } catch (NumberFormatException e) {
		System.out.println("Invalid stun server port " + s
		   + " defaulting to " + stunServerPort);
	    }
	}
    }

    public StunServerImpl() {

		SecureRandom random = new SecureRandom();
		tieBreaker = random.nextLong();
    }

    public void startServer() throws IOException {
	stunUdpListenerRTP = new StunUdpListener(stunServerPort);
	stunUdpListenerRTCP = new StunUdpListener(stunServerPort + 1);

	stunTcpListenerRTP = new StunTcpListener(stunServerPort);
	stunTcpListenerRTCP = new StunTcpListener(stunServerPort + 1);
    }

    public void stopServer()  {
	stunUdpListenerRTP.stop();
	stunTcpListenerRTP.stop();
	stunUdpListenerRTCP.stop();
	stunTcpListenerRTCP.stop();
    }

    public static void setLogLevel(Level newLevel) {
        logger.setLevel(newLevel);
    }

    class StunUdpListener extends Thread {

	private DatagramSocket socket;
	private int stunServerPort;

	public StunUdpListener(int stunServerPort) throws IOException {
            try {
                socket = new DatagramSocket(stunServerPort);
            } catch (SocketException e) {
                throw new IOException("Can't create DatagramSocket:  "
		    + e.getMessage());
            }

	    this.stunServerPort = stunServerPort;

	    synchronized (this) {
	        start();

	        try {
		    wait();
	        } catch (InterruptedException e) {
	        }
	    }
        }

        public void run() {
	    logger.warning("STUN Server:  Listening for Stun requests on UDP port "
	        + stunServerPort + "...");

	    synchronized (this) {
	        notifyAll();
	    }

	    while (true) {
	        try {
		    byte[] buf = new byte[10000];
        	    DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            socket.receive(packet);

	        //logger.warning("New UDP STUN from "	+ packet.getSocketAddress());

		    processStunRequest(socket, packet);
                } catch (IOException e) {
                    logger.warning(
		        "STUN Server:  send or received failed " + e.toString());
	        }
	    }
	}

    }

    class StunTcpListener extends Thread {

	private ServerSocket serverSocket;
	private int stunServerPort;

	public StunTcpListener(int stunServerPort) throws IOException {
            try {
                serverSocket = new ServerSocket(stunServerPort);
            } catch (SocketException e) {
                throw new IOException("Can't create ServerSocket:  "
		    + e.getMessage());
            }

	    this.stunServerPort = stunServerPort;

	    synchronized (this) {
	        start();

	        try {
		    wait();
	        } catch (InterruptedException e) {
	        }
	    }
        }

        public void run() {
	    //logger.warning("STUN Server:  Listening for Stun requests on TCP port " + stunServerPort + "...");

	    synchronized (this) {
	        notifyAll();
	    }

	    while (true) {
	        try {
		    Socket socket = serverSocket.accept();

		    InetAddress address = socket.getInetAddress();

	        //logger.warning("New TCP STUN connection accepted from "	+ address.getHostAddress() + ":" + socket.getPort());

		    processStunRequest(socket);
                } catch (IOException e) {
                    logger.warning(
		        "STUN Server:  send or received failed " + e.toString());
	        }
	    }
	}

    }

    /*
     * This is called from the voice bridge when a packet is received
     * which looks like a STUN Binding request rather than an RTP packet.
     */

    public byte[] handleRfc5389Packet(InetSocketAddress isa, StunPacket packet, CallParticipant cp)
    {
		byte[] bytes = null;

		try {
			StunPacket response = new StunPacketImpl(MessageClass.SUCCESS, packet.getMethod(), packet.getTransactionId());

			response.getAttributes().add(AttributeFactory.createXORMappedAddressAttribute(
				((InetSocketAddress) isa).getAddress(),
				((InetSocketAddress) isa).getPort(), packet.getTransactionId()));

			if (cp != null && cp.getPassword() != null && cp.getUsername() != null)
			{
				response.getAttributes().add(AttributeFactory.createUsernameAttribute(cp.getUsername() + cp.getUsername()));
			}

			bytes = response.getBytes();
			StunReplyImpl reply = new StunReplyImpl(response);

		} catch (Exception e) {

			logger.warning("handleRfc5389Packet exception " + e);
		    e.printStackTrace();
		}

		return bytes;
	}


    public void makeStunRequest(DatagramChannel channel, InetSocketAddress isa, CallParticipant cp)
    {
		try {

			if (cp != null && cp.getPassword() != null)
			{
				StunPacket stunPacket = new StunPacketImpl(MessageClass.REQUEST, MessageMethod.BINDING);
				stunPacket.getAttributes().add(AttributeFactory.createUsernameAttribute(cp.getUsername() + cp.getUsername()));

				byte[] stunBytes = stunPacket.getBytes();
				channel.send(ByteBuffer.wrap(stunBytes), isa);
			}


		} catch (Exception e) {

			logger.warning("makeStunRequest exception " + e);
			e.printStackTrace();
		}
	}

    public void processStunRequest(DatagramChannel channel, InetSocketAddress isa, byte[] request, CallParticipant cp)
    {
		byte[] response = null;
		StunPacket stunPacket = null;

		//logger.warning("Got UDP internal Stun request from " + isa);

		try {
			stunPacket = new StunPacketImpl(request, 0, request.length);
		} catch (Exception e) {

			return;
		}

		if (stunPacket.isRfc5389())
		{
			try {
				//logger.warning("Got UDP Stun Rfc5389 request for channel " + stunPacket.toString());

				response = handleRfc5389Packet(isa, stunPacket, cp);
				channel.send(ByteBuffer.wrap(response), isa);

			} catch (Exception e) {

				logger.warning("processStunRequest exception " + e);
		    	e.printStackTrace();
			}
			return;
		}
		//logger.warning("Got UDP Stun request for channel " + request.length + " bytes from " + isa);

		response = getStunResponse(isa, request, request.length);

		InetSocketAddress responseAddress = StunHeader.getAddress(request, StunHeader.RESPONSE_ADDRESS);

		if (responseAddress != null)
		{
			isa = new InetSocketAddress(responseAddress.getAddress(), responseAddress.getPort());
		}

		int changeRequest = StunHeader.getChangeRequest(request);

		if ((changeRequest & StunHeader.CHANGE_IP_MASK) != 0)
		{
			/*
			 * Not sure we can change our IP Source Address.
			 * Just ignore the request so the client thinks it failed
			 * to get a response.
			 */

			logger.warning("UDP Stun request: Not sure we can change our IP Source Address");
			return;
		}

		if ((changeRequest & StunHeader.CHANGE_PORT_MASK) == 0)
		{
			try {
				//logger.warning("UDP Stun request: Change Port request");

			channel.send(ByteBuffer.wrap(response), isa);
			} catch (IOException ee) {
			logger.warning("Can't send Binding Error response on channel: "
				+ ee.getMessage());
			}

			return;
		}

		/*
		 * We have been asked to change our source port.
		 * Rather than use a channel, we might as well use a socket.
		 */
			DatagramSocket responseSocket = null;

		String s = null;

		try {
			responseSocket = new DatagramSocket();

			DatagramPacket packet = new DatagramPacket(response, response.length, isa);

			responseSocket.send(packet);

			logger.warning("UDP Stun request: Change Source Port request");

			return;

		} catch (SocketException e) {
			s = "CHANGE_PORT set but can't create new socket! "
				+ e.getMessage();
		} catch (IOException e) {
			s = ("Can't send Binding Response on socket:  "
			+ e.getMessage());
		}

		logger.warning("UDP Stun request: CHANGE_PORT set but can't create new socket!");

		logger.warning(s);
		response = getBindingErrorResponse(request, StunHeader.GLOBAL_ERROR, s);

		try {
			channel.send(ByteBuffer.wrap(response), isa);
		} catch (IOException e) {
			logger.warning("Can't send Binding Error response on channel: "
			+ e.getMessage());
			return;
		}
    }

    /*
     * This is called from the NIST SIP Stack UDPMessageProcessor.java
     * to get the public address of the SIP Listening point.
     */

    public void processStunRequest(DatagramSocket socket, DatagramPacket packet)
    {
		byte[] request = packet.getData();
		byte[] response = null;

		int length = packet.getLength();

		InetSocketAddress isa = (InetSocketAddress) packet.getSocketAddress();

		//logger.warning("Got UDP Stun request on socket "  + socket.getLocalAddress() + ":" + socket.getLocalPort() + " length " + length + " bytes " + " from " + isa);

		StunPacket stunPacket = new StunPacketImpl(request, 0, request.length);

		if (stunPacket.isRfc5389())
		{
			try {
				//logger.warning("Got UDP Stun Rfc5389 request for channel " + stunPacket.toString());
				response = handleRfc5389Packet(isa, stunPacket, null);
				packet.setData(response);
				socket.send(packet);

			} catch (Exception e) {

				logger.warning("processStunRequest exception " + e);
		    	e.printStackTrace();
			}
			return;
		}

		response = getStunResponse(isa, request, length);

		InetSocketAddress responseAddress = StunHeader.getAddress(
			request, StunHeader.RESPONSE_ADDRESS);

		if (responseAddress != null) {
			packet.setAddress(responseAddress.getAddress());
			packet.setPort(responseAddress.getPort());
		}

		int changeRequest = StunHeader.getChangeRequest(request);

		if ((changeRequest & StunHeader.CHANGE_IP_MASK) != 0) {
			/*
			 * Not sure we can change our IP Source Address.
			 * Just ignore the request so the client thinks it failed
			 * to get a response.
			 */
			 return;
		}

		DatagramSocket responseSocket = socket;

		if ((changeRequest & StunHeader.CHANGE_PORT_MASK) != 0) {
			try {
				responseSocket = new DatagramSocket();
			} catch (SocketException e) {
			String s = "CHANGE_PORT set but can't create new socket! "
				+ e.getMessage();
			logger.warning(s);
			response = getBindingErrorResponse(request,
				StunHeader.GLOBAL_ERROR, s);
			}
		}

		packet.setData(response);

		try {
			responseSocket.send(packet);

			String s = "";

			if (request.length >= StunHeader.STUN_HEADER_LENGTH
					+ StunHeader.TLV_LENGTH + StunHeader.MAPPED_ADDRESS_LENGTH) {

			int port = (int) (((request[StunHeader.STUN_HEADER_LENGTH
						+ StunHeader.TLV_LENGTH + 2] << 8) & 0xff00) |
				(request[StunHeader.STUN_HEADER_LENGTH
						+ StunHeader.TLV_LENGTH + 3] & 0xff));

			String privateAddress = " private address "
				+ (int) (request[StunHeader.STUN_HEADER_LENGTH
						+ StunHeader.TLV_LENGTH + 4] & 0xff) + "."
				+ (int) (request[StunHeader.STUN_HEADER_LENGTH
						+ StunHeader.TLV_LENGTH + 5] & 0xff) + "."
				+ (int) (request[StunHeader.STUN_HEADER_LENGTH
						+ StunHeader.TLV_LENGTH + 6] & 0xff) + "."
				+ (int) (request[StunHeader.STUN_HEADER_LENGTH
						+ StunHeader.TLV_LENGTH + 7] & 0xff);

			s = privateAddress + ":" + port;
			}

			logger.warning("Sent STUN Binding Response from "
			+ responseSocket.getLocalAddress() + ":"
			+ responseSocket.getLocalPort()
			+ " to " + packet.getAddress() + ":" + packet.getPort() + s);
		} catch (IOException e) {
			logger.warning("Unable to send STUN response! " + e.getMessage());
		}
    }

    public void processStunRequest(Socket socket)
    {
		//logger.warning("Got TCP Stun request from " + socket.getInetAddress() + ":" + socket.getPort());

		try {
			DataInputStream input = new DataInputStream(
			socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());

			InetSocketAddress isa = new InetSocketAddress(socket.getInetAddress(), socket.getPort());

			byte[] request = new byte[1000];

			int length = input.read(request);  // read from socket

			if (length == -1) {
				logger.warning("Stunserver socket closed to " + isa);
				return;
			}

			byte[] response = getStunResponse(isa, request, length);

			output.write(response);

			logger.warning("Sent TCP STUN Binding Response to " + isa);
		} catch (IOException e) {
			logger.warning("Unable to send TCP STUN response! "
			+ e.getMessage());
		}
    }

    private byte[] getStunResponse(InetSocketAddress isa, byte[] request,
	    int length) {

	if (length < StunHeader.STUN_HEADER_LENGTH) {
	    String msg = "Too short to have STUN HEADER " + length;
	    logger.warning(msg);
	    return getBindingErrorResponse(request, StunHeader.BAD_REQUEST,
		msg);
	}

	int messageType = (int) (((request[0] << 8) & 0xff00) |
	    (request[1] & 0xff));

	if (messageType != StunHeader.BINDING_REQUEST) {
	    String msg = "Only Binding Request is supported";
	    return getBindingErrorResponse(request, StunHeader.GLOBAL_ERROR,
		msg);
	}

	return processBindingRequest(isa, request, length);
    }

    private byte[] processBindingRequest(InetSocketAddress isa,
	    byte[] request, int length) {

 	byte[] response = new byte[StunHeader.STUN_HEADER_LENGTH
	    + StunHeader.TLV_LENGTH + StunHeader.MAPPED_ADDRESS_LENGTH
	    + StunHeader.TLV_LENGTH + StunHeader.CHANGED_ADDRESS_LENGTH];

	System.arraycopy(request, 0, response, 0,
	    StunHeader.STUN_HEADER_LENGTH);

	response[0] = 1;	// set Binding Response

	response[3] = (byte)
	    StunHeader.TLV_LENGTH + StunHeader.MAPPED_ADDRESS_LENGTH +
	    StunHeader.TLV_LENGTH + StunHeader.CHANGED_ADDRESS_LENGTH;

	response[StunHeader.STUN_HEADER_LENGTH + 1] =
	    StunHeader.MAPPED_ADDRESS;	// type

	response[StunHeader.STUN_HEADER_LENGTH + 3] =
	    StunHeader.MAPPED_ADDRESS_LENGTH;  // length

	response[StunHeader.STUN_HEADER_LENGTH + 5] = 1;  // address family

	logger.warning("responding with " + isa);

	int sourcePort = isa.getPort();

	response[StunHeader.STUN_HEADER_LENGTH + 6] = (byte) (sourcePort >> 8);
	response[StunHeader.STUN_HEADER_LENGTH + 7] = (byte)
	    (sourcePort & 0xff);

	byte[] sourceAddress = isa.getAddress().getAddress();

 	response[StunHeader.STUN_HEADER_LENGTH + 8] = sourceAddress[0];
 	response[StunHeader.STUN_HEADER_LENGTH + 9] = sourceAddress[1];
 	response[StunHeader.STUN_HEADER_LENGTH + 10] = sourceAddress[2];
 	response[StunHeader.STUN_HEADER_LENGTH + 11] = sourceAddress[3];

 	response[StunHeader.STUN_HEADER_LENGTH + 13] =
	    StunHeader.CHANGED_ADDRESS;

 	response[StunHeader.STUN_HEADER_LENGTH + 15] =
	    StunHeader.CHANGED_ADDRESS_LENGTH;

 	response[StunHeader.STUN_HEADER_LENGTH + 17] = 1;  // address family

 	response[StunHeader.STUN_HEADER_LENGTH + 18] = (byte) (sourcePort >> 8);
 	response[StunHeader.STUN_HEADER_LENGTH + 19] = (byte)
	    (sourcePort & 0xff);

 	response[StunHeader.STUN_HEADER_LENGTH + 20] = sourceAddress[0];
 	response[StunHeader.STUN_HEADER_LENGTH + 21] = sourceAddress[1];
 	response[StunHeader.STUN_HEADER_LENGTH + 22] = sourceAddress[2];
 	response[StunHeader.STUN_HEADER_LENGTH + 23] = sourceAddress[3];

	return response;
    }

    private byte[] getBindingErrorResponse(byte[] request,
	    int responseCode, String reason) {

 	byte[] response = new byte[StunHeader.STUN_HEADER_LENGTH
	    + StunHeader.ERROR_CODE_LENGTH + reason.length()];

	System.arraycopy(request, 0, response, 0,
	    StunHeader.STUN_HEADER_LENGTH);

	response[0] = 1;	// set Binding Error Response
	response[1] = 0x11;

	response[StunHeader.STUN_HEADER_LENGTH + 2] = (byte)
	    (responseCode >> 8);

	response[StunHeader.STUN_HEADER_LENGTH + 3] = (byte)
	    (responseCode & 0xff);

	byte[] reasonBytes = reason.getBytes();

	System.arraycopy(reasonBytes, 0, response, 24, reasonBytes.length);

	int length = StunHeader.STUN_HEADER_LENGTH
	    + StunHeader.ERROR_CODE_LENGTH + reasonBytes.length;

	response[2] = (byte) (length >> 8);
	response[3] = (byte) (length & 0xff);

	return response;
    }

    /* For debugging */

    public static void main(String[] args) {
	StunServerImpl stunServerImpl = new StunServerImpl();

	try {
	    stunServerImpl.startServer();
	} catch (IOException e) {
            System.out.println("IOException:  " + e.getMessage());
	    System.exit(1);
	}

	stunServerImpl.test();
    }

    private void test() {
        DatagramSocket socket = null;

	try {
	    socket = new DatagramSocket();
	} catch (SocketException e) {
	    System.out.println(e.getMessage());
	    System.exit(1);
	}

	byte[] request = new byte[StunHeader.STUN_HEADER_LENGTH];

	request[1] = 1;

	for (int i = 0; i < 16; i++) {
	    request[4 + i] = (byte) i;   // transaction id
	}

	DatagramPacket packet = null;

	try {
	    packet = new DatagramPacket(request,
	        request.length, InetAddress.getLocalHost(), stunServerPort);
	} catch (UnknownHostException e) {
	    System.out.println("Can't get LocalHost!");
	    System.exit(1);
	}

        try {
            socket.send(packet);

	    if (logger.isLoggable(Level.FINEST)) {
                StunHeader.dump("Sent STUN Binding Request to "
                    + packet.getAddress() + ":" + packet.getPort(),
                    request, 0, request.length);
	    }
        } catch (IOException e) {
            System.out.println("Unable to send STUN Binding Request! "
		+ e.getMessage());
        }
    }

}
