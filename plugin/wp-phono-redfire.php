<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link href="phono.css" media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="phono/jquery_1_4_2.js"></script>
<script type="text/javascript" src="phono/log4javascript.js"></script>
<script type="text/javascript" src="phono/jquery.phono-redfire.js"></script>
<script type="text/javascript" src="chat/js/interfaceUI.js"></script>


<script language="JavaScript">
	var phono, call;

	function closeWindow()
	{
		top.presenceMini('', '', '', '')
	}

	function setupWindow()
	{
		jQuery('#call-controls').hide();
		top.showControls(70)

		phono = jQuery.phono(
		{
			jid: window.location.hostname,
			connectionUrl: window.location.protocol + "//" + window.location.host + "/http-bind/",
			direct: false,

			onReady: function()
			{
			  jQuery("#demo-btn").attr("disabled", false).val("Call");
			  top.presenceMini('', '', '', 'sip:' + this.sessionId);
			},

			onUnready: function()
			{
			     jQuery("#demo-btn").val("Unavailable");
  			},

            onError: function(event)
            {
                console.log(event.reason);
            },

			phone:
			{
                ringTone: "phono/ringtones/Diggztone_Marimba.mp3",
                ringbackTone: "phono/ringtones/ringback-us.mp3",
				headset: false,

				onIncomingCall: function(event)
				{
					top.showRosterMini();
					top.incomingCall(event.call.id);

				  	jQuery("#demo-status").html("Incoming Call");
				  	jQuery("#demo-btn").val("Answer");

				  	call = event.call;

					Phono.events.bind(call,
					{
						   onHangup: function(event)
						   {
								hangupCall();
						   },

						   onError: function(event)
						   {
							 jQuery("#demo-status").html("Phone error: " + e.reason);
						   }
					});
				},

				onError: function(event)
				{
				  jQuery("#demo-status").html("Phone error: " + e.reason);
				}
			}

		});

		jQuery('#demo-btn').bind(
			'click',
			function()
			{
				if(jQuery(this).val() == "Call")
				{
					makeCall();

				} else if (jQuery(this).val() == "Answer") {

					call.answer();
					answerCall();

				} else {

					if(jQuery(this).val() != "Loading...")
						call.hangup();
				}
			}
		);
	}

	function makeCall()
	{
		top.showRosterMini();

		if(jQuery("#demo-number").val().length)
		{
			jQuery("#demo-btn").val("Calling...");

			var number=jQuery("#demo-number").val();

			call = phono.phone.dial(number,
			{
			  onRing: function()
			  {
				jQuery("#demo-status").html("Ringing");
			  },

			  headset: false,

			  onAnswer: function()
			  {
				answerCall();
			  },

			  onHangup: function()
			  {
				hangupCall();
			  }
			});
		}
	}


	function answerCall()
	{
		jQuery("#demo-status").html("Answered");
		jQuery("#demo-btn").val("Hangup");
		jQuery('#call-controls').show();
		top.showControls(180);
	}

	function hangupCall()
	{
		jQuery("#demo-btn").attr("disabled", false).val("Call");
		jQuery("#demo-status").html("Hangup");
		jQuery('#call-controls').hide();
		top.showControls(70);

		call = null;
		number=jQuery("#demo-number").val("")
		top.hideRosterMini();
	}

</script>
</head>
<body onload="setupWindow()" onunload="closeWindow()">
<div class="col-box center-col-box">
	<div class="content demo-box">
	   <table>
			<tr><td><input type="text" size="15" id="demo-number" value="<?php echo $_GET['dest'] ? $_GET['dest'] : ""; ?>"/></td></tr>
			<tr><td><input type="button" id="demo-btn" disabled="true" value="Loading..."/>&nbsp;<span id="demo-status"></span></td></tr>

			<tr><td><div id='call-controls' style='padding:3px;'>
				<table width="110px">
					<tr>
						<td><input type='button' onclick="call.digit('1');" value='1' /></td>
						<td><input type='button' onclick="call.digit('2');" value='2' /></td>
						<td><input type='button' onclick="call.digit('3');" value='3' /></td>
					</tr>
					<tr>
						<td><input type='button' onclick="call.digit('4');" value='4' /></td>
						<td><input type='button' onclick="call.digit('5');" value='5' /></td>
						<td><input type='button' onclick="call.digit('6');" value='6' /></td>
					</tr>
					<tr>
						<td><input type='button' onclick="call.digit('7');" value='7' /></td>
						<td><input type='button' onclick="call.digit('8');" value='8' /></td>
						<td><input type='button' onclick="call.digit('9');" value='9' /></td>
					</tr>
					<tr>
						<td><input type='button' onclick="call.digit('*');" value='*' /></td>
						<td><input type='button' onclick="call.digit('0');" value='0' /></td>
						<td><input type='button' onclick="call.digit('#');" value='#' /></td>
					</tr>
				</table>
			</div></td></tr>
			<tr><td><hr></td></tr>
		</table>
	</div>
</div>
</body>
</html>