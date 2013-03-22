<?php
global $buddymobile_options;

$buddymobile_options = get_option('buddymobile_plugin_options');


function buddysuite_register_menus() {
	register_nav_menus (
	array ( 'mobile-menu' => __ ('Mobile Menu') )
	);
}
add_action ( 'init', 'buddysuite_register_menus' );


function bp_mobile_plugin_menu() {
	add_options_page('BuddyMobile', 'BuddyMobile', 'manage_options', __file__, 'buddymobile_plugin_options_page' );

}
add_action('admin_menu', 'bp_mobile_plugin_menu');
//add_action('network_admin_menu', 'bp_mobile_plugin_menu');


function buddymobile_plugin_admin_init() {
	register_setting( 'buddymobile_plugin_options', 'buddymobile_plugin_options', 'buddymobile_plugin_options_validate' );
	add_settings_section('general_section', 'General Settings', 'buddymobile_section_general', __FILE__);
	add_settings_section('style_section', 'Style Settings', 'buddymobile_section_style', __FILE__);

	//general options
	add_settings_field('add2homescreen', 'Add to Homescreen', 'buddymobile_setting_add2homescreen', __FILE__, 'general_section');

	//style options
	add_settings_field('theme-style', 'Theme Style', 'buddymobile_setting_theme_style', __FILE__, 'style_section');
	add_settings_field('toolbar-color', 'Toolbar Color', 'buddymobile_setting_toolbar_color', __FILE__, 'style_section');

}
add_action('admin_init', 'buddymobile_plugin_admin_init');


function buddymobile_plugin_options_page() {
?>

	<div class="wrap">
		<div class="icon32" id="icon-options-general"><br></div>
		<h2>BuddyMobile</h2>
		<form action="options.php" method="post">
		<?php settings_fields('buddymobile_plugin_options'); ?>
		<?php do_settings_sections(__FILE__); ?>

		<p class="submit">
			<input name="Submit" type="submit" class="button-primary" value="<?php esc_attr_e('Save Changes'); ?>" />
		</p>
		</form>
	</div>

<?php
}

function buddymobile_section_general() {

}

function buddymobile_section_style() {

}


function buddymobile_plugin_options_validate($input) {

	return $input; // return validated input

}

/*** General settings functions ***/
function buddymobile_setting_add2homescreen() {
	global $buddymobile_options;
	$checked = '';

	if( !empty( $buddymobile_options['add2homescreen']) ) { $checked = ' checked="checked" '; }
	echo "<input ".$checked." id='add2homescreen' name='buddymobile_plugin_options[add2homescreen]' type='checkbox' />  ";
	_e('Enable add to homescreen notice on iOS devices.', 'buddymobile');

}


function buddymobile_setting_theme_style() {
	global $buddymobile_options;
	$checked = '';
	$checked2 = '';

	if( $buddymobile_options['theme-style'] == 'default' ) { $checked = ' checked="checked" '; }
	if( $buddymobile_options['theme-style'] == 'dark' ) { $checked2 = ' checked="checked" '; }

	echo "<input ". $checked  ." type='radio' id='theme-style-default' name='buddymobile_plugin_options[theme-style]' value='default' />   Default      ";
	echo "<input ". $checked2 ." type='radio' id='theme-style-dark' name='buddymobile_plugin_options[theme-style]' value='dark' />   Dark";


}

/*** style settings functions ***/
function buddymobile_setting_toolbar_color() {
	global $buddymobile_options;

	$value = !empty( $buddymobile_options['toolbar-color'] ) ? $buddymobile_options['toolbar-color'] : '' ;

	echo "<input id='toolbar-color' name='buddymobile_plugin_options[toolbar-color]' size='20' type='text' value='$value' />";
}


function buddymobile_admin_enqueue_scripts() {

    wp_enqueue_script( 'wp-color-picker' );
    // load the minified version of custom script
    wp_enqueue_script( 'buddymobile-custom', plugins_url( 'color-pick.js', __FILE__ ), array( 'jquery', 'wp-color-picker' ), '1.1', true );
    wp_enqueue_style( 'wp-color-picker' );
}
if ( isset( $_GET['page'] ) && ( $_GET['page'] == 'buddymobile/includes/bp-mobile-admin.php' ) ) {
	add_action( 'admin_enqueue_scripts', 'buddymobile_admin_enqueue_scripts' );
}