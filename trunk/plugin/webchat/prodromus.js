/**
 * "Hey there, I'm PRODROMUS, a very simple XMPP messaging client, 
 * mainly reasonable as contact form replacement/supplement or 
 * support contact utility."
 * 
 * (c) 2009-10 by Raphael Kallensee, http://raphael.kallensee.name
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * PRODROMUS uses jQuery, which is available under the MIT or GPL Version 2 
 * licenses. Secondly this little piece of software uses the great Strophe.js
 * library which is available under the MIT license, except for two functions
 * which are licensed as public domain and BSD. Additionally a Date.format
 * function is used which is available under the MIT license.
 *
 * jQuery: http://jquery.com 
 * Strophe.js: http://code.stanziq.com/strophe/
 */

/**
 * Prodromus class
 *
 * Serves as a namespace for all methods and functions
 * 
 * @license Affero General Public License
 */
var Prodromus = {
    // version constant
    VERSION: '0.2',
    
    // initialize connection property
    connection: null
}

Prodromus.config = {
    
    // ID of element (with prepended #), DOM element
    // or jQuery object of the element which should 
    // get used for the PRODROMUS client
    'TARGET_ELEMENT': '#prodromus',
    
    // XMPP server to connect to (should be accessible via BOSH service).
    // Authentication gets done via SASL ANONYMOUS, so you should use a 
    // server which supports that authentication type.
    'XMPP_SERVER': "btg199251",

    // BOSH service (should be absolute or relative path). This might be a
    // tricky part due to the JavaScript same origin policy. The easiest
    // way is to setup a reverse proxy under Apache. This only requires 
    // mod_proxy, mod_rewrite and a .htaccess file similar to the example
    // supplied with PRODROMUS.
    'BOSH_SERVICE': "http://btg199251:7070/http-bind/",
    
    // JID of receiver
    'INIT_RECEIVER': "demo@workgroup.btg199251",
    
    // name of receiver
    'INIT_RECEIVERNAME': "Demo Workgroup",
    
    // leave this empty or fill in a default name
    'SENDERNAME': 'John Doe',
    
    // date format, syntax like php's date function
    // (for US something like 'm-d-Y H:i:s', Germany like 'd.m.Y H:i:s')
    'DATEFORMAT': 'm-d-Y H:i:s',
    
    // language, currently supported: 'de' and 'en'
    'LANGUAGE': 'en',
    
    'INIT_CHATTYPE': 'chat',
    
    'ROOM': null
}

$(document).ready( function() {
    Prodromus.UI.initialize( $( Prodromus.config.TARGET_ELEMENT ) );
    
    Prodromus.connection = new Openfire.Connection( Prodromus.config.BOSH_SERVICE );
    
    // Uncomment the following lines to spy on the wire traffic.
    //Prodromus.connection.rawInput = function (data) { Prodromus.UI.log('RECV: ' + data, 'system'); };
    //Prodromus.connection.rawOutput = function (data) { Prodromus.UI.log('SEND: ' + data, 'system'); };

    // Uncomment the following line to see all the debug output.
    //Strophe.log = function (level, msg) { Prodromus.UI.log('LOG: ' + msg, 'system'); };
    
    $('#prodromus-messaging').hide();

    $('#prodromus-connect').bind( 'click', function( e ) {
	    Prodromus.actionhandler.connect();
    });
    
    $('#prodromus-talk').bind( 'click', function( e ) {
	    Prodromus.actionhandler.talk();
    });  
    
    $('#prodromus-screenshare').bind( 'click', function( e ) {
	    Prodromus.actionhandler.screenshare();
    });     
    
    $('#prodromus-msgform').bind( 'submit', function( e ) {
        return Prodromus.actionhandler.sendmessage();
    });
    
    $('#prodromus-login').bind( 'submit', function( e ) {
        return Prodromus.actionhandler.connect();
    });
    
    
    $('#prodromus-message').bind( 'keyup', function( e ) {
        return Prodromus.actionhandler.messagekeyup( e );
    });
    
    $('#prodromus-sendmessage').bind( 'click', function( e ) {
        Prodromus.actionhandler.sendmessage();
    });
});

$(window).unload( function() {
    Prodromus.connection.disconnect();
});

Prodromus.Util = {
    
    text2link: function( text )
    {
        if( !text ) {
            return text;
        }
        
        text = text.replace(
            /((https?\:\/\/|ftp\:\/\/)|(www\.))(\S+)(\w{2,4})(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/gi
          , function( url ) {
                nice = url;
                if( url.match( "^https?:\/\/" ) ) {
                    nice = nice.replace( /^https?:\/\//i, "" )
                } else {
                    url = 'http://'+url;
                }			
                return '<a target="_blank" href="' + url + '">' + nice.replace( /^www./i, "" ) + '</a>';
		    }
		);
		
		return text;
	},
	
	htmlspecialchars: function( text ) {
        text = text.replace(/\&/g, "&amp;");
        text = text.replace(/</g,  "&lt;");
        text = text.replace(/>/g,  "&gt;");
        text = text.replace(/'/g,  "&#39;");
        
        return text;
    }
	
}

Prodromus.buildAndSendMessage = function( message, type ) {

    var msg = $msg({from: Prodromus.connection.jid, to: Prodromus.config.RECEIVER, type: type})
        .c("body").t(message).up()
        .c("active", {xmlns: "http://jabber.org/protocol/chatstates"});
    
    // TODO: add real support for XEP-0085 (Chat State Notifications)

    Prodromus.connection.send( msg );

    if (type == "chat") Prodromus.UI.log( message, "msgOut" );
}


Prodromus.actionhandler = {
    connect: function() {
        if( $('#prodromus-username').val() != '' ) {
            var button = $('#prodromus-connect');
	        if( button.val() == Prodromus.i18n.t9n( 'connect' ) ) {
	            button.val( Prodromus.i18n.t9n( 'disconnect' ) );
	            $('#prodromus-usernameDisplay').text( $('#prodromus-username').val() );
	            $('#prodromus-username').hide();
	            
	            Prodromus.config.SENDERNAME = $('#prodromus-username').val();
		    Prodromus.config.RECEIVER = Prodromus.config.INIT_RECEIVER;
		    Prodromus.config.CHATTYPE = Prodromus.config.INIT_CHATTYPE;
		    Prodromus.config.CHATTYPE.RECEIVERNAME = Prodromus.config.CHATTYPE.INIT_RECEIVERNAME;
		    
    		    Prodromus.connection.resource = Math.random().toString(36).substr(2,9);	            
	            Prodromus.connection.connect( Prodromus.config.XMPP_SERVER, '', Prodromus.actionhandler.onConnect );
	        } else {
	            button.val( Prodromus.i18n.t9n( 'connect' ) );
	            
                    if (Prodromus.config.CHATTYPE == "groupchat") 
                    	Prodromus.connection.muc.leave(Prodromus.config.RECEIVER, Prodromus.config.SENDERNAME);
                    	
                    else {
			    Prodromus.buildAndSendMessage( 
				Prodromus.Util.htmlspecialchars( Prodromus.config.SENDERNAME ) + Prodromus.i18n.t9n( 'msg-goodbye' )
			      , Prodromus.config.CHATTYPE  
			    );                    
                    }
                    
	            setTimeout( 'Prodromus.connection.disconnect();', 1000 );
	        }
	    }
        return false;
    },
    
    messagekeyup: function( e ) {
        if( e.which == 13 ) {
            Prodromus.actionhandler.sendmessage();
	    }
	    
	    // TODO: add real support for XEP-0085 (Chat State Notifications)
	    
	    return true;
    },

    sendmessage: function() {
        $('#prodromus-message').val( 
            $('#prodromus-message').val().replace(/\n/g,"").replace(/\r/g,"")
        );
        
        if( $('#prodromus-message').val() != '' ) {
            Prodromus.buildAndSendMessage( $('#prodromus-message').val(), Prodromus.config.CHATTYPE );
	        $('#prodromus-message').val('');
	    }
        return false;
    },
    
    onConnect: function( status ) {
        switch( status ) 
        {
            case Strophe.Status.CONNECTING:
                $('#prodromus-messaging').slideDown();
                Prodromus.UI.log( Prodromus.i18n.t9n( 'connecting' ), 'system' );
                break;
                
            case Strophe.Status.CONNFAIL:
                Prodromus.UI.log( Prodromus.i18n.t9n( 'failed-to-connect' ), 'system' );
                $('#prodromus-connect').val() = Prodromus.i18n.t9n( 'connect' );
                $('#prodromus-username').get(0).readOnly = false;
                $('#prodromus-messaging').slideUp();
                break;
                
            case Strophe.Status.DISCONNECTING:
                Prodromus.UI.log( Prodromus.i18n.t9n( 'disconnecting' ), 'system' );
                break;
                
            case Strophe.Status.DISCONNECTED:
                Prodromus.UI.log( Prodromus.i18n.t9n( 'disconnected' ), 'system' );
                $('#prodromus-connect').val( Prodromus.i18n.t9n( 'connect' ) );
                $('#prodromus-username').get(0).readOnly = false;
                $('#prodromus-messaging').slideUp();
                
                WebRtc.close();	
                $('#prodromus-talk').css('display', 'none');
		$('#prodromus-screenshare').css('display', 'none');                
                break;
                
            case Strophe.Status.CONNECTED:
                Prodromus.UI.log( Prodromus.i18n.t9n( 'connected' ), 'system' );
                
                var username = $('#prodromus-username').get(0);
                username.readOnly = true;
                $('#prodromus-message').focus();

                Prodromus.connection.addHandler( 
                    Prodromus.actionhandler.onMessage, null, 'message'
                ); 
                
                Prodromus.connection.addHandler(
                    Prodromus.actionhandler.onPresence, null,"presence"
                ); 
                
                Prodromus.connection.send( $pres() );

                Prodromus.buildAndSendMessage(
                    Prodromus.Util.htmlspecialchars( Prodromus.config.SENDERNAME ) + Prodromus.i18n.t9n( 'msg-hello' )
                  , Prodromus.config.CHATTYPE 
                );

		if (WebRtc.localStream == null)
		{
			WebRtc.mediaHints = {audio:true, video:false};

			navigator.webkitGetUserMedia({audio:WebRtc.mediaHints.audio, video:WebRtc.mediaHints.video}, function(stream) 
			{
				WebRtc.localStream = stream;
				WebRtc.callback = {onReady: Prodromus.actionhandler.onReady, onMute: Prodromus.actionhandler.onMute};
				WebRtc.init(Prodromus.connection, {"iceServers": [{"url": "stun:stun.l.google.com:19302"}]}); 
				
			}, function(error) {

				WebRtc.log("WebRtc.onUserMediaError " + error.code);	
			});
			
		} 
	
                break;
        }
    },

    onMessage: function( msg ) 
    {    
        if($(msg).attr('type') == "chat" && $(msg).find('body').size() > 0 ) 
        {
       		var message = $(msg).find('body').first().text();

		Prodromus.config.RECEIVERNAME = Strophe.getNodeFromJid($(msg).attr('from'));        
		Prodromus.UI.log( message, 'msgIn' );
	       		
        	if ("Would you like to join the chat, yes or no?" == message)
        	{  
			Prodromus.buildAndSendMessage( "yes", "chat" );          	
        	
        	} else if ("Name:" == message) {
           	
			Prodromus.buildAndSendMessage( Prodromus.config.SENDERNAME , "chat" );    
			
        	}
        } else
        
        if($(msg).attr('type') == "groupchat" && $(msg).find('body').size() > 0 ) 
        {
        	var message = $(msg).find('body').first().text();

        	if (message.indexOf("screen___share") > -1)
        	{ 
        		var prompt = "click here to view screen share"
        		message = '<a target="_blank" href="' + message + '"><img style="width:16px;" src="../chat/img/others/share_on.png">' + prompt + '</a>';
        	}
        	
		Prodromus.config.RECEIVERNAME = Strophe.unescapeNode(Strophe.getResourceFromJid($(msg).attr('from')));
		Prodromus.UI.log( message, 'msgIn' );

        }  else      

        if($(msg).find('workgroup').size() > 0 && $(msg).find('invite').size() > 0)
        {
        	Prodromus.config.CHATTYPE = "groupchat";
        	Prodromus.config.RECEIVER = $(msg).attr('from');
        	Prodromus.config.ROOM = Strophe.getNodeFromJid($(msg).attr('from'));
        	Prodromus.config.SENDER = Prodromus.config.RECEIVER + "/" + Strophe.escapeNode(Prodromus.config.SENDERNAME)
        	
		Prodromus.connection.muc.join($(msg).attr('from'), Prodromus.config.SENDERNAME) ;
		
        } else if ($(msg).find('webrtc').size() > 0) {
        
		var jid = $(msg).attr('from'); 
		var room = Strophe.getNodeFromJid(jid);		
        
		WebRtc.handleMessage(msg, jid, room);
	}
		
        return true;
    },
    
    onPresence: function( presence ) 
    {   
	var _from = $(presence).attr('from');
	var xquery = presence.getElementsByTagName("x");

	console.log("onPresence " + _from);
	
	if (xquery.length == 0)
	{
		var type = $(presence).attr('type');   	
		var $show = $(presence).find('show');
		var $status = $(presence).find('status');
		
		var showText = "available";
		var statusText = "";
		var identityText = "";

		if ($show.length == 1) {
		    showText = $show.text();
		} 

		if ($status.length == 1) {
		    statusText = $status.text();
		}
	
	} else {	
	
	    for (var i = 0; i < xquery.length; i++)
	    {
		var xmlns = xquery[i].getAttribute("xmlns");

		if (xmlns && xmlns.match(Strophe.NS.MUC))
		{
			var _presenceType = presence.getAttribute('type');
			var _room = _from.split("@")[0];  			
			var _participant = _from.split("/")[1]; 			

			Prodromus.config.RECEIVERNAME = Strophe.unescapeNode(Strophe.getResourceFromJid(_from));

			if (Prodromus.config.SENDER != _from)
			{
				if (_presenceType && _presenceType == "unavailable")
				{		
					Prodromus.UI.log( "Leaving convesation", 'msgIn' );

				} else {
					Prodromus.UI.log( "Joining convesation", 'msgIn' );			

				}
			}
			
			WebRtc.handleRoster(Prodromus.config.SENDER, _from, _room, _presenceType && _presenceType == "unavailable" ? "leave" : "join")
		}
	    }
	}
	
	return true;
    },
    
    onReady: function( peer ) 
    { 
	WebRtc.log("handleReady " + peer.farParty); 
	$('#prodromus-talk').css('display', 'inline');
	$('#prodromus-screenshare').css('display', 'inline');	
    },
    
    onMute: function( peer, type, muc, audioMuted, videoMuted, videoRequested ) 
    { 
	WebRtc.log("handleMuteSignal " + peer.farParty + " " + audioMuted + " " + videoMuted + " " + type + " " + muc, 3);
    
    },

    screenshare: function()
    {
	var stream = "screen___share" + Math.random().toString(36).substr(2,9);  	
	Prodromus.buildAndSendMessage(stream, "groupchat");  

	var url = "../video/screenshare?stream=" + stream + "&app=xmpp";		
	$('body').append('<iframe height="0" width="0" src="' + url + '"></iframe>');	
    },
    
    talk: function()
    {
	WebRtc.toggleRoomMute(Prodromus.config.ROOM);  

	$('#prodromus-talk').val( 
		WebRtc.isRoomMuted(Prodromus.config.ROOM) ? Prodromus.i18n.t9n( 'talk' ) : 
								  Prodromus.i18n.t9n( 'mute' )
	);
    }
}

Prodromus.UI = {

    initialize: function( el ) {
        var pattern = 
             '<div id="prodromus-login">'
                +'<form name="prodromus-credentials" action="">'
                    +'<input type="button" id="prodromus-connect" value="{t9n_connect}" />'
                    +'<input style="display:none" type="button" id="prodromus-talk" value="{t9n_talk}" />'                    
                    +'<input style="display:none" type="button" id="prodromus-screenshare" value="{t9n_screenshare}" />'                       
                    +'<label for="prodromus-username">{t9n_your-name}:</label>'
                    +'<input type="text" id="prodromus-username" value="{sendername}" />'
                    +'<span id="prodromus-usernameDisplay"></span>'
                    +'<div class="clear"></div>'
                +'</form>'
            +'</div>'
            +'<div id="prodromus-messaging">'
                +'<div id="prodromus-log"></div>'
                +'<form id="prodromus-msgform" action="">'
                    +'<textarea id="prodromus-message" rows="2" cols="52"></textarea>'
                    +'<input type="button" id="prodromus-sendmessage" value="{t9n_send}" />'
                    +'<div class="prodromus-clear"></div>'
                +'</form>'
            +'</div>';
        
        pattern = pattern.replace( "{sendername}", Prodromus.config.SENDERNAME );
        pattern = pattern.replace( "{t9n_your-name}", Prodromus.i18n.t9n( 'your-name' ) );
        pattern = pattern.replace( "{t9n_connect}", Prodromus.i18n.t9n( 'connect' ) );
        pattern = pattern.replace( "{t9n_talk}", Prodromus.i18n.t9n( 'talk' ) );        
        pattern = pattern.replace( "{t9n_screenshare}", Prodromus.i18n.t9n( 'screenshare' ) );            
        pattern = pattern.replace( "{t9n_send}", Prodromus.i18n.t9n( 'send' ) );
            
        $(el).html( pattern );
    },
    
    log: function( msg, type ) {
        var pattern = 
             '<div class="message message{type}">' 
                +'<span class="msgText">{message}</span>'
                +'<span class="msgPerson">{person}<span class="msgTime">, {time}</span></span>'
            +'</div>';
        
        switch( type ) {
            case 'msgIn':
                pattern = pattern.replace( "{type}", "In" );
                pattern = pattern.replace( "{person}", Prodromus.Util.htmlspecialchars( Prodromus.config.RECEIVERNAME ) );
                break;
            case 'msgOut':
                pattern = pattern.replace( "{type}", "Out" );
                pattern = pattern.replace( "{person}", Prodromus.Util.htmlspecialchars( Prodromus.config.SENDERNAME ) );
                break;
            case 'system':
            default:
                pattern = pattern.replace( "{type}", "System" );
                pattern = pattern.replace( "{person}", "System" );
                break;
        }
        
        if (msg.indexOf("screen___share") == -1)
        {
        	msg = Prodromus.Util.htmlspecialchars( msg );        
        	msg = Prodromus.Util.text2link( msg );
        }
        
        pattern = pattern.replace( "{message}", msg );
        
        pattern = pattern.replace( "{time}", Prodromus.i18n.getFormattedDate() );
        $( pattern ).appendTo( $('#prodromus-log') );
        
        $("#prodromus-log").animate( { scrollTop: $("#prodromus-log").attr("scrollHeight") }, 1000 );
    }

}

Prodromus.i18n = {

    getFormattedDate: function() {
        return new Date().format( Prodromus.config.DATEFORMAT );
    },
    
    t9n: function( key ) {
        return Prodromus.t9n[ Prodromus.config.LANGUAGE ][ key ];
    }

}

Prodromus.t9n = {

    'de': {
        'your-name': 'Ihr Name',
        'connect': 'Verbinden',
        'talk': 'Talk',   
        'mute': 'Mute',          
        'screenshare': 'Screen Share',          
        'connecting': 'Verbindung wird hergestellt...',
        'connected': 'Verbunden!',
        'disconnect': 'Verbindung trennen',
        'disconnecting': 'Verbindung wird getrennt...',
        'disconnected': 'Verbindung getrennt.',
        'send': 'Senden',
        'failed-to-connect': 'Es konnte keine Verbindung zum Server aufgebaut werden!',
        'msg-hello': ' startete eine Unterhaltung.',
        'msg-goodbye': ' verlässt die Unterhaltung.'
    },
    
    'en': {
        'your-name': 'Your name',
        'connect': 'connect',
        'talk': 'talk',    
        'mute': 'mute',   
        'screenshare': 'Screen Share',         
        'connecting': 'Connecting...',
        'connected': 'Connected!',
        'disconnect': 'disconnect',
        'disconnecting': 'Disconnecting...',
        'disconnected': 'Disconnected.',
        'send': 'Send',
        'failed-to-connect': 'Failed to connect to the server!',
        'msg-hello': ' joins the chat.',
        'msg-goodbye': ' leaves the chat.'
    }

}

// Simulates PHP's date function
// @see http://jacwright.com/projects/javascript/date_format
// Licensed under a MIT license.
Date.prototype.format = function(format) {
	var returnStr = '';
	var replace = Date.replaceChars;
	for (var i = 0; i < format.length; i++) {
		var curChar = format.charAt(i);
		if (replace[curChar]) {
			returnStr += replace[curChar].call(this);
		} else {
			returnStr += curChar;
		}
	}
	return returnStr;
};

Date.replaceChars = {
	shortMonths: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
	longMonths: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
	shortDays: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
	longDays: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
	
	// Day
	d: function() { return (this.getDate() < 10 ? '0' : '') + this.getDate(); },
	D: function() { return Date.replaceChars.shortDays[this.getDay()]; },
	j: function() { return this.getDate(); },
	l: function() { return Date.replaceChars.longDays[this.getDay()]; },
	N: function() { return this.getDay() + 1; },
	S: function() { return (this.getDate() % 10 == 1 && this.getDate() != 11 ? 'st' : (this.getDate() % 10 == 2 && this.getDate() != 12 ? 'nd' : (this.getDate() % 10 == 3 && this.getDate() != 13 ? 'rd' : 'th'))); },
	w: function() { return this.getDay(); },
	z: function() { return "Not Yet Supported"; },
	// Week
	W: function() { return "Not Yet Supported"; },
	// Month
	F: function() { return Date.replaceChars.longMonths[this.getMonth()]; },
	m: function() { return (this.getMonth() < 9 ? '0' : '') + (this.getMonth() + 1); },
	M: function() { return Date.replaceChars.shortMonths[this.getMonth()]; },
	n: function() { return this.getMonth() + 1; },
	t: function() { return "Not Yet Supported"; },
	// Year
	L: function() { return "Not Yet Supported"; },
	o: function() { return "Not Supported"; },
	Y: function() { return this.getFullYear(); },
	y: function() { return ('' + this.getFullYear()).substr(2); },
	// Time
	a: function() { return this.getHours() < 12 ? 'am' : 'pm'; },
	A: function() { return this.getHours() < 12 ? 'AM' : 'PM'; },
	B: function() { return "Not Yet Supported"; },
	g: function() { return this.getHours() % 12 || 12; },
	G: function() { return this.getHours(); },
	h: function() { return ((this.getHours() % 12 || 12) < 10 ? '0' : '') + (this.getHours() % 12 || 12); },
	H: function() { return (this.getHours() < 10 ? '0' : '') + this.getHours(); },
	i: function() { return (this.getMinutes() < 10 ? '0' : '') + this.getMinutes(); },
	s: function() { return (this.getSeconds() < 10 ? '0' : '') + this.getSeconds(); },
	// Timezone
	e: function() { return "Not Yet Supported"; },
	I: function() { return "Not Supported"; },
	O: function() { return (-this.getTimezoneOffset() < 0 ? '-' : '+') + (Math.abs(this.getTimezoneOffset() / 60) < 10 ? '0' : '') + (Math.abs(this.getTimezoneOffset() / 60)) + '00'; },
	T: function() { var m = this.getMonth(); this.setMonth(0); var result = this.toTimeString().replace(/^.+ \(?([^\)]+)\)?$/, '$1'); this.setMonth(m); return result;},
	Z: function() { return -this.getTimezoneOffset() * 60; },
	// Full Date/Time
	c: function() { return "Not Yet Supported"; },
	r: function() { return this.toString(); },
	U: function() { return this.getTime() / 1000; }
};

