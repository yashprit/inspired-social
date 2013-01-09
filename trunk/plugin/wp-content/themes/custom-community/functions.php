<?php

    
require_once( dirname(__FILE__) . '/admin/cheezcap.php');
require_once( dirname(__FILE__) . '/core/loader.php');

/** Tell WordPress to run cc_setup() when the 'after_setup_theme' hook is run. */
add_action( 'after_setup_theme', 'cc_setup' );
if ( ! function_exists( 'cc_setup' ) ):
/**
 * Sets up theme defaults and registers support for various WordPress features.
 *
 * To override cc_setup() in a child theme, add your own cc_setup to your child theme's
 * functions.php file.
 *
 * @uses add_theme_support() To add support for post thumbnails and automatic feed links.
 * @uses register_nav_menus() To add support for navigation menus.
 * @uses add_custom_background() To add support for a custom background.
 * @uses add_editor_style() To style the visual editor.
 * @uses load_theme_textdomain() For translation/localization support.
 * @uses add_custom_image_header() To add support for a custom header.
 * @uses register_default_headers() To register the default custom header images provided with the theme.
 * @uses set_post_thumbnail_size() To set a custom post thumbnail size.
 * @uses $content_width To set a content width according to the sidebars.
 * @uses BP_DISABLE_ADMIN_BAR To disable the admin bar if set to disabled in the themesettings.
 *
 */
function cc_setup() {
    global $cap, $content_width;

    // This theme styles the visual editor with editor-style.css to match the theme style.
    add_editor_style();

    // This theme uses post thumbnails
    if ( function_exists( 'add_theme_support' ) ) {
        add_theme_support( 'post-thumbnails' );
        set_post_thumbnail_size( 222, 160, true );
        add_image_size( 'slider-top-large', 1006, 250, true  );
        add_image_size( 'slider-large', 990, 250, true  );
        add_image_size( 'slider-middle', 756, 250, true  );
        add_image_size( 'slider-thumbnail', 80, 50, true );
        add_image_size( 'post-thumbnails', 222, 160, true  );
        add_image_size( 'single-post-thumbnail', 598, 372, true );
    }

    // Add default posts and comments RSS feed links to head
    add_theme_support( 'automatic-feed-links' );

    // Make theme available for translation
    // Translations can be filed in the /languages/ directory
    load_theme_textdomain( 'cc', get_template_directory() . '/languages' );

    $locale = get_locale();
    $locale_file = get_template_directory() . "/languages/$locale.php";
    if ( is_readable( $locale_file ) )
        require_once( $locale_file );

    // This theme uses wp_nav_menu() in one location.
    register_nav_menus( array(
        'menu_top' => __( 'Header top menu', 'cc' ),
        'primary'  => __( 'Header bottom menu', 'cc' ),
    ) );
    
    // This theme allows users to set a custom background
    if($cap->add_custom_background == true){
        add_theme_support('custom-background');
    }
    // Your changeable header business starts here
    define( 'HEADER_TEXTCOLOR', '888888' );
    
    // No CSS, just an IMG call. The %s is a placeholder for the theme template directory URI.
    define( 'HEADER_IMAGE', '%s/images/default-header.png' );

    // The height and width of your custom header. You can hook into the theme's own filters to change these values.
    // Add a filter to cc_header_image_width and cc_header_image_height to change these values.
    define( 'HEADER_IMAGE_WIDTH', apply_filters( 'cc_header_image_width', 1000 ) );
    define( 'HEADER_IMAGE_HEIGHT', apply_filters( 'cc_header_image_height', 233 ) );


    // Add a way for the custom header to be styled in the admin panel that controls
    // custom headers. See cc_admin_header_style(), below.
    if($cap->add_custom_image_header == true){
        $defaults = array(
            /*'default-image'          => '',
            'random-default'         => false,
            'width'                  => 0,
            'height'                 => 0,
            'flex-height'            => false,
            'flex-width'             => false,
            'default-text-color'     => '',
            'header-text'            => true,
            'uploads'                => true,*/
//            'wp-head-callback'       => 'cc_admin_header_style',
//            'admin-head-callback'    => 'cc_header_style',
            'admin-preview-callback' => 'cc_admin_header_image',
        );
        add_theme_support('custom-header',$defaults);
        //add_custom_image_header( 'cc_header_style', 'cc_admin_header_style', 'cc_admin_header_image' );
    }
    
    // Define Content with
    $content_width  = "670";
    if($cap->sidebar_position == "left and right"){
        $content_width  = "432";
    }
    
    // Define disable the admin bar
    if($cap->bp_login_bar_top == 'off' || $cap->bp_login_bar_top == __('off','cc') ) {
        define( 'BP_DISABLE_ADMIN_BAR', true );
    } 
}
endif;

if ( ! function_exists( 'cc_admin_header_image' ) ) :
/**
 * Custom header image markup displayed on the Appearance > Header admin panel.
 *
 * Referenced via add_custom_image_header() in cc_setup().
 *
 */
function cc_admin_header_image() { ?>
    <div id="headimg">
        <?php
        if ( 'blank' == get_theme_mod( 'header_textcolor', HEADER_TEXTCOLOR ) || '' == get_theme_mod( 'header_textcolor', HEADER_TEXTCOLOR ) )
            $style = ' style="display:none;"';
        else
            $style = ' style="color:#' . get_theme_mod( 'header_textcolor', HEADER_TEXTCOLOR ) . ';"';
        ?>
        <h1><a id="name"<?php echo $style; ?> onclick="return false;" href="<?php echo home_url( '/' ); ?>"><?php bloginfo( 'name' ); ?></a></h1>
        <div id="desc"<?php echo $style; ?>><?php bloginfo( 'description' ); ?></div>
        <img src="<?php esc_url ( header_image() ); ?>" alt="" />
    </div>
<?php }
endif;

add_filter('widget_text', 'do_shortcode');
add_action('widgets_init', 'cc_widgets_init');
function cc_widgets_init(){
    register_sidebars( 1,
        array(
            'name'          => 'sidebar right',
            'id'            => 'sidebar',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'sidebar left',
            'id'            => 'leftsidebar',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    ### Add Sidebars
    register_sidebars( 1,
        array(
            'name'          => 'header full width',
            'id'            => 'headerfullwidth',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'header left',
            'id'            => 'headerleft',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'header center',
            'id'            => 'headercenter',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'header right',
            'id'            => 'headerright',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'footer full width',
            'id'            => 'footerfullwidth',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'footer left',
            'id'            => 'footerleft',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'footer center',
            'id'            => 'footercenter',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'footer right',
            'id'            => 'footerright',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'member header',
            'id'            => 'memberheader',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'member header left',
            'id'            => 'memberheaderleft',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'member header center',
            'id'            => 'memberheadercenter',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'member header right',
            'id'            => 'memberheaderright',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'member sidebar left',
            'id'            => 'membersidebarleft',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'member sidebar right',
            'id'            => 'membersidebarright',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'group header',
            'id'            => 'groupheader',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'group header left',
            'id'            => 'groupheaderleft',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'group header center',
            'id'            => 'groupheadercenter',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'group header right',
            'id'            => 'groupheaderright',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'group sidebar left',
            'id'            => 'groupsidebarleft',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 1,
        array(
            'name'          => 'group sidebar right',
            'id'            => 'groupsidebarright',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );
    register_sidebars( 15,
        array(
            'name'          => 'shortcode %1$s',
            'id'            => 'shortcode',
            'before_widget' => '<div id="%1$s" class="widget %2$s">',
            'after_widget'  => '</div><div class="clear"></div>',
            'before_title'  => '<h3 class="widgettitle">',
            'after_title'   => '</h3>'
        )
    );

}
if($cap->buddydev_search == true && defined('BP_VERSION') && function_exists('bp_is_active')) {
        
    //* Add these code to your functions.php to allow Single Search page for all buddypress components*/
    //    Remove Buddypress search drowpdown for selecting members etc
    add_filter("bp_search_form_type_select", "cc_remove_search_dropdown"  );
    function cc_remove_search_dropdown($select_html){
        return '';
    }
    
    remove_action( 'init', 'bp_core_action_search_site', 5 );//force buddypress to not process the search/redirect
    add_action( 'init', 'cc_bp_buddydev_search', 10 );// custom handler for the search
    
    function cc_bp_buddydev_search(){
    global $bp;
        if ( $bp->current_component == BP_SEARCH_SLUG )//if thids is search page
            bp_core_load_template( apply_filters( 'bp_core_template_search_template', 'search-single' ) );//load the single searh template
    }
    add_action("advance-search","cc_show_search_results",1);//highest priority
    
    /* we just need to filter the query and change search_term=The search text*/
    function cc_show_search_results(){
        //filter the ajaxquerystring
         add_filter("bp_ajax_querystring","cc_global_search_qs",100,2);
    }
    
    //show the search results for member*/
    function cc_show_member_search(){ ?>
        <div class="memberss-search-result search-result">
            <h2 class="content-title"><?php _e("Members Results","cc");?></h2>
            <?php locate_template( array( 'members/members-loop.php' ), true ) ;  ?>
            <?php global $members_template;
            if($members_template->total_member_count>1):?>
                <a href="<?php echo bp_get_root_domain().'/'.BP_MEMBERS_SLUG.'/?s='.$_REQUEST['search-terms']?>" ><?php echo sprintf(__("View all %d matched Members",'cc'),$members_template->total_member_count);?></a>
            <?php endif; ?>
        </div>
        <?php    
    }
    
    //Hook Member results to search page
    add_action("advance-search","cc_show_member_search",10); //the priority defines where in page this result will show up(the order of member search in other searchs)
    function cc_show_groups_search(){?>
        <div class="groups-search-result search-result">
        <h2 class="content-title"><?php _e("Group Search","cc");?></h2>
        <?php locate_template( array('groups/groups-loop.php' ), true ) ;  ?>
        
        <a href="<?php echo bp_get_root_domain().'/'.BP_GROUPS_SLUG.'/?s='.$_REQUEST['search-terms']?>" ><?php _e("View All matched Groups","cc");?></a>
    </div>
        <?php
     //endif;
      }
    
    //Hook Groups results to search page
     if(bp_is_active( 'groups' ))
        add_action("advance-search","cc_show_groups_search",10);
    
    /**
     *
     * Show blog posts in search
     */
    function cc_show_site_blog_search(){
        ?>
     <div class="blog-search-result search-result">
     
      <h2 class="content-title"><?php _e("Blog Search","cc");?></h2>
       
       <?php locate_template( array( 'search-loop.php' ), true ) ;  ?>
       <a href="<?php echo bp_get_root_domain().'/?s='.$_REQUEST['search-terms']?>" ><?php _e("View All matched Posts","cc");?></a>
    </div>
       <?php
      }
    
    //Hook Blog Post results to search page
     add_action("advance-search","cc_show_site_blog_search",10);
    
    //show forums search
    function cc_show_forums_search(){
        ?>
     <div class="forums-search-result search-result">
       <h2 class="content-title"><?php _e("Forums Search","cc");?></h2>
      <?php locate_template( array( 'forums/forums-loop.php' ), true ) ;  ?>
      <a href="<?php echo bp_get_root_domain().'/'.BP_FORUMS_SLUG.'/?s='.$_REQUEST['search-terms']?>" ><?php _e("View All matched forum posts","cc");?></a>
    </div>
      <?php
      }
    
    //Hook Forums results to search page
    if ( bp_is_active( 'forums' ) && bp_is_active( 'groups' ) && ( function_exists( 'bp_forums_is_installed_correctly' )))
        add_action("advance-search","cc_show_forums_search",20);
    
    
    //show blogs search result
    
    function cc_show_blogs_search(){
    
    if(!is_multisite())
        return;
        
        ?>
      <div class="blogs-search-result search-result">
      <h2 class="content-title"><?php _e("Blogs Search","cc");?></h2>
      <?php locate_template( array( 'blogs/blogs-loop.php' ), true ) ;  ?>
      <a href="<?php echo bp_get_root_domain().'/'.BP_BLOGS_SLUG.'/?s='.$_REQUEST['search-terms']?>" ><?php _e("View All matched Blogs","cc");?></a>
     </div>
      <?php
      }
    
    //Hook Blogs results to search page if blogs comonent is active
     if(bp_is_active( 'blogs' ))
        add_action("advance-search","cc_show_blogs_search",10);
    
    
     //modify the query string with the search term
    function cc_global_search_qs(){
        if(empty($_REQUEST['search-terms']))
            return;
    
        return "search_terms=".$_REQUEST['search-terms'];
    }
    
    function cc_is_advance_search(){
    global $bp;
    if($bp->current_component == BP_SEARCH_SLUG)
        return true;
    return false;
    }
    remove_action( 'bp_init', 'bp_core_action_search_site', 7 );
        
}
//load current displaymode template - loop-list.php or loop-grid.php
function cc_get_displaymode($object){
    $_BP_COOKIE = &$_COOKIE;
    if ( isset( $_BP_COOKIE['bp-' . $object . '-displaymode'])) {
        get_template_part( "{$object}/{$object}-loop", $_BP_COOKIE['bp-' . $object . '-displaymode']);        
    }
    else{
        get_template_part( "{$object}/{$object}-loop",'list');    
    }
    
}
//check if displaymode grid
function cc_is_displaymode_grid($object){
    $_BP_COOKIE = &$_COOKIE;
    return ( isset( $_BP_COOKIE['bp-' . $object . '-displaymode']) && $_BP_COOKIE['bp-' . $object . '-displaymode'] == 'grid');
}

/**
 * Get pro version
 */
function cc_get_pro_version(){
   $pro_enabler = get_template_directory() . DIRECTORY_SEPARATOR . '_pro' . DIRECTORY_SEPARATOR . 'pro-enabler.php';
   if(file_exists($pro_enabler)){
       require_once $pro_enabler;
   }
}
/**
 * Get Admin styles
 */
function cc_add_admin_styles(){
    wp_enqueue_style('cc_admin', get_template_directory_uri() . '/_inc/css/admin.css');
}
add_action('admin_init', 'cc_add_admin_styles');

/**
 * Fix ...[]
 */
function cc_replace_read_more($text){
    return ' <a class="read-more-link" href="'. get_permalink() . '"><br />' .  __("read more...","cc") . '</a>';
}
add_filter('excerpt_more', 'cc_replace_read_more');

/**
 * Display the rate for us message
 */
function cc_add_rate_us_notice(){
    $hide_message = get_option('cc_hide_activation_message', false);
    if(!$hide_message){
        echo '<div class="update-nag cc-rate-it">
                '.cc_get_add_rate_us_message().'<a href="#" class="dismiss-activation-message">'.__('Dismiss', 'cc'). '</a>
            </div>';
    }
}

function cc_get_add_rate_us_message(){
    return 'Please rate for <a class="go-to-wordpress-repo" href="http://wordpress.org/extend/themes/custom-community" target="_blank">Custom Community</a> theme on WordPress.org';
}
/**
 * Ajax processor for show/hide Please rate for
 */
add_action('wp_ajax_dismiss_activation_message', 'cc_dismiss_activation_message');
function cc_dismiss_activation_message(){
    echo update_option('cc_hide_activation_message', $_POST['value']);
    die();
}