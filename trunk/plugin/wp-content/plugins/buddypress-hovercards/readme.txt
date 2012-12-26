=== Plugin Name ===
Contributors: Mike_Cowobo
Donate link: http://trenvo.nl/
Tags: buddypress, hovercards
Requires at least: WP 3.2.1, BP 1.5
Tested up to: WP 3.4.2, BP 1.6.1
Stable tag: 1.0
License: GPLv2 or later
License URI: http://www.gnu.org/licenses/gpl-2.0.html

Add themable hovercards to your BuddyPress installation.

== Description ==

This plugin adds hovercards (like on WordPress,com (Gravatar), Twitter, Facebook, Google+, etc.) to BuddyPress. Whenever a user hovers over a user avatar, the hovercard shows up.

Hovercards are completely themable by adding a `hovercard.php` template to your (child) theme.

Based on imath's blogpost [on BuddyPress xprofile hovercards](http://imath.owni.fr/2010/11/23/buddypress-xprofile-hovercard/) and uses the jQuery [Tipsy](http://onehackoranother.com/projects/jquery/tipsy/) library and [Rrrene's hovercards](https://github.com/rrrene/tipsy.hovercard).

== Installation ==

1. You can download and install BuddyPress hovercards using the built in WordPress plugin installer. If you download BuddyPress Hovercards manually, upload the whole folder to "/wp-content/plugins/".
1. Activate the plugin through the 'Plugins' menu in WordPress

If you want to add a custom hovercard, or change the displayed fields, copy '/bp-hovercards/templates/hovercard.php' to the root of your (child) theme and edit it there to prevent it being overwritten at an update.

== Frequently Asked Questions ==

= Can I make my own hovercard? =

Yes. If you want to add a custom hovercard, or change the displayed fields, copy '/bp-hovercards/templates/hovercard.php' to the root of your (child) theme and edit it there to prevent it being overwritten at an update.

== Screenshots ==

1. Example hovercard using the template included in the plugin.

== Changelog ==

= 1.0 =
* Fixed that non-logged in users got a stylish '0' instead of the hovercard
* Hovercards are now reloaded on _all_ AJAX request (except BP Live Notifications)

= 0.9.6 =
* Hovercards now refresh anytime something is loaded in with AJAX
* BP 1.6 Beta 1 & 2 compatibility

= 0.95 =
* Fixed 'Load More'-bug. Users of the WP admin bar would not get hovercards on posts loaded in using the 'Load More' button.

= 0.9 =
* Initial upload