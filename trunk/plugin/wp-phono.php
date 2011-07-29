<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link href="phono.css" media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="chat/js/jquery.js"></script>
<script type="text/javascript" src="http://s.phono.com/releases/0.2/jquery.phono.js"></script>
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

		phono = jQuery.phono(
		{
			apiKey: "73fd814cf1a5f9513a84048f8bec1645",

			onReady: function()
			{
			  jQuery("#demo-btn").attr("disabled", false).val("Call");

			  top.presenceMini('', '', '', 'sip:' + this.sessionId)
			}
		});

		jQuery('#demo-btn').bind(
			'click',
			function()
			{
				if(jQuery(this).val() == "Call")
				{
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

						  headset: true,

						  onAnswer: function()
						  {
							jQuery("#demo-status").html("Answered");
							jQuery("#demo-btn").val("Hangup");
							jQuery('#call-controls').show();
						  },

						  onHangup: function()
						  {
							jQuery("#demo-btn").attr("disabled", false).val("Call");
							jQuery("#demo-status").html("Hangup");
							jQuery('#call-controls').hide();
						  }
						});
					}

				} else {

					if(jQuery(this).val() != "Loading...")
						call.hangup();
				}
			}
		);
	}

</script>
</head>
<body onload="setupWindow()" onunload="closeWindow()">
<div class="col-box center-col-box"><center>
	<div class="content demo-box">
		<h2>Phone Call</h2>
		<p>
			<input type="text" size="30" id="demo-number" value="<?php echo $_GET['dest'] ? $_GET['dest'] : ""; ?>"/>
		</p>
		<p>
			<input type="button" id="demo-btn" disabled="true" value="Loading..."/>
			<div id='call-controls' style='padding:5px;'>
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
			</div>
			<div id="demo-status"></div>
		</p>
</center></div>
</body>
</html>