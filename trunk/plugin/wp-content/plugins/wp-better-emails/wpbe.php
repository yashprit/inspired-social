<?php
/*
Plugin Name: WP Better Emails
Plugin URI: http://wordpress.org/extend/plugins/wp-better-emails/
Description: Beautify the default text/plain WP mails into fully customizable HTML emails.
Version: 0.1.3
Author: ArtyShow
Author URI: http://wordpress.org/extend/plugins/wp-better-emails/
*/

/**
 * Hooks & actions
 */
register_activation_hook(__FILE__,'wpbe_register_options');
add_action('init', 'wpbe_init_textdomain');
add_action('admin_init', 'wpbe_plugin_init');
add_action('admin_menu', 'wpbe_add_settings_page');
add_action('wp_ajax_send_preview', 'wpbe_ajax_send_preview');
add_filter('plugin_action_links_' . plugin_basename(__FILE__), 'wpbe_add_settings_link');
add_filter('wp_mail_from_name', 'wpbe_set_from_name');
add_filter('wp_mail_from', 'wpbe_set_from_email');
add_filter('wp_mail_content_type', 'wpbe_set_content_type');

/**
 * Load the text domain for i18n
 *
 * @since 0.1.1
 */
function wpbe_init_textdomain() {
	load_plugin_textdomain( 'wp-better-emails', null, basename(dirname(__FILE__)) . '/langs/' );
}

/**
 * Init plugin options to white list our options & register our script
 *
 * @since 0.1
 */
function wpbe_plugin_init() {
	register_setting('wpbe_full_options', 'wpbe_options', 'wpbe_options_validate');
	wp_register_script('wpbe-admin-script', plugins_url('js/wpbe-admin-script.js', __FILE__), array('jquery'), null, true);
	wp_register_script('jquery-cookie', plugins_url('js/jquery.cookie.js', __FILE__), array('jquery'), null, true);
	wp_register_script('jquery-markitup', plugins_url('markitup/jquery.markitup.js', __FILE__), array('jquery'), null, true);
	wp_register_script('markitup-set', plugins_url('markitup/sets/html/set.js', __FILE__), array('jquery'), null, true);
	wp_register_style('wpbe-admin-style', plugins_url('css/wpbe-admin-style.css', __FILE__));
	wp_register_style('markitup-skin', plugins_url('markitup/skins/simple/style.css', __FILE__));
	wp_register_style('markitup-skin-toolbar', plugins_url('markitup/sets/html/style.css', __FILE__));
}

/**
 * Add a settings link in the plugins page
 *
 * @since 0.1
 *
 * @param array $links Plugin links
 * @return array Plugins links with settings added
 */
function wpbe_add_settings_link( $links ) {
	$links[] = '<a href="options-general.php?page=wpbe_options">' . __('Settings', 'wp-better-emails') . '</a>';
	return $links;
}

/**
 * Register options on plugin activation
 *
 * @since 0.1
 */
function wpbe_register_options() {
	global $wp_version;
	// Prevent activation if requirements are not met
	// WP 2.8 required
	if (version_compare($wp_version, '2.8', '<=')) {
		if (function_exists('deactivate_plugins')) {
			deactivate_plugins(__FILE__);
		}
		die(__('WP Better Emails requires WordPress 2.8 or newer.', 'wp-better-emails'));
	}

	$template = '';
	@include('email_template.php');
	$domain = strtolower( $_SERVER['SERVER_NAME'] );
	if ( substr( $domain, 0, 4 ) == 'www.' ) {
		$domain = substr( $domain, 4 );
	}
	$title = get_option('blogname');
	$wpbe_options = array (
		'from_email' => 'wordpress@' . $domain,
		'from_name' => $title,
		'template' => $template
	);
	if( get_option('wpbe_options') == null ) {
		add_option('wpbe_options', $wpbe_options);
	}
}

/**
 * Get options
 */
$wpbe_options = get_option('wpbe_options');

/**
 * Add option page to the built in settings menu
 *
 * @since 0.1
 */
function wpbe_add_settings_page() {
	$page = add_options_page(__('Email Options', 'wp-better-emails'), __('Email Options', 'wp-better-emails'), 'administrator', 'wpbe_options', 'wpbe_options_page');
	add_action('admin_print_scripts-' . $page, 'wpbe_admin_print_script');
	add_action('admin_print_styles-' . $page, 'wpbe_admin_print_style');
}

/**
 * Enqueue the script to display it on the options page
 *
 * @since 0.1
 */
function wpbe_admin_print_script() {
	//wp_enqueue_script('thickbox');
	wp_enqueue_script('jquery-cookie');
	wp_enqueue_script('jquery-ui-core');
	wp_enqueue_script('jquery-ui-tabs');
	wp_enqueue_script('jquery-markitup');
	wp_enqueue_script('markitup-set');
	wp_enqueue_script('wpbe-admin-script');
}

/**
 * Enqueue the style to display it on the options page
 *
 * @since 0.1
 */
function wpbe_admin_print_style() {
	wp_enqueue_style('wpbe-admin-style');
	//wp_enqueue_style('thickbox');
	wp_enqueue_style('markitup-skin');
	wp_enqueue_style('markitup-skin-toolbar');
}

/**
 * Include admin options page
 *
 * @since 0.1
 */
function wpbe_options_page() {
	include('wpbe-options.php');
}

/**
 * Sanitize each option value
 *
 * @since 0.1
 * @param array $input The options returned by the options page
 * @return array $input Sanitized values
 */
function wpbe_options_validate( $input ) {

	$from_email		= strtolower($input['from_email']);

	// Checking emails
	if ( !empty($from_email) && !is_email($from_email) ) {
		add_settings_error('wpbe_options', 'settings_updated', __('Please enter a valid sender email address.', 'wp-better-emails'));
		$input['from_email'] = '';
	} else {
		$input['from_email'] = sanitize_email($from_email);
	}

	// Check name
	$input['from_name']	=  esc_html($input['from_name']);

	if( empty($input['template']) )
		add_settings_error('wpbe_options', 'settings_updated', __('Template is empty', 'wp-better-emails'));
	// Check if %content% tag is the template body
	elseif ( strpos( $input['template'], '%content%') === false )
		add_settings_error('wpbe_options', 'settings_updated', __('No content tag found. Please insert  the %content% tag in your template', 'wp-better-emails'));
	$input['template']	= $input['template'];

	return $input;
}

/**
 * Send a email preview to test your template
 *
 * @since 0.1
 * @param string $email
 */
function wpbe_ajax_send_preview( $email ) {
	check_ajax_referer( 'email_preview' );
	$preview_email = sanitize_email($_POST['preview_email']);
	if( empty($preview_email) )
		die( '<div class="error"><p>' . __('Please enter an email', 'wp-better-emails') . '</p></div>' );
	if( !is_email($preview_email) )
		die( '<div class="error"><p>' . __('Please enter a valid email', 'wp-better-emails') . '</p></div>' );
	$message = __('Hey !', 'wp-better-emails');
	$message .= "\r\n\r\n";
	$message .= __('This is a sample email to test your template.', 'wp-better-emails');
	$message .= "\r\n\r\n";
	$message .= __('If you\'re not skilled in HTML/CSS email coding, I strongly recommend to leave the default template as it is. It has been tested on various and popular email clients like Gmail, Yahoo Mail, Hotmail/Live, Thunderbird, Apple Mail, Outlook, and many more. ', 'wp-better-emails');
	$message .= "\r\n\r\n";
	$message .= __('If you have any problem or any suggestions to improve this plugin, please let me know.', 'wp-better-emails');
	$message .= "\r\n\r\n";
	if( wp_mail( $preview_email, '[' . wp_specialchars_decode(get_option('blogname'), ENT_QUOTES) . '] - ' . __('Email template preview', 'wp-better-emails'), $message) )
		die('<div class="updated"><p>' . __('An email preview has been successfully sent to ' . $preview_email, 'wp-better-emails') . '</p></div>');
	else
		die( '<div class="error"><p>' . __('An error occured while sending email. Please check your server configuration.', 'wp-better-emails') . '</p></div>' );
}

/**
 * Add the HTML template to the message body.
 * Looks for %message% into the template and replace it with the message
 *
 * @since 0.1
 * @global array $wpbe_options
 * @param string $body The message to templatize
 * @return string $email The email surrounded by template
 */
function wpbe_email_templatize( $body ) {
	global $wpbe_options;
	$template = '';
	if( isset ($wpbe_options['template']) && !empty($wpbe_options['template']) )
		$template .= $wpbe_options['template'];
	$html_email = str_replace('%content%', $body, $template);
	return $html_email;
}

/**
 * Replace variables in the template
 *
 * @since 0.1
 * @param string $template Template with variables
 * @return string Template with variables replaced
 */
function wpbe_template_replacement( $template ) {
	$to_replace = array(
		'blog_url' => get_option('siteurl'),
		'blog_name' => get_option('blogname'),
		'blog_description' => get_option('blogdescription'),
		'admin_email' => get_option('admin_email'),
		'date' => date_i18n( get_option('date_format') ),
		'time' => date_i18n( get_option('time_format') )
	);
	foreach ( $to_replace as $tag => $var ) {
		$template = str_replace('%' . $tag . '%', $var, $template);
	}
	return $template;
}

/**
 * Checks the WP Better Emails options to activate the new mail function
 *
 * @since 0.1
 * @return bool
 */
function wpbe_check_template() {
	global $wpbe_options;
	if ( strpos( $wpbe_options['template'], '%content%') === false || empty($wpbe_options['template']) )
		return false;
	return true;
}

/**
 * Replaces sender email if set & valid
 *
 * @since 0.1
 * @global array $wpbe_options
 * @param string $from_email
 * @return string
 */
function wpbe_set_from_email( $from_email ) {
	global $wpbe_options;
	if ( !empty($wpbe_options['from_email']) && is_email( $wpbe_options['from_email'] ) )
		return $wpbe_options['from_email'];
	return $from_email;
}

/**
 * Replaces sender name if set
 *
 * @since 0.1
 * @global array $wpbe_options
 * @param string $from_name
 * @return string
 */
function wpbe_set_from_name( $from_name ) {
	global $wpbe_options;
	if ( !empty($wpbe_options['from_name']) )
		return wp_specialchars_decode($wpbe_options['from_name'], ENT_QUOTES);
	return $from_name;
}

/**
 * Always set content type to HTML
 *
 * @since 0.1
 * @param string $content_type
 * @return string $content_type
 */
function wpbe_set_content_type( $content_type ) {
	// Only convert if the message is text/plain and the template is ok
	if( $content_type == 'text/plain' && wpbe_check_template() === true ) {
		add_action('phpmailer_init', 'wpbe_send_html');
		return $content_type = 'text/html';
	}
	return $content_type;
}

/**
 * Add the email template and set it multipart
 *
 * @since 0.1
 * @param object $phpmailer
 */
function wpbe_send_html( $phpmailer ) {
	// Set the original plain text message
	$phpmailer->AltBody = wp_specialchars_decode($phpmailer->Body, ENT_QUOTES);
	// Clean < and > around text links in WP 3.1
	$phpmailer->Body = wpbe_esc_textlinks($phpmailer->Body);
	// Convert line breaks & make links clickable
	$phpmailer->Body = nl2br ( make_clickable ($phpmailer->Body) );
	// Add template to message
	$phpmailer->Body = wpbe_email_templatize($phpmailer->Body);
	// Replace variables in email
	$phpmailer->Body = wpbe_template_replacement($phpmailer->Body);
}

/**
 * Replaces the < & > of the 3.1 email text links
 *
 * @since 0.1.2
 * @param string $body
 * @return string
 */
function wpbe_esc_textlinks( $body ) {
	return preg_replace('#<(https?://[^*]+)>#', '$1', $body);
}
?>