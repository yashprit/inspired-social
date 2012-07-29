<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link rel="stylesheet" type="text/css" href="chat/css/mini.css" />
<link rel="stylesheet" type="text/css" href="chat/css/window.css" />
<script type="text/javascript" src="chat/js/jquery.js"></script>
<script type="text/javascript" src="chat/js/interfaceUI.js"></script>
<script type="text/javascript" src="chat/php/get.php?l=en&t=js&g=mini.xml"></script>


<script language="JavaScript">
	var username, password;

	function setUser(user, pass)
	{
		username = user;
		password = pass;

		MINI_GROUPCHATS = [];
	}


	function getUsername()
	{
		return username;
	}

	function getPassword()
	{
		return password;
	}

	function setGroups(groups)
	{
		if (MINI_GROUPCHATS.length != groups.length || MINI_GROUPCHATS.length == 0)
		{
			if (username != "")
			{
				disconnectMini();

				MINI_GROUPCHATS = groups;
				MINI_ANIMATE = true;

				if (MINI_GROUPCHATS.length != groups.length)
				{
					launchMini(true, false, window.location.hostname, username, password);

				} else {

					launchMini(true, false, window.location.hostname, username, password);
				}

			}
		}

	}

	function showControls(height)
	{
		jQuery('#jappix_mini div.jm_roster div.jm_phone').css('height', height + "px");
		jQuery('#red5phone').css('height', height - 2 + "px");
		jQuery('#red5phone').css('width', "225px");
	}


	function doPhono(destination)
	{
		document.getElementById("red5phone").contentWindow.document.getElementById("demo-number").value = destination;
		document.getElementById("red5phone").contentWindow.makeCall();
	}

	function incomingCall(callId)
	{

	}


	function openURL(url, type)
	{
		if (type == "phono")
		{
			document.getElementById("red5frame").contentWindow.location.href = url;
			openWindow(300, 300);
		}

		if (type == "video-chat")
		{
			document.getElementById("red5frame").contentWindow.location.href = url;
			openWindow(720, 560);
		}

		if (type == "video-groupchat")
		{
			document.getElementById("red5frame").contentWindow.location.href = url;
			openWindow(1084, 660);
		}

		if (type == "screen-share-viewer")
		{
			document.getElementById("red5frame").contentWindow.location.href = url;
			openWindow(1064, 818);
		}

		if (type == "screen-share-publisher")
		{
			jQuery('body').append('<iframe height="0" width="0" src="' + url + '"></iframe>');
		}
	}

	function openWindow(width, height)
	{
		if(jQuery('#window').css('display') == 'none')
		{
			jQuery('#window').css('height', height + 'px');
			jQuery('#window').css('width', width + 'px');

			resizeWindow(width, height);

			jQuery('#window').show();
			jQuery('#red5frame').show();
		}
	}

	function resizeWindow(width, height)
	{
		jQuery('#windowBottom, #windowBottomContent').css('height', height-33 + 'px');

		var newHeight = height - 48;
		var newWidth = width - 25;

		jQuery('#windowContent').css('width', newWidth + 'px');
		jQuery('#red5frame').css('width', newWidth + 'px');

		jQuery('#windowContent').css('height', newHeight + 'px');
		jQuery('#red5frame').css('height', newHeight + 'px');

	}

	function setupWindow()
	{
		jQuery('#windowClose').bind(
			'click',
			function()
			{
				document.getElementById("red5frame").contentWindow.location.href = "about:blank";
				jQuery('#red5frame').hide();
				jQuery('#window').hide();
			}
		);

		jQuery('#windowMin').bind(
			'click',
			function()
			{
				jQuery('#windowContent').SlideToggleUp(300);
				jQuery('#windowBottom, #windowBottomContent').animate({height: 10}, 300);
				jQuery('#window').animate({height:30},300).get(0).isMinimized = true;
				jQuery(this).hide();
				jQuery('#red5frame').hide();
				jQuery('#windowResize').hide();
				jQuery('#windowMax').show();
			}
		);
		jQuery('#windowMax').bind(
			'click',
			function()
			{
				var windowSize = jQuery.iUtil.getSize(document.getElementById('windowContent'));
				jQuery('#windowContent').SlideToggleUp(300);
				jQuery('#windowBottom, #windowBottomContent').animate({height: windowSize.hb + 13}, 300);
				jQuery('#window').animate({height:windowSize.hb+43}, 300).get(0).isMinimized = false;
				jQuery('#red5frame').show();
				jQuery(this).hide();
				jQuery('#windowMin, #windowResize').show();
			}
		);

		jQuery('#window').Resizable(
			{
				minWidth: 200,
				minHeight: 60,
				maxWidth: 1400,
				maxHeight: 1050,
				dragHandle: '#windowTop',
				handlers: {
					se: '#windowResize'
				},
				onResize : function(size, position)
				{
					resizeWindow(size.width, size.height)
				}
			}
		);

		var myWidth;
		var myHeight;

		if ( typeof( window.innerWidth ) == 'number' )
		{
			myWidth = window.innerWidth;
			myHeight = window.innerHeight;

		} else if ( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {

			myWidth = document.documentElement.clientWidth;
			myHeight = document.documentElement.clientHeight;

		} else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {

			myWidth = document.body.clientWidth;
			myHeight = document.body.clientHeight;
		}

		jQuery('#wordpress').css('height', myHeight + 'px');
		jQuery('#wordpress').css('width',  myWidth +  'px');
	}

	function reload()
	{
		document.getElementById("red5frame").contentWindow.location.reload();
	}
</script>
</head>
<body topmargin="0" leftmargin="0" onload="setupWindow()" onunload="disconnectMini()" style="border-width:0px; overflow: hidden;margin-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px">
<iframe id="wordpress" frameborder="0" src="<?php echo $_GET['goto'] ? $_GET['goto'] : "index.php"; ?>" style="border:0px; border-width:0px; margin-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px; width:100%;height:100%;"></iframe>

<div id="window" style="display:none">
	<div id="windowTop">
		<div id="windowTopContent"><span onclick='reload()'>Inspired</span></div>
		<img src="chat/img/window/window_min.jpg" id="windowMin" />
		<img src="chat/img/window/window_max.jpg" id="windowMax" />
		<img src="chat/img/window/window_close.jpg" id="windowClose" />
	</div>
	<div id="windowBottom"><div id="windowBottomContent">&nbsp;</div></div>
	<div id="windowContent" style="overflow: hidden;margin-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px;">
		<iframe id="red5frame" width="100%" height="100%" marginwidth="0" marginheight="0" frameborder="no" scrolling="no" style="border-width:0px; border-color:#333; background:#FFF; overflow: hidden;margin-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px;">
	</div>
	<img src="chat/img/window/window_resize.gif" id="windowResize" />
</div>

</body>
</html>