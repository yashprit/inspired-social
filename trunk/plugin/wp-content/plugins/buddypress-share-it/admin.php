<?php

add_action('admin_menu', 'bp_share_it_plugin_menu');
add_action( 'network_admin_menu', 'bp_share_it_plugin_menu' );


function bp_share_it_plugin_menu() {
	add_submenu_page( 'bp-general-settings', 'Share It', 'Sharing', 'manage_options', 'bp-share-it', 'bpsi_plugin_options');
	
	//call register settings function
	add_action( 'admin_init', 'bpsi_register_settings' );
}

function bpsi_register_settings() {
	//register our settings
	register_setting( 'bpsi_plugin_options', 'icon-size' );

	register_setting( 'bpsi_plugin_options', 'external-posting' );
	register_setting( 'bpsi_plugin_options', 'activity-repost' );
	register_setting( 'bpsi_plugin_options', 'twitter' );
	register_setting( 'bpsi_plugin_options', 'buzz' );
	register_setting( 'bpsi_plugin_options', 'facebook' );
	register_setting( 'bpsi_plugin_options', 'linkedin' );
	register_setting( 'bpsi_plugin_options', 'digg' );
	register_setting( 'bpsi_plugin_options', 'email' );
	register_setting( 'bpsi_plugin_options', 'pinterest' );
	register_setting( 'bpsi_plugin_options', 'blog' );
	register_setting( 'bpsi_plugin_options', 'activity' );
	register_setting( 'bpsi_plugin_options', 'forum' );
	register_setting( 'bpsi_plugin_options', 'groups' );
	if ( !function_exists( 'bp_album_install' ) ) {
	}else {
		register_setting( 'bpsi_plugin_options', 'bp-media' );
	}
}

function bpsi_plugin_options() {
	if (!current_user_can('manage_options'))  {
		wp_die( __('You do not have sufficient permissions to access this page.') );
				
	}
	
$pluginpath = plugins_url();
	
?>

			<?php if ( !empty( $_GET['updated'] ) ) : ?>
				<div id="message" class="updated">
					<p><strong><?php _e('settings saved.', 'bpsi' ); ?></strong></p>
				</div>
			<?php endif; ?>


<div class="wrap">
<h2><?php _e('BuddyPress Share Settings', 'bpsi') ?></h2>

<form method="post" action="<?php echo admin_url('options.php');?>">
<?php wp_nonce_field('update-options'); ?>

<table class="wp-list-table widefat users" cellspacing="0"">
<thead>
	<tr>
		<th width="50%"><?php _e('Share buttons:', 'bpsi') ?></th>
		<th></th>
	</tr>
</thead>
<tbody id="the-list" class="list:user">
<tr valign="top">
<th scope="row"><img src="<?php echo $pluginpath ?>/buddypress-share-it/img/16px/Twitter.png" alt="Share on Twitter">  Twitter</th>
<td><input type="checkbox" name="twitter" value="1" <?php if (get_option('twitter')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<tr class="alternate" valign="top">
<th scope="row"><img src="<?php echo $pluginpath ?>/buddypress-share-it/img/16px/Google+.png" alt="Share on Google+">  Google+</th>
<td><input type="checkbox" name="buzz" value="1" <?php if (get_option('buzz')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<tr valign="top">
<th scope="row"><img src="<?php echo $pluginpath ?>/buddypress-share-it/img/16px/Facebook.png" alt="Share on Facebook">  Facebook</th>
<td><input type="checkbox" name="facebook" value="1" <?php if (get_option('facebook')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<tr class="alternate" valign="top">
<th scope="row"><img src="<?php echo $pluginpath ?>/buddypress-share-it/img/16px/LinkedIn.png" alt="Share on LinkedIn">  Linkedin</th>
<td><input type="checkbox" name="linkedin" value="1" <?php if (get_option('linkedin')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<tr valign="top">
<th scope="row"><img src="<?php echo $pluginpath ?>/buddypress-share-it/img/16px/Digg.png" alt="Share on Digg">  Digg</th>
<td><input type="checkbox" name="digg" value="1" <?php if (get_option('digg')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<tr class="alternate" valign="top">
<th scope="row"><img src="<?php echo $pluginpath ?>/buddypress-share-it/img/16px/Mail.png" alt="Share on Email">  Email</th>
<td><input type="checkbox" name="email" value="1" <?php if (get_option('email')==1) echo 'checked="checked"'; ?>/></td>
</tr>
<?php if ( !function_exists( 'bp_album_install' ) ) {
	}else {
?>
<tr valign="top">
<th scope="row"> <img src="<?php echo $pluginpath ?>/buddypress-share-it/img/16px/Pinterest.png" alt="Share on Pinterest">  Pinterest</th>
<td><input type="checkbox" name="pinterest" value="1" <?php if (get_option('pinterest')==1) echo 'checked="checked"'; ?>/></td>
</tr>
<?php	} ?>
</tbody>

<tfoot>
	<tr>
		<th></th>
		<th></th>
	</tr>
</tfoot>
</table>

<p></p>

<table class="wp-list-table widefat users" cellspacing="0"">
<thead>
	<tr>
		<th width="50%"><?php _e('Content to share:', 'bpsi') ?></th>
		<th></th>
	</tr>
</thead>
<tbody id="the-list" class="list:user">

<tr valign="top">
<th scope="row">Activity</th>
<td><input type="checkbox" name="activity" value="1" <?php if (get_option('activity')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<tr class="alternate" valign="top">
<th scope="row">Blog</th>
<td><input type="checkbox" name="blog" value="1" <?php if (get_option('blog')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<tr valign="top">
<th scope="row">Forums</th>
<td><input type="checkbox" name="forum" value="1" <?php if (get_option('forum')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<tr class="alternate" valign="top">
<th scope="row">Groups</th>
<td><input type="checkbox" name="group" value="1" <?php if (get_option('group')==1) echo 'checked="checked"'; ?>/></td>
</tr>
<?php if ( !function_exists( 'bp_album_install' ) ) {
	}else {
?>
<tr valign="top">
<th scope="row">BP Media Images</th>
<td><input type="checkbox" name="bp-media" value="1" <?php if (get_option('bp-media')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<?php	} ?>

<tfoot>
	<tr>
		<th></th>
		<th></th>
	</tr>
</tfoot>
</table>

<p></p>

<table class="wp-list-table widefat users" cellspacing="0"">
<thead>
	<tr>
		<th><?php _e('More Options:', 'bpsi') ?></th>
		<th></th>
		<th></th>
	</tr>
</thead>
<tbody id="the-list" class="list:user">

<tr valign="center">
<th scope="row">Button Icon size</th>
	<td><input style="float:left; margin:10px 5px 0 0;" type="radio" id="16-icon" name="icon-size" value="16" <?php if (get_option('icon-size')=='16') echo 'checked="checked"'; ?>/><img style="margin:10px 0 0 0;" src="<?php echo $pluginpath ?>/buddypress-share-it/img/16px/Twitter.png" alt="Share on Twitter"></td>
	<td><input style="float:left; margin:10px 5px 0 0;" type="radio" id="32-icon" name="icon-size" value="32" <?php if (get_option('icon-size')=='32') echo 'checked="checked"'; ?>/><img src="<?php echo $pluginpath ?>/buddypress-share-it/img/32px/Twitter.png" alt="Share on Twitter"></td>
</tr>

<!-- <tr valign="top">
<th scope="row">External Sharing</th>
<td><input type="checkbox" name="external-posting" value="1" <?php if (get_option('external-posting')==1) echo 'checked="checked"'; ?>/></td>
</tr>

<tr valign="top">
<th scope="row">Activity Repost</th>
<td><input type="checkbox" name="activity-repost" value="1" <?php if (get_option('activity-repost')==1) echo 'checked="checked"'; ?>/></td>
</tr> -->
<tfoot>
	<tr>
		<th></th>
		<th></th>
		<th></th>
	</tr>
</tfoot>
</table>


<input type="hidden" name="action" value="update" />
<input type="hidden" name="page_options" value="twitter,buzz,facebook,linkedin,digg,email,pinterest,activity,blog,forum,group,bp-media,icon-size,external-posting,activity-repost" />

<p class="submit">
<input type="submit" class="button-primary" value="<?php _e('Save Changes') ?>" />
</p>

</form>
</div>

<?php
}
?>