/*===========================================================================*
*                       		                                     *
*                                                                            *
*      Class methods     					 	     *
*                                                                            *
*                                                                            *
*============================================================================*/


WebRtc = function(room) 
{
	this.room = room;
	this.pc = null;	
	this.farParty = null;
	this.inviter = false;
	this.closed = true;
	this.candidates = new Array();	
	this.mediaHints = WebRtc.mediaHints;	
	this.localStream = WebRtc.localStream;
	this.localVideoPreview = "localVideoPreview";
	this.remoteVideo = "remoteVideo";
	this.remoteRoomMuteType = "room";
}

WebRtc.localStream = null;
WebRtc.mediaHints = {audio:true, video:false};

WebRtc.peers = {}; 
WebRtc.rooms = {}; 

WebRtc.log = function (msg) {console.log(msg)}; 


WebRtc.init =  function(connection, peerConfig)
{
	WebRtc.log("WebRtc.init");

	if (!window.webkitRTCPeerConnection) 
	{
		var msg = "webkitRTCPeerConnection not supported by this browser";			
		alert(msg);
		throw Error(msg);
	}

	WebRtc.peerConfig = peerConfig;
	WebRtc.connection = connection;	
};

WebRtc.close = function()
{
	WebRtc.log("WebRtc.close");
	
	var peers = Object.getOwnPropertyNames(WebRtc.peers)
	
	for (var i=0; i< peers.length; i++)
	{
		var peer = WebRtc.peers[peers[i]];

		if (peer && peer.pc)
		{		
			peer.terminate();
			WebRtc.peers[peers[i]] = null;
			peer = null;
		}
	}
	
	WebRtc.peers = {}; 
	WebRtc.rooms = {}; 	
	WebRtc.localStream = null;	
};


WebRtc.setPeer = function (uniqueKey, room)
{
	WebRtc.log("WebRtc.setPeer " + uniqueKey + " " + room);
		
	if (WebRtc.peers[uniqueKey] == null)
	{
		WebRtc.peers[uniqueKey] = new WebRtc(room);
	} else {
		
		WebRtc.peers[uniqueKey].room = room;
	}
	
	WebRtc.peers[uniqueKey].open();		
};

WebRtc.getPeer = function (jid)
{
	WebRtc.log("WebRtc.getPeer " + jid);
	
	return WebRtc.peers[WebRtc.escape(jid)];

};


WebRtc.handleMessage = function(message, jid, room)
{
	var uniqueKey = WebRtc.escape(jid)

	WebRtc.log("WebRtc.handleMessage " + jid + " " + room + " " + uniqueKey);
	
	if (message.getAttribute("type") == "error")
	{
		return;
	}
	
	if (message.getElementsByTagName("answer").length > 0)
	{
		var peer = WebRtc.peers[uniqueKey]

		if (peer && peer.pc)
		{
			peer.handleAnswer(message);

		}
		return;
	}
	
	if (message.getElementsByTagName("candidate").length > 0)
	{
		var peer = WebRtc.peers[uniqueKey]

		if (peer && peer.pc)
		{
			peer.handleCandidate(message);

		}
		return;	
	}

	if (message.getElementsByTagName("mute").length > 0)
	{
		var peer = WebRtc.peers[uniqueKey]

		if (peer && peer.pc)
		{
			peer.handleMute(message);

		}
		return;
	}
	
	if (message.getElementsByTagName("offer").length > 0)
	{
		WebRtc.setPeer(uniqueKey, room);
		WebRtc.peers[uniqueKey].handleOffer(message, jid);
		
		return;
	}

	
	var channels = message.getElementsByTagName("channel");

	if (channels.length > 0)
	{
		var peer = WebRtc.peers[uniqueKey]

		if (peer && peer.pc)
		{
			peer.handleChannel(channels[0]);

		}
		return;	
	}
};


WebRtc.handleRoster = function(myJid, jid, room, action, mediaHints)
{
	WebRtc.log("WebRtc.handleRoster " + myJid + " " + jid + " " + room + " " + action);

	var uniqueKey = WebRtc.escape(jid)
	
	if (!mediaHints) mediaHints = WebRtc.mediaHints;	// use global default;
	
	if (action == "chat")
	{
		WebRtc.log("WebRtc.handleRoster opening chat with " + room);					
		WebRtc.rooms[room] = {ready: true, active: false};		
		WebRtc.setPeer(uniqueKey, room);
		WebRtc.peers[uniqueKey].muc = false;
			
		if (!WebRtc.peers[uniqueKey].pc || !WebRtc.peers[uniqueKey].pc.remoteStreams)
		{
			WebRtc.peers[uniqueKey].initiate(jid, mediaHints);		
		}
			
	}
	
	if (action == "join")
	{
		if (myJid == jid)
		{
			WebRtc.log("WebRtc.handleRoster opening room " + room);					
			WebRtc.rooms[room] = {ready: true, active: false};
		}

		if (WebRtc.rooms[room] == null && myJid != jid)
		{
			WebRtc.setPeer(uniqueKey, room);
			WebRtc.peers[uniqueKey].muc = true;			
			WebRtc.peers[uniqueKey].initiate(jid, mediaHints);
		}			
	}	

	if (action == "leave")
	{
		if (myJid == jid)	// I have left, close all peerconnections
		{
			WebRtc.log("WebRtc.handleRoster closing room " + room);					
			WebRtc.rooms[room] = null;
			
			var peers = Object.getOwnPropertyNames(WebRtc.peers)

			for (var i=0; i< peers.length; i++)
			{
				var peer = WebRtc.peers[peers[i]];
				
				if (peer.room == room)
				{
					peer.close();	
				}
			}
			
		} else {				// someone has left, close their peerconnection
			var peer = WebRtc.peers[uniqueKey]

			if (peer != null)
			{		
				peer.close();		
			}		
		}
	}

};

WebRtc.muteRoom = function(mute, room)
{
	var peers = Object.getOwnPropertyNames(WebRtc.peers)
	
	for (var i=0; i< peers.length; i++)
	{
		WebRtc.log("Found participant " + peers[i]);		
	
		var peer = WebRtc.peers[peers[i]];

		if (peer && !peer.closed && peer.pc && peer.pc.localStreams.length > 0)
		{		
			if (peer.room == room)
			{
				WebRtc.log("muting to participant " + peer.farParty + " in room " + room + " " + mute);

				peer.pc.localStreams[0].audioTracks[0].enabled = !mute;	
				
				peer.sendMuteSignal(mute, true, "room");				
			}
		}
	}
	
	if (WebRtc.rooms[room]) WebRtc.rooms[room].active = !mute;
};

WebRtc.isRoomMuted = function(room)
{
	var mute = true;
	if (WebRtc.rooms[room]) mute = !WebRtc.rooms[room].active;
	
	return mute;
};

WebRtc.toggleRoomMute = function(room)
{
	WebRtc.muteRoom(!WebRtc.isRoomMuted(room), room);
};

WebRtc.muteRemoteRoom = function(mute, room)
{
	var peers = Object.getOwnPropertyNames(WebRtc.peers)
	
	for (var i=0; i< peers.length; i++)
	{
		var peer = WebRtc.peers[peers[i]];

		if (peer && !peer.closed && peer.pc && peer.pc.remoteStreams.length > 0)
		{							
			if (peer.room == room)
			{
				WebRtc.log("muting from participant " + peer.farParty + " in room " + room + " " + mute);

				peer.pc.remoteStreams[0].audioTracks[0].enabled = !mute;					
			}
		}
	}
	
	if (WebRtc.rooms[room]) WebRtc.rooms[room].active = !mute;
};

WebRtc.muteUser = function(mute, jid, video)
{	
	var uniqueKey = WebRtc.escape(jid)

	var peer = WebRtc.peers[uniqueKey]

	if (peer != null && peer.pc && peer.pc.localStreams.length > 0)
	{		
		WebRtc.log("muting local user " + peer.farParty + " " + mute);

		peer.pc.localStreams[0].audioTracks[0].enabled = !mute;				
				
		if (peer.pc.localStreams[0].videoTracks.length > 0 && video) 
		{
			peer.pc.localStreams[0].videoTracks[0].enabled = !mute;	
			
			peer.sendMuteSignal(mute, mute, "private");			
		} else {

			peer.sendMuteSignal(mute, true, "private");		
		}
	}	
};

WebRtc.isUserMuted = function(jid)
{
	var mute = true;
	var uniqueKey = WebRtc.escape(jid)

	var peer = WebRtc.peers[uniqueKey]

	if (peer != null && peer.pc && peer.pc.localStreams.length > 0)
	{		
		mute = !peer.pc.localStreams[0].audioTracks[0].enabled;		
	}	
	return mute;
};

WebRtc.toggleUserMute = function(jid, video)
{
	WebRtc.muteUser(!WebRtc.isUserMuted(jid), jid, video);
};

WebRtc.muteRemoteUser = function(mute, jid)
{
	var uniqueKey = WebRtc.escape(jid)

	var peer = WebRtc.peers[uniqueKey]

	if (peer != null && peer.pc && peer.pc.remoteStreams.length > 0)
	{		
		WebRtc.log("muting remote user " + peer.farParty + " " + mute);

		peer.pc.remoteStreams[0].audioTracks[0].enabled = !mute;
		
		if (peer.pc.remoteStreams[0].videoTracks.length > 0) 
		{
			peer.pc.remoteStreams[0].videoTracks[0].enabled = !mute;									
		}
	}
};

WebRtc.textToXML = function(text)
{
	var doc = null;

	if (window['DOMParser']) {
	    var parser = new DOMParser();
	    doc = parser.parseFromString(text, 'text/xml');

	} else if (window['ActiveXObject']) {
	    var doc = new ActiveXObject("MSXML2.DOMDocument");
	    doc.async = false;
	    doc.loadXML(text);

	} else {
	    throw Error('No DOMParser object found.');
	}

	return doc.firstChild;
};

WebRtc.escape = function(s)
{
        return s.replace(/^\s+|\s+$/g, '')
            .replace(/\\/g,  "")
            .replace(/ /g,   "")
            .replace(/\"/g,  "")
            .replace(/\&/g,  "")
            .replace(/\'/g,  "")
            .replace(/\//g,  "")
            .replace(/:/g,   "")
            .replace(/</g,   "")
            .replace(/>/g,   "")
            .replace(/\./g,  "")            
            .replace(/@/g,   "");

};

/*===========================================================================*
*                       		                                     *
*                                                                            *
*                Object Methods						     *
*                                                                            *
*                                                                            *
*============================================================================*/



WebRtc.prototype.initiate = function(farParty, mediaHints)
{
	WebRtc.log("initiate " + farParty);

	this.farParty = farParty;
	this.inviter = true;	
	
	var _webrtc = this;
				
	this.createPeerConnection(function() {

		WebRtc.log("initiate createPeerConnection callback");
	
		if (this.pc != null)
		{
			this.pc.createOffer( function(desc) 
			{
				_webrtc.pc.setLocalDescription(desc);
				_webrtc.sendSDP(desc.sdp, mediaHints); 				

			});		
		}	
	}, mediaHints);
}

WebRtc.prototype.terminate = function ()
{
	WebRtc.log("terminate");

	if (this.pc != null) this.pc.close();
	this.pc = null;	
}

WebRtc.prototype.open = function ()
{
	WebRtc.log("open");

	if (this.pc)
	{
		if (this.pc.remoteStreams.length > 0)
		{
			this.pc.remoteStreams[0].audioTracks[0].enabled = true;	
			if (this.pc.remoteStreams[0].videoTracks.length > 0) this.pc.remoteStreams[0].videoTracks[0].enabled = true;				
		}
		if (this.pc.localStreams.length > 0)
		{
			this.pc.localStreams[0].audioTracks[0].enabled = false;	
			if (this.pc.localStreams[0].videoTracks.length > 0) this.pc.localStreams[0].videoTracks[0].enabled = false;				
		}
		
		this.closed = false;		
	}
}

WebRtc.prototype.close = function ()
{
	WebRtc.log("close");

	if (this.pc)
	{
		if (this.pc.remoteStreams.length > 0)
		{
			this.pc.remoteStreams[0].audioTracks[0].enabled = false;
			if (this.pc.remoteStreams[0].videoTracks.length > 0) this.pc.remoteStreams[0].videoTracks[0].enabled = false;			
		}
		if (this.pc.localStreams.length > 0)
		{
			this.pc.localStreams[0].audioTracks[0].enabled = false;	
			if (this.pc.localStreams[0].videoTracks.length > 0) this.pc.localStreams[0].videoTracks[0].enabled = false;				
		}
		
		this.closed = true;		
	}
}
WebRtc.prototype.handleAnswer = function(elem)
{
	WebRtc.log("handleAnswer");

	var node = elem.getElementsByTagName("answer")[0];
	var mediaHints = {audio: node.getAttribute("audio") == "true", video: node.getAttribute("video") == "true"};
	
	var sdp = node.firstChild.data;
	this.inviter= true;
	this.pc.setRemoteDescription(new RTCSessionDescription({type: "answer", sdp : sdp}));
	
	this.addJingleNodesCandidates();	

}

WebRtc.prototype.handleOffer = function(elem, jid)
{
	WebRtc.log("handleOffer");

	var node = elem.getElementsByTagName("offer")[0];
	
	var mediaHints = {audio: node.getAttribute("audio") == "true", video: node.getAttribute("video") == "true"};
	
	var _webrtc = this;
	
	this.createPeerConnection(function() {

		WebRtc.log("handleOffer createPeerConnection callback");

		var sdp = node.firstChild.data;	
		
		_webrtc.inviter= false;	
		_webrtc.farParty = jid;
		_webrtc.muc = elem.getElementsByTagName("muc").length > 0;
		_webrtc.pc.setRemoteDescription(new RTCSessionDescription({type: "offer", sdp : sdp}));	
		
	}, mediaHints);

}

WebRtc.prototype.setAudioTrack = function()
{
	WebRtc.log("setAudioTrack");
	
	this.pc.localStreams[0].audioTracks[0].enabled = false;		
	if (this.pc.localStreams[0].videoTracks.length > 0) this.pc.localStreams[0].videoTracks[0].enabled = false;		
	
	var room = this.farParty.split("@")[0];
	
	if (WebRtc.rooms[room] && WebRtc.rooms[room].active) 
	{
		var audioMuted = false;
		var videoMuted = true;
		
		this.pc.localStreams[0].audioTracks[0].enabled = true;	
		
		if (this.pc.localStreams[0].videoTracks.length > 0)
		{
			videoMuted = false;
			this.pc.localStreams[0].videoTracks[0].enabled = true;	
		}
		
		if (WebRtc.callback) WebRtc.callback(this, this.remoteRoomMuteType, this.muc, audioMuted, videoMuted);		
	}
}

WebRtc.prototype.handleMute = function(elem)
{
	WebRtc.log("handleMute");
	
	var mute = elem.getElementsByTagName("mute")[0];
	var audio = mute.getAttribute("audio");
	var video = mute.getAttribute("video");	
	var muc = mute.getAttribute("muc");
	
	this.remoteRoomMuteType = mute.getAttribute("type");
	
	if (WebRtc.callback) WebRtc.callback(this, this.remoteRoomMuteType, muc == "true", audio == "true", video == "true");
}

WebRtc.prototype.handleCandidate = function(elem)
{
	WebRtc.log("handleCandidate");
	
	var candidate = elem.getElementsByTagName("candidate")[0];
	var ice = {sdpMLineIndex: candidate.getAttribute("label"), candidate: candidate.getAttribute("candidate")};
	var iceCandidate = new RTCIceCandidate(ice);
	
	if (this.farParty == null)	
	{
		this.candidates.push(iceCandidate);
	} else {
		this.pc.addIceCandidate(iceCandidate);
	}	
}

WebRtc.prototype.handleChannel = function(channel)
{
	WebRtc.log("handleChannel");

	var relayHost = channel.getAttribute("host");
	var relayLocalPort = channel.getAttribute("localport");
	var relayRemotePort = channel.getAttribute("remoteport");

	WebRtc.log("add JingleNodes candidate: " + relayHost + " " + relayLocalPort + " " + relayRemotePort); 

	this.sendTransportInfo("0", "a=candidate:3707591233 1 udp 2113937151 " + relayHost + " " + relayRemotePort + " typ host generation 0");				

	var candidate = new RTCIceCandidate({sdpMLineIndex: "0", candidate: "a=candidate:3707591233 1 udp 2113937151 " + relayHost + " " + relayLocalPort + " typ host generation 0"});				
	this.pc.addIceCandidate(candidate);				
}
	

WebRtc.prototype.createPeerConnection = function(callback, mediaHints)
{
	WebRtc.log("createPeerConnection");

	this.candidates = new Array();
	this.createCallback = callback;
	this.pc = new window.webkitRTCPeerConnection(WebRtc.peerConfig);

	this.pc.onicecandidate = this.onIceCandidate.bind(this);		
	this.pc.onstatechange = this.onStateChanged.bind(this);
	this.pc.onopen = this.onSessionOpened.bind(this);
	this.pc.onaddstream = this.onRemoteStreamAdded.bind(this);
	this.pc.onremovestream = this.onRemoteStreamRemoved.bind(this);
	
	if (this.mediaHints.audio != mediaHints.audio || this.mediaHints.video != mediaHints.video)
	{
		this.localStream = null;
		this.mediaHints = mediaHints;
	}
	
	if (this.localStream == null)
		navigator.webkitGetUserMedia(this.mediaHints, this.onUserMediaSuccess.bind(this), this.onUserMediaError.bind(this));
	else {
		this.pc.addStream(this.localStream);
		this.createCallback();	
	}

	this.closed = false;	
}

WebRtc.prototype.onUserMediaSuccess = function(stream)
{
	WebRtc.log("onUserMediaSuccess");
	this.pc.addStream(stream);
	this.localStream = stream;
	
	if (WebRtc.localStream == null) WebRtc.localStream = stream;
	
	this.createCallback();	
	
	if (this.localVideoPreview && stream.videoTracks.length > 0)
	{
		document.getElementById(this.localVideoPreview).src = webkitURL.createObjectURL(stream);
		document.getElementById(this.localVideoPreview).play();	
	}
}

WebRtc.prototype.onUserMediaError = function (error)
{
	WebRtc.log("onUserMediaError " + error.code);
}

WebRtc.prototype.onIceCandidate = function (event)
{
	WebRtc.log("onIceCandidate");

	while (this.candidates.length > 0)
	{
		var candidate = this.candidates.pop();

		console.log("Retrieving candidate " + candidate.candidate);		    

		this.pc.addIceCandidate(candidate);
	}
	
	if (event.candidate && this.closed == false)
	{		
		this.sendTransportInfo(event.candidate.sdpMLineIndex, event.candidate.candidate);
	}	
		
}


WebRtc.prototype.onSessionOpened = function (event)
{
	WebRtc.log("onSessionOpened");
	WebRtc.log(event);
}

WebRtc.prototype.onRemoteStreamAdded = function (event)
{
	var url = webkitURL.createObjectURL(event.stream);
	WebRtc.log("onRemoteStreamAdded " + url);
	WebRtc.log(event);
	
	if (this.inviter == false)
	{
	    var _webrtc = this;	
	    
	    this.pc.createAnswer( function (desc)
	    {
		_webrtc.pc.setLocalDescription(desc);			
		_webrtc.sendSDP(desc.sdp, {audio: event.stream.audioTracks.length > 0, video: event.stream.videoTracks.length > 0}); 	
		
	    });			
	}

	if (this.remoteVideo && event.stream.videoTracks.length > 0)
	{
		document.getElementById(this.remoteVideo).src = url;
		document.getElementById(this.remoteVideo).play();
	}
			
	this.setAudioTrack();	
}

WebRtc.prototype.onRemoteStreamRemoved = function (event)
{
	//var url = webkitURL.createObjectURL(event.stream);
	WebRtc.log("onRemoteStreamRemoved " + url);
	WebRtc.log(event);
}

WebRtc.prototype.onStateChanged = function (event)
{
	WebRtc.log("onStateChanged");
	WebRtc.log(event);
}



WebRtc.prototype.sendSDP = function(sdp, mediaHints)
{
	WebRtc.log("sendSDP " + mediaHints.audio + " " + mediaHints.video);
	WebRtc.log(sdp);	
	
	var msg = "";
	msg += "<message  type='chat' to='" + this.farParty + "'>";	
	msg += "<webrtc xmlns='http://webrtc.org/xmpp'>";
	
	if (this.inviter)
		msg += "<offer audio='" + mediaHints.audio + "' video='" + mediaHints.video + "'>" + sdp + "</offer>";
	else
		msg += "<answer audio='" + mediaHints.audio + "' video='" + mediaHints.video + "'>" + sdp + "</answer>";		

	if (this.muc) msg += "<muc/>";
	
	msg += "</webrtc>";	
	msg += "</message>";	
	
	this.sendPacket(msg);
}

	
WebRtc.prototype.sendMuteSignal = function (audio, video, type)
{
	WebRtc.log("sendMuteSignal " + audio + " " + video + " " + type);
		
	var msg = "";
	msg += "<message type='chat' to='" + this.farParty + "'>";	
	msg += "<webrtc xmlns='http://webrtc.org/xmpp'>";
	msg += "<mute audio='" + audio + "' video='" + video + "' muc='" + this.muc + "' type='" + type + "' />";	
	msg += "</webrtc>";
	if (this.muc) msg += "<muc/>";	
	msg += "</message>";	
	
	this.sendPacket(msg);
}

WebRtc.prototype.sendTransportInfo = function (sdpMLineIndex, candidate)
{
	WebRtc.log("sendTransportInfo");
	
	var msg = "";
	msg += "<message type='chat' to='" + this.farParty + "'>";	
	msg += "<webrtc xmlns='http://webrtc.org/xmpp'>";
	msg += "<candidate label='" + sdpMLineIndex + "' candidate='" + candidate + "' />";	
	msg += "</webrtc>";
	if (this.muc) msg += "<muc/>";	
	msg += "</message>";	
	
	this.sendPacket(msg);	
}


WebRtc.prototype.addJingleNodesCandidates = function() 
{
	WebRtc.log("addJingleNodesCandidates");
	
	var iq = "";
	var id = this.farParty;
		
	iq += "<iq type='get' to='" +  "relay." + window.location.hostname + "' id='" + id + "'>";
	iq += "<channel xmlns='http://jabber.org/protocol/jinglenodes#channel' protocol='udp' />";
	iq += "</iq>";	

	this.sendPacket(iq);	
}


WebRtc.prototype.sendPacket = function(packet) {

	try {	
		if (WebRtc.connection instanceof Strophe.Connection) 
		{	
			var xml = WebRtc.textToXML(packet);

			WebRtc.log("sendPacket with Strophe.Connection");
			WebRtc.log(xml);		

			WebRtc.connection.send(xml);		

		} else {

			WebRtc.log("sendPacket as String");
			WebRtc.log(packet);

			WebRtc.connection.sendXML(packet);
		}
	
	} catch (e) {

		WebRtc.log("sendPacket as String");
		WebRtc.log(packet);

		WebRtc.connection.sendXML(packet);	
	}
};
