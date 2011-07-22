<?php
/*
Plugin Name: Inspired Social
Plugin URI: http://code.google.com/p/inspired-social/
Description: This plugin adds communication features.
Version: 0.1
Author: Dele Olajide
Author URI: http://www.olajide.net
*/


add_action('login_head', 					'inspired_login_page');
add_action('init', 							'my_plugin_init' );

add_action('friends_friendship_accepted', 	'inspired_create_friendship', 10, 3);
add_action('friends_friendship_deleted', 	'inspired_delete_friendship', 10, 3);
add_action('groups_group_create_complete', 	'inspired_create_group');
add_action('groups_join_group',				'inspired_join_group', 10, 2);
add_action('groups_leave_group',			'inspired_leave_group', 10, 2);
add_action('wp_head', 						'inspired_user_page');
add_action('admin_head', 					'inspired_user_page');


function my_plugin_init() {
      $plugin_dir = basename(dirname(__FILE__));
      load_plugin_textdomain( 'inspired', null, $plugin_dir );
}

function inspired_user_page()
{
	global $bp;

	echo '<script type="text/javascript">top.setGroups([' . getGroupChats($bp->loggedin_user->id) . ']);</script>';
}


function inspired_login_page()
{
	echo "\n".'<script type="text/javascript">
		window.onload = function()
		{
			top.disconnectMini();
			top.setUser("", "");

		};
		window.onunload = function()
		{
			user_login = document.getElementById("user_login").value;
			user_pass = document.getElementById("user_pass").value;

			top.setUser(user_login, user_pass);
		};
		</script>';
}

function inspired_join_group($group, $user)
{
	joinLeaveGroup($user, $group, "join");

}

function inspired_leave_group($group, $user)
{
	joinLeaveGroup($user, $group, "leave");
}


function inspired_create_friendship($id, $from, $to)
{
	createFriendship($from, $to, "Friends");
}

function inspired_delete_friendship($id, $from, $to)
{
	removeFriendship($from, $to);
}

function inspired_create_group($id)
{
	createGroupChat($id);
}

?>