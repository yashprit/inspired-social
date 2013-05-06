<?php
/*
Plugin Name: Inspired Social
Plugin URI: http://code.google.com/p/inspired-social/
Description: This plugin adds communication features.
Version: 0.1
Author: Dele Olajide
Author URI: http://www.olajide.net
*/

/*
---------------------------------------

Mod pluggable.php

	return md5($password);

Mod functions.php

	$trace = debug_backtrace();

-----------------------------------------
*/

add_action('wp_login', 						'inspired_login_ok');
add_action('init', 							'my_plugin_init' );

add_action('friends_friendship_accepted', 	'inspired_create_friendship', 10, 3);
add_action('friends_friendship_deleted', 	'inspired_delete_friendship', 10, 3);
add_action('groups_group_create_complete', 	'inspired_create_group');
add_action('groups_join_group',				'inspired_join_group', 10, 2);
add_action('groups_leave_group',			'inspired_leave_group', 10, 2);
add_action('wp_head', 						'inspired_user_page');
add_action('admin_head', 					'inspired_user_page');
add_action('login_head', 					'inspired_user_page');
add_action('admin_menu', 					'openfire_userimport_menu');



function my_plugin_init() {
      $plugin_dir = basename(dirname(__FILE__));
      load_plugin_textdomain( 'inspired', null, $plugin_dir );
}

function openfire_userimport_menu()
{
	add_submenu_page( 'users.php', 'Openfire User Import', 'Import', 'manage_options', 'openfire-user-import', 'openfire_userimport_page');
}


function inspired_user_page()
{
	global $bp;
	//of_logInfo("inspired_user_page logged user " . $bp->loggedin_user->userdata->user_login);

	if ($bp->loggedin_user->userdata->user_login != "")
		of_set_user_session($bp->loggedin_user->userdata->user_login);

	echo '<script type="text/javascript">top.setGroups("' . $bp->loggedin_user->userdata->user_login . '", [' . getGroupChats($bp->loggedin_user->id) . ']);</script>';
}


function inspired_login_ok($username)
{
	of_logInfo("inspired_login_ok " . $username);
	of_set_user_session($username);
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

function openfire_userimport_page() {

	global $wpdb;

  	if (!current_user_can('manage_options')) {
    	wp_die( __('You do not have sufficient permissions to access this page.') );
  	}

	if ($_POST['mode'] == "submit")
	{
		$password = "changeme";
		$str_rows = getOpenfireUsers();
		$arr_rows = split("\|", $str_rows);

		if (is_array($arr_rows))
		{
			foreach ($arr_rows as $row)
			{
				of_logInfo("Importing... " . $row);

				$arr_values = split(",", $row);

				// username, email, name

				$username 		= $arr_values[0];
				$user_email		= $arr_values[1];
				$user_nicename	= $arr_values[2];

				if ($username != "admin")
				{
					$arr_names = split(" ", $user_nicename);

					$firstname 		= trim($arr_names[0]);
					$lastname 		= trim($arr_names[1]);

					$user_nicename	= sanitize_title($username);

					// add the new user

					$arr_user = array( 	'user_login' => $username,
										'user_nicename' => $user_nicename,
										'user_email' => $user_email,
										'user_registered' => date( 'Y-m-d H:i:s' ),
										'user_status' => "0",
										'display_name' => $username);

					$wpdb->insert( $wpdb->users, $arr_user );
					$user_id = $wpdb->insert_id;
					wp_set_password($password, $user_id);

					// add default meta values

					$arr_meta_values = array(
										'nickname' => $username,
										'rich_editing' => "true",
										'comment_shortcuts' => "false",
										'admin_color' => "fresh",
										$wpdb->prefix . 'capabilities' => 'a:1:{s:10:"subscriber";b:1;}',
										'first_name' => $firstname,
										'last_name' => $lastname,
										'default_password_nag' => "1");

					foreach ($arr_meta_values as $key => $value)
					{
						$arr_meta = array(	'user_id' => $user_id,
											'meta_key' => $key,
											'meta_value' => $value
										);

						$wpdb->insert( $wpdb->usermeta, $arr_meta );
					}
				}
			}

			$html_update = "<div class='updated'>All users appear to be have been imported successfully.</div>";

		} else {

			$html_update = "<div class='updated' style='color: red'>It seems the file was not uploaded correctly.</div>";
		}
	}

	?>
	<div class="wrap">
		<?php echo $html_update; ?>
		<div id="icon-users" class="icon32"><br /></div>
		<h2>Openfire User Import</h2>

		<form action="users.php?page=openfire-user-import" method="post">
			<input type="hidden" name="mode" value="submit">
			<input type="submit" value="Import" />
		</form>

		<p style="color: red">Please make sure you back up your database before proceeding!</p>
	</div>

	<?php
}
?>
