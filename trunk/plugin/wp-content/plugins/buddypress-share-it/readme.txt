=== BuddyShare ===
Contributors: modemlooper
Tags: buddypress, twitter, tweet button, facebook share, google+, share button, linkedin, digg
Tested up to: 3.4
BuddyPress: 1.5
Stable tag: 1.2.1
Version: 1.2.1


== Description ==

Adds a share buttons to your BuddyPress site to let people share content on sites like Twitter and Facebook.
== Installation ==

= Automatic Installation =

1. From inside your WordPress administration panel, visit 'Plugins -> Add New'
2. Search for `BuddyPress Share It` and find this plugin in the results
3. Click 'Install'
4. Once installed, activate via the 'Plugins -> Installed' page
5. Click BuddyPress share it link under the BuddyPress admin menu
6. Check the options you want

= Manual Installation =

1. Upload `buddypress-share-it` to the `/wp-content/plugins/` directory
2. Activate the plugin through the 'Plugins' menu in WordPress
5. Click BuddyPress share it link under the BuddyPress admin menu
6. Check the options you want

== Screenshots ==

1. Activity share button

2. Forum share button

3. BP Media share button

4. Admin options

== Notes ==
License.txt - contains the licensing details for this component.

To add sharing to BP media you must add the following add action to the BP Media template file comments.php. Add it right after the endif on line 21:

<?php do_action( 'bp_media_after_activity_meta' ); ?>

== Changelog ==
= 1.2.1 =
fixed missing class error

= 1.2 =
Various enhancements. added share buttons to BP media images when BP media is installed. Ability to pin images to Pinterest. New share icons with option to choose sizes of icons.

= 1.1.7 =
bud fixes

= 1.1.5 =
fixed ajax bug

= 1.1.4 =
Added Google+ and Linkedin
BuddyPress 1.3 compatibility
Removed google Buzz  

= 1.1.3 =
file path fix

= 1.1.2 =
* BuddyPress 1.2.8 and WordPress 3.1 support plus minor CSS tweaks

= 1.1.1 =
* added admin options

= 1.1 =
* added localization

= 1.01 =
* fixed file paths

= 1.0 =
* first release