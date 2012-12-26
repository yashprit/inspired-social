<?php

class PikList_Widget
{
  public static $current_widget = null;
  
  private static $widget_classes = array();
  
  public static function _construct()
  {    
    add_action('init', array('piklist_widget', 'init'));
    add_action('widgets_init', array('piklist_widget', 'widgets_init'));
    
    add_filter('dynamic_sidebar_params', array('piklist_widget', 'dynamic_sidebar_params'));
  }

  public static function init()
  {   
    self::register_sidebars();
  }
  
  public static function register_sidebars()
  {
    $sidebars = apply_filters('piklist_sidebars', array());
    
    foreach ($sidebars as $sidebar)
    {
      register_sidebar(array_merge(array(
        'name' => __($sidebar['name'])
        ,'id' => sanitize_title_with_dashes($sidebar['name'])
        ,'description' => isset($sidebar['description']) ? __($sidebar['description']) : null
        ,'before_widget' => isset($sidebar['before_widget']) ? $sidebar['before_widget'] : '<div id="%1$s" class="widget-container %2$s">'
        ,'after_widget' => isset($sidebar['after_widget']) ? $sidebar['after_widget'] : '</div>'
        ,'before_title' => isset($sidebar['before_title']) ? $sidebar['before_title'] : '<h3 class="widget-title">'
        ,'after_title' => isset($sidebar['after_title']) ? $sidebar['after_title'] : '</h3>'
     ), $sidebar));
    }
  }
  
  public static function widgets_init()
  {
    global $wp_widget_factory;
    
    $widget_class = 'piklist_universal_widget';

    foreach (piklist::$paths as $from => $path)
    {
      if (!piklist::directory_empty($path . '/parts/widgets'))
      {
        $widget_class_name = $widget_class . '_' . piklist::slug($from);
      
        if (isset(piklist_add_on::$available_add_ons[$from]))
        {
          $title = piklist_add_on::$available_add_ons[$from]['Name'] . ' ' . __('Widgets','piklist');
          $description = strip_tags(piklist_add_on::$available_add_ons[$from]['Description']);
        }
        else if ($from == 'plugin')
        {
          $title = __('Piklist Widgets','piklist');
          $description = __('Core Widgets for Piklist.','piklist');
        }
        else if ($from == 'theme')
        {
          global $wp_version;
          
          $current_theme = ($wp_version >= 3.4 ? wp_get_theme() : get_current_theme());

          $title = $current_theme . ' ' . __('Widgets','piklist');
          $description = sprintf(__('Widgets for the %s Theme', 'piklist'), $current_theme);
        }

        $wp_widget_factory->widgets[$widget_class_name] = new $widget_class($widget_class_name, $title, $description, array($from => $path));
      }
    }
  }
  
  public static function widget()
  {
    global $wp_widget_factory;

    return $wp_widget_factory->widgets[self::$current_widget];
  }

  public static function dynamic_sidebar_params($params) 
  {
    $id = $params[0]['id'];
    
    if (!isset(self::$widget_classes[$id]))
    {
      self::$widget_classes[$id] = 0;
    }
    self::$widget_classes[$id]++;

    $class = 'class="widget-' . self::$widget_classes[$id] . ' ';

    if (self::$widget_classes[$id] % 2)
    {
      $class .= 'widget-even ';
      $class .= 'widget-alt ';
    }
    else
    {
      $class .= 'widget-odd ';
    }

    $params[0]['before_widget'] = str_replace('class="', $class, $params[0]['before_widget']);

    return $params;
  }
}

?>