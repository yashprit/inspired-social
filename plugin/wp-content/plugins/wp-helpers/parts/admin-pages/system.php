<?php
/*
Title: WordPress
Page: piklist_wp_helpers_system_information
Save: false
*/
?>

<style type="text/css">
  .widefat td, .widefat th {
    border-bottom-color: #DFDFDF;
    border-top-color: #DFDFDF;
  }

  .widefat td.e {
    width: 10%;
  }

  .widefat td.v {
    width: 90%;
  }

</style>

<?php

piklist_site_inventory();
clean_phpinfo();



function clean_phpinfo()
{
  ob_start();
  phpinfo();
  $phpinfo = ob_get_contents();
  ob_end_clean();

  $phpinfo = preg_replace( '%^.*<body>(.*)</body>.*$%ms','$1',$phpinfo);
  $phpinfo = str_ireplace('width="600"','class="form-table widefat"',$phpinfo);

  echo $phpinfo;
}



// The Vast major of this code comes from the super awesome WPHelpcenter plugin.
// http://wordpress.org/extend/plugins/wphelpcenter/
function piklist_site_inventory()
{
  global $wp_version;

  $plugins_active = '';
  $plugins_inactive =' ';

  // WordPress Environment
  $theme = wp_get_theme(wp_get_theme());
  $themename = $theme['Name'];
  $themeversion = $theme['Version'];
  $themeauthor = strip_tags($theme['Author']);
  $wp_version = get_bloginfo('version');
  is_multisite() ? $wp_type = __('WordPress Multisite', 'piklist') : $wp_type = __('WordPress (single user)', 'piklist');
  get_option('permalink_structure') == '' ? $permalinks = __('Query String (index.php?p=123)', 'piklist') : $permalinks = __('Pretty Permalinks', 'piklist');
  get_option('users_can_register') == '1' ? $reg = __('Yes', 'piklist') : $reg = __('No', 'piklist');
  get_option('enable_xmlrpc') == '1' ? $xmprpc = __('Yes', 'piklist') : $xmprpc = __('No', 'piklist');
  get_option('enable_app') == '1' ? $atompub = __('Yes', 'piklist') : $atompub = __('No', 'piklist');
  get_option('blog_public') == '1' ? $privacy = __('Public', 'piklist') : $privacy = __('Private', 'piklist');
  get_option('rss_use_excerpt') == '1' ? $feed = __('Summaries', 'piklist') : $feed = __('Full Content', 'piklist');
  
  
  // Plugins
  $all_plugins = get_plugins();
  $active_plugins = array();
  $inactive_plugins = array();
  foreach ( (array)$all_plugins as $plugin_file => $plugin_data)
  {
    if ( is_plugin_active($plugin_file) )
    {
      $active_plugins[ $plugin_file ] = $plugin_data;
    }
    else
    {
      $inactive_plugins[ $plugin_file ] = $plugin_data;
    }
  }

  foreach ( (array)$active_plugins as $plugin_file => $plugin_data)
  {
    $plugins_active .= '&bull; ' . strip_tags($plugin_data['Title']). " " .strip_tags($plugin_data['Version']). " " . __('by:', 'piklist') .  " " . strip_tags($plugin_data['Author']) . PHP_EOL ;
  }

  foreach ( (array)$inactive_plugins as $plugin_file => $plugin_data)
  {
    $plugins_inactive .= '&bull; ' . strip_tags($plugin_data['Title']). " " .strip_tags($plugin_data['Version']). " " . __('by:', 'piklist') .  " " . strip_tags($plugin_data['Author']) . PHP_EOL ;
  }


  // Widgets
  $sidebar_widgets = '';
  $current_sidebar = '';
  $active_widgets = get_option('sidebars_widgets');
  if (is_array($active_widgets) && count($active_widgets))
  {
    foreach ($active_widgets as $sidebar => $widgets)
    {
      if (is_array($widgets))
      {
        if ($sidebar != $current_sidebar)
        {
          $sidebar_widgets .= '&bull; ' . $sidebar . ': ';
          $current_sidebar = $sidebar;
        }
        if (count($widgets))
        {
          $sidebar_widgets .= implode(', ', $widgets);
        }
        else
        {
          $sidebar_widgets .= __('(none)', 'piklist');
        }
        
        $sidebar_widgets .= PHP_EOL;
      }
    }
  }

?>

<?php printf(__('%1$s %3$sThis information can be used to help debug your website or just provide quick access to important information.%4$s %3$sTo email, just copy and paste.%4$s %3$s %5$sNEVER%6$s place this information on a public forum.%4$s %2$s','piklist'),'<ul>','</ul>','<li>','</li>','<strong>','</strong>');?>
<textarea style="width:90%; height:500px;">
<?php printf(__('== SERVER ENVIRONMENT ==%1$s','piklist'),PHP_EOL);?>
<?php printf(__('%1$s PHP Version: %2$s %3$s','piklist'),'&bull;', phpversion(), PHP_EOL);?>
<?php printf(__('%1$s PHP Extensions: %2$s %3$s','piklist'),'&bull;', implode(', ', get_loaded_extensions()), PHP_EOL);?>
<?php printf(__('%1$s MYSQL Version: %2$s %3$s','piklist'),'&bull;', mysql_get_server_info(), PHP_EOL);?>
<?php printf(__('%1$s Web Server: %2$s %3$s','piklist'),'&bull;', $_SERVER['SERVER_SOFTWARE'], PHP_EOL);?>

<?php printf(__('== WORDPRESS ENVIRONMENT ==%1$s','piklist'),PHP_EOL);?>
<?php printf(__('%1$s Type: %2$s %3$s','piklist'),'&bull;', $wp_type, PHP_EOL);?>
<?php printf(__('%1$s Version: %2$s %3$s','piklist'),'&bull;', $wp_version, PHP_EOL);?>
<?php printf(__('%1$s File Path: %2$s %3$s','piklist'),'&bull;', ABSPATH, PHP_EOL);?>
<?php printf(__('%1$s Site URL: %2$s %3$s','piklist'),'&bull;', get_bloginfo('url'), PHP_EOL);?>
<?php printf(__('%1$s WordPress URL: %2$s %3$s','piklist'),'&bull;', get_bloginfo('wpurl'), PHP_EOL);?>
<?php printf(__('%1$s Permalink Type: %2$s %3$s','piklist'),'&bull;', $permalinks, PHP_EOL);?>
<?php printf(__('%1$s Registration Enabled: %2$s %3$s','piklist'),'&bull;', $reg, PHP_EOL);?>
<?php printf(__('%1$s XML-RPC Enabled: %2$s %3$s','piklist'),'&bull;', $xmprpc, PHP_EOL);?>
<?php printf(__('%1$s Atom Pub Enabled: %2$s %3$s','piklist'),'&bull;', $atompub, PHP_EOL);?>
<?php printf(__('%1$s Privacy Settings: %2$s %3$s','piklist'),'&bull;', $privacy, PHP_EOL);?>
<?php printf(__('%1$s Feed Content: %2$s %3$s','piklist'),'&bull;', $feed, PHP_EOL);?>

<?php printf(__('== APPEARANCE ==%1$s','piklist'),PHP_EOL);?>
<?php printf(__('Active Theme: %1$s %2$s by %3$s %4$s','piklist'),$themename, $themeversion, $themeauthor, PHP_EOL);?>

<?php printf(__('== ACTIVE PLUGINS ==%1$s','piklist'),PHP_EOL);?>
<?php echo $plugins_active;?>

<?php printf(__('== INACTIVE PLUGINS ==%1$s','piklist'),PHP_EOL);?>
<?php echo $plugins_inactive;?>

<?php printf(__('== SIDEBARS / WIDGETS ==%1$s','piklist'),PHP_EOL);?>
<?php echo $sidebar_widgets;?>
</textarea>

<?php
}


?>