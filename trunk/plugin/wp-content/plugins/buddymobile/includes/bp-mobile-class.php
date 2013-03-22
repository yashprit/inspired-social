<?php
/** Constants *****************************************************************/

if ( !class_exists( 'BuddyMobile' ) ) :
/**
 * Main BuddyMobile Class
 *
 * Tap tap tap... Is this thing on?
 *
 * @since BuddyMobile (1.6)
 */
class BuddyMobile {

	var $iphoned;
	var $ipadd;

	/** Functions *************************************************************/

	/**
	 * The main BuddyMobile loader
	 *
	 * @since BuddyMobile (1.6)
	 *
	 */
	public function __construct() {

		$this->constants();
		$this->includes();
		$this->setup_actions();

	}

	/**
	 * BuddyMobile constants
	 *
	 * @since BuddyMobile (1.6)
	 *
	 */
	private function constants() {

			// Path and URL
		if ( !defined( 'BP_MOBILE_PLUGIN_DIR' ) ) {
			define( 'BP_MOBILE_PLUGIN_DIR',  trailingslashit( WP_PLUGIN_DIR . '/buddymobile' )  );
		}

		if ( !defined( 'BP_MOBILE_PLUGIN_URL' ) ) {

			$plugin_url = WP_PLUGIN_URL . '/buddymobile' ;

			// If we're using https, update the protocol. Workaround for WP13941, WP15928, WP19037.
			if ( is_ssl() )
					$plugin_url = str_replace( 'http://', 'https://', $plugin_url );
					define( 'BP_MOBILE_PLUGIN_URL', $plugin_url );
		}

	}

	/**
	 * Include required files
	 *
	 * @since BuddyMobile (1.6)
	 * @access private
	 *
	 */
	private function includes() {

		// Files to include
		$includes = array(
			'/includes/bp-mobile-loader.php',
			'/includes/bp-mobile-actions.php',
			'/includes/bp-mobile-class.php',
		);

		foreach ($includes as $include )
			include( BP_MOBILE_PLUGIN_DIR . $include );

		if ( is_admin() || is_network_admin() ) {
			include( BP_MOBILE_PLUGIN_DIR . '/includes/bp-mobile-admin.php' );
		}

	}

	/**
	 * Setup the default hooks and actions
	 *
	 * @since BuddyMobile (1.6)
	 * @access private
	 *
	 */
	private function setup_actions() {

		// load plugin text domain
		add_action( 'init', array( $this, 'textdomain' ) );

		// Register admin styles and scripts
		add_action( 'admin_print_styles', array( $this, 'register_admin_styles' ) );
		add_action( 'admin_enqueue_scripts', array( $this, 'register_admin_scripts' ) );

		add_action( 'plugins_loaded', array( &$this,'detectiPhone' ) );

		if ( !is_admin() ) {
			add_filter( 'stylesheet', array(&$this, 'get_stylesheet') );
			add_filter( 'theme_root', array(&$this, 'theme_root') );
			add_filter( 'theme_root_uri', array(&$this, 'theme_root_uri') );
			add_filter( 'template', array(&$this, 'get_template') );
		}

		$this->iphoned = false;
		$this->ipadd = false;
		$this->detectiPhone();

	}
		/**
	 * Loads the plugin text domain for translation
	 */
	public function textdomain() {
	}

	/**
	 * Registers and enqueues admin-specific styles.
	 */
	public function register_admin_styles() {

		// TODO change 'plugin-name' to the name of your plugin
		wp_enqueue_style( 'bp-mobile-admin-styles', BP_MOBILE_PLUGIN_DIR . 'css/admin.css' );

	}

	/**
	 * Registers and enqueues admin-specific JavaScript.
	 */
	public function register_admin_scripts() {

		// TODO change 'plugin-name' to the name of your plugin
		wp_enqueue_script( 'bp-mobile-admin-script', BP_MOBILE_PLUGIN_DIR . 'js/admin.js' );

	}


	public function detectiPhone( $query = '' ) {

		$container = $_SERVER['HTTP_USER_AGENT'] ;

		$useragents = array (
			"iPhone",      		// Apple iPhone
			"iPod",     		// Apple iPod touch
			"incognito",    	// Other iPhone browser
			"webmate",     		// Other iPhone browser
			"Android",     		// 1.5+ Android
			"dream",     		// Pre 1.5 Android
			"CUPCAKE",      	// 1.5+ Android
			"blackberry9500",   // Storm
			"blackberry9530",   // Storm
			"blackberry9520",   // Storm v2
			"blackberry9550",   // Storm v2
			"blackberry 9700",
			"blackberry 9800", 	//curve
			"blackberry 9850",
			"webOS",    		// Palm Pre Experimental
			"s8000",     		// Samsung Dolphin browser
			"bada",      		// Samsung Dolphin browser
			"Googlebot-Mobile"  // the Google mobile crawler
		);

		$ipadagents = array (
			"iPad"
		);

		foreach ( $useragents as $useragent ) {
			if ( preg_match("/".$useragent."/i",$container) && $_COOKIE['bpthemeswitch'] != 'normal' ){
				$this->iphoned = true;

			}
		}

		foreach ( $ipadagents as $ipadagent ) {
			if (preg_match("/".$ipadagent."/i",$container) ){
				$this->ipadd = true;

			}
		}

	}

	/**
	 * gets proper theme from plugin folder
	 */
	function get_stylesheet( $stylesheet ) {
		if ( $this->iphoned ) {
			return 'iphone';
		} else {
			return $stylesheet;
		}
	}

	function get_template( $template ) {
		if ( $this->iphoned ) {
			return 'iphone';
		} else {
			return $template;
		}
	}

	function get_template_directory( $value ) {
		$theme_root = BP_MOBILE_PLUGIN_DIR;
		if ( $this->iphoned ) {
				return $theme_root . '/themes';
		} else {
				return $value;
		}
	}

	function theme_root( $path ) {
		$theme_root = BP_MOBILE_PLUGIN_DIR ;
		if ( $this->iphoned ) {
			return $theme_root . '/themes';
		} else {
			return $path;
		}
	}

	function theme_root_uri( $url ) {
		if ( $this->iphoned ) {
			$dir = BP_MOBILE_PLUGIN_URL ;
			return $dir . '/themes';
		} else {
			return $url;
		}
	}


}
$bpmobile = new BuddyMobile ;

endif;