<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="X-UA-Compatible" content="chrome=1">
<link rel="stylesheet" type="text/css" href="chat/css/mini.css" />
<link rel="stylesheet" type="text/css" href="chat/css/window.css" />
<script type="text/javascript" src="chat/js/jquery.js"></script>
<script type="text/javascript" src="chat/js/interfaceUI.js"></script>
<script type="text/javascript" src="chat/js/webrtc.js"></script>
<script type="text/javascript" src="chat/php/get.php?l=en&t=js&g=mini.xml"></script>


<script language="JavaScript">
	var username, password, videoXid;

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
		if (username != "")
		{
			if (MINI_GROUPCHATS.length != groups.length || MINI_GROUPCHATS.length == 0)
			{
				if (MINI_GROUPCHATS.length != groups.length)
				{
					if(!MINI_INITIALIZED)
					{
						disconnectMini();

						MINI_GROUPCHATS = groups;
						MINI_ANIMATE = true;
						launchMini(true, false, window.location.hostname, username, password);

					} else {

						for(var i = 0; i < MINI_GROUPCHATS.length; i++)
						{
							if(!MINI_GROUPCHATS[i])
								continue;

							//console.log("setGroups removing group " + MINI_GROUPCHATS[i])

							try {
								var chat_room = bareXID(generateXID(MINI_GROUPCHATS[i], 'groupchat'));
								var hash = hex_md5(chat_room);
								var current = '#jappix_mini #chat-' + hash;

								jQuery(current).remove();
								presenceMini('unavailable', '', '', '', chat_room + '/' + unescape(jQuery(current).attr('data-nick')));
							}

							catch(e) {}
						}

						MINI_GROUPCHATS = groups;

						for(var i = 0; i < MINI_GROUPCHATS.length; i++)
						{
							if(!MINI_GROUPCHATS[i])
								continue;

							//console.log("setGroups adding group " + MINI_GROUPCHATS[i])

							try {
								var chat_room = bareXID(generateXID(MINI_GROUPCHATS[i], 'groupchat'));
								chatMini('groupchat', chat_room, getXIDNick(chat_room), hex_md5(chat_room), MINI_PASSWORDS[i], MINI_SHOWPANE);
							}

							catch(e) {}
						}
					}
				}
			}
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
			jQuery('#webrtcframe').show();
		}
	}

	function resizeWindow(width, height)
	{
		jQuery('#windowBottom, #windowBottomContent').css('height', height-33 + 'px');

		var newHeight = height - 48;
		var newWidth = width - 25;

		jQuery('#windowContent').css('width', newWidth + 'px');
		jQuery('#webrtcframe').css('width', newWidth + 'px');

		jQuery('#windowContent').css('height', newHeight + 'px');
		jQuery('#webrtcframe').css('height', newHeight + 'px');

	}

	function setupWindow()
	{
		jQuery('#windowClose').bind(
			'click',
			function()
			{
				jQuery('#webrtcframe').hide();
				jQuery('#window').hide();

				if (videoXid)
				{
					WebRtc.handleRoster(MINI_USER + "@" + MINI_DOMAIN, bareXID(videoXid), getXIDNick(videoXid), "leave");
					jQuery('#jappix_mini #chat-' + hex_md5(videoXid)).remove();
				}
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
				jQuery('#webrtcframe').hide();
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
				jQuery('#webrtcframe').show();
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

	function toggleFullScreen()
	{
		var videoElement = document.getElementById("remoteVideo");

		if (!document.mozFullScreen && !document.webkitFullScreen)
		{
		  if (videoElement.mozRequestFullScreen) {
			videoElement.mozRequestFullScreen();

		  } else {
			videoElement.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
		  }

		} else {

		  if (document.mozCancelFullScreen) {
			document.mozCancelFullScreen();
		  } else {
			document.webkitCancelFullScreen();
		  }
		}
	}
</script>
</head>
<body topmargin="0" leftmargin="0" onload="setupWindow()" onunload="disconnectMini()" style="border-width:0px; overflow: hidden;margin-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px">
<iframe id="wordpress" frameborder="0" src="<?php echo $_GET['goto'] ? $_GET['goto'] : "index.php"; ?>" style="border:0px; border-width:0px; margin-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px; width:100%;height:100%;"></iframe>

<div id="window" style="display:none">
	<div id="windowTop">
		<div id="windowTopContent"><span>Inspired</span></div>
		<img src="chat/img/window/window_min.jpg" id="windowMin" />
		<img src="chat/img/window/window_max.jpg" id="windowMax" />
		<img src="chat/img/window/window_close.jpg" id="windowClose" />
	</div>
	<div id="windowBottom"><div id="windowBottomContent">&nbsp;</div></div>
	<div id="windowContent" style="overflow: hidden;margin-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px;">
		<div id='webrtcframe' style='display:none'>
			<div style="position:relative;width:330px;height:250px;margin-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px;">
			  <video id="remoteVideo" onDblClick="toggleFullScreen();" style="width:320px;height:240px"></video>
			  <div style="position:absolute;bottom:0;left:0;right:0:width:64px;height:48px">
				<video id="localVideoPreview" autoplay="autoplay" style="width:64px;height:48px"/>
			  </div>
			</div>
		</div>
	</div>
	<img src="chat/img/window/window_resize.gif" id="windowResize" />
</div>

</body>
</html>