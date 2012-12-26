<?php

class PikList_Media
{
  private static $meta_boxes = array();
  
  private static $meta_box_nonce = false;
  
  public static function _construct()
  {    
    add_action('init', array('piklist_media', 'init'));
    
    add_filter('attachment_fields_to_save', array('piklist_media', 'process_form'), 10, 2);
    add_filter('attachment_fields_to_edit', array('piklist_media', 'attachment_fields_to_edit'), 100, 2);
  }

  public static function attachment_fields_to_edit($form_fields, $post)
  {
    if ($meta_boxes = self::meta_box($post))
    {
      $form_fields['_final'] = $meta_boxes . '<tr class="final"><td colspan="2">' . (isset($form_fields['_final']) ? $form_fields['_final'] : '');
    }
    
    return $form_fields;
  }
  
  public static function init()
  {   
    self::register_meta_boxes();
  }

  public static function register_meta_boxes()
  {
    piklist::process_views('media', array('piklist_media', 'register_meta_boxes_callback'));
  }

  public static function register_meta_boxes_callback($arguments)
  {
    extract($arguments);
    
    $current_user = wp_get_current_user();
    
    $data = get_file_data($path . '/parts/' . $folder . '/' . $part, array(
              'name' => 'Title'
              ,'description' => 'Description'
              ,'capability' => 'Capability'
              ,'role' => 'Role'
              ,'order' => 'Order'
              ,'Status' => 'Status'
              ,'new' => 'New'
              ,'id' => 'ID'
            ));
    
    $meta_box = array(
      'config' => $data
      ,'part' => $path . '/parts/' . $folder . '/' . $part
    );
    
    if ((!$data['capability'] || ($data['capability'] && current_user_can(strtolower($data['capability']))))
      && (!$data['role'] || in_array(strtolower($data['role']), $current_user->roles))
      && (!$data['new'] || ($data['new'] && !in_array($pagenow, array('async-upload.php', 'media-new.php'))))
    )
    {    
      if (isset($order))
      {
        self::$meta_boxes[$order] = $meta_box;
      }
      else
      {
        array_push(self::$meta_boxes, $meta_box);
      }
    }
  }

  public static function meta_box($post)
  {
    if (!empty(self::$meta_boxes))
    {
      ob_start();
      
      if (!self::$meta_box_nonce)
      {
        piklist_form::render_field(array(
          'type' => 'hidden'
          ,'field' => 'nonce'
          ,'value' => wp_create_nonce('piklist/piklist.php')
          ,'scope' => 'piklist'
        ));
      
        self::$meta_box_nonce = true;
      }
      
      $GLOBALS['piklist_attachment'] = $post;
      
      foreach (self::$meta_boxes as $meta_box)
      {
        piklist::render('shared/meta-box-start', array(
          'meta_box' => $meta_box
          ,'wrapper' => 'media_meta'
        ), false);
  
        piklist::render($meta_box['part'], array(
          'post_id' => $post->ID
          ,'prefix' => 'piklist'
          ,'plugin' => 'piklist'
        ), false);
                
        piklist::render('shared/meta-box-end', array(
          'meta_box' => $meta_box
          ,'wrapper' => 'media_meta'
        ), false);
      }
      
      unset($GLOBALS['piklist_attachment']);
      
      $output = ob_get_contents();
      
      ob_end_clean();
      
      return $output;
    }
    
    return null;
  }
  
  public static function process_form($post, $attachment)
  {
    piklist_form::process_form(array(
      'post' => $post['ID']
    ));

    return $post;
  }
}

?>