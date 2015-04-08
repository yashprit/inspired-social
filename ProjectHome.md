# Inspired Social #
An Openfire plugin which combines [WordPress](http://wordpress.org) and [BuddyPress](http://buddypress.org) with [WebRTC](http://webrtc.org) to provide a fully collaborative online social community.

![http://inspired-social.googlecode.com/files/Image4.jpg](http://inspired-social.googlecode.com/files/Image4.jpg)

Please note that **Only Openfire and MySQL required. No Apache, No PHP. The Inspired-Social plugin has embedded Quercus PHP Engine and RTMP server for  screenshare only. You will also need Openfire Jingle Relay Nodes plugin in order to relay WebRTC audio and video when users are behind restrictive firewalls or home routers**

A User Guide can be found at http://inspired-social.googlecode.com/files/Inspired%20Social%20user%20guide.pdf

An Admin Guide can be found at http://inspired-social.googlecode.com/files/Inspired%20Social%20admin%20guide%20_Revised.pdf

## Inspired Social Software Profile ##

  1. WordPress 3.5.1
  1. BuddyPress 1.7.0
  1. BuddyPress Docs 1.3.3 by Boone B Gorges
  1. oEmbed for BuddyPress 0.5.2 by r-a-y
  1. Private BuddyPress 1.0.4 by Dennis Morhardt
  1. WP Better Emails 0.2.5 By ArtyShow
  1. Article2PDF 0.27 By Marc Schieferdecker
  1. BuddyPress Album+ 0.1.8.14 By The BP-Media Team
  1. TDLC Birthdays 0.4 by Tom Granger
  1. BP-Album 0.1.8.14 By The BP-Media Team
  1. BuddyMobile 1.6.9 By modemlooper
  1. BuddyPress Hovercards 1.1.3 by Mike Martel
  1. BuddyShare 1.2.1 by BuddyShare
  1. Piklist 0.7.2 by Piklist
  1. Wordpress Helpers 1.4.8 by PikList

## Inspired Communication Toolbar ##

  1. XMPP Webclient (http://www.jappix.com)
  1. WebRTC Video/Audio Conferencing (http://webrtc.org)
  1. Worgroups (Fastpath live chat) support
  1. RTMP Screen Share ([red5-screenshare](http://code.google.com/p/red5-screenshare))
  1. Embedded RTMP Server ([Milenia Grafter 64K RTMP Server](http://milgra.com/server/mileniagrafter/index.html))

## Requirements ##

  1. Openfire Server 3.8.0 and above configured with MySQL (Needed by WordPress)
  1. Openfire Jingle Relay Nodes plugin to relay WebRTC audio/video behind firewalls.
  1. Openfire [WebSockets](http://code.google.com/p/openfire-websockets/) plugin
  1. Nothing else required. No Apache, No PHP, No Asterisk and No Red5. The Inspired-Social plugin has embedded servers.

![http://inspired-social.googlecode.com/files/inspired-social.jpg](http://inspired-social.googlecode.com/files/inspired-social.jpg)


## How to Install ##

  1. Unzip inspired-x.x.x.x.zip  and copy the inspired.war file to the OPENFIRE\_HOME/plugins directory
  1. From a browser, go to http://your_openfire-server:7070/inspired/wp-admin and complete the installation of WordPress. Login as admin with default password admin. Make sure you change the default password from the WordPress dashboard.
  1. Go to WordPress Dashboard, activate BuddyPress.
  1. Do a bulk activate of all other plugins and configure them from dashboard.
  1. Go to appearance and configure your widgets. Add birthdays and events if required.
  1. Create a new user with username and password of choice.
  1. Go to Openfire admin web console and change HTTP bind ports from 7070/7443 to 80/443.
  1. Configure Openfire email and run the test to make sure it works.
  1. Go to http://your_openfire-server/inspired/social and login with your new user details.

## Openfire is auto-configured as follows ##

  1. All Openfire users are WordPress users and Openfire admin console **cannot** add or modify users
  1. All Openfire user groups are read from BuddyPress groups Openfire admin console **cannot** add or modify user groups
  1. Group Chat rooms are auto-created every time a BuddyPress group is created
  1. User Rosters are updated every time a BuddyPress friendship is made or broken.



## How to Use Communication Toolbar ##

  1. Roster of Friendships showing Presence
  1. IM chat with friends or MUC chats with BuddyPress group members
  1. Audio/Video conference with friends
  1. Audio conference only with BuddyPress group members
  1. Live chat (including audio) with anonymous visitors on another web site
  1. Screen share to friends or a group. Requires Java for publisher and Flash for viewer.