<?php
/*
Title: Writing
Setting: piklist_wp_helpers
Tab: Writing
Order: 200
*/

global $wp_version;

  piklist('field', array(
    'type' => 'select'
    ,'field' => 'screen_layout_columns_post'
    ,'label' => 'Columns in editor'
    ,'value' => 'default'
    ,'attributes' => array(
      'class' => 'small-text'
    )
    ,'choices' => array(
      'default' => 'Default'
      ,'1' => '1'
      ,'2' => '2'
    )
  ));

  piklist('field', array(
    'type' => 'select'
    ,'field' => 'disable_visual_editor'
    ,'label' => 'Editor'
    ,'value' => 'false'
    ,'choices' => array(
      'true' => (($wp_version >= 3.5) ? 'Only show Text editor' : 'Only show HTML editor')
      ,'false' => (($wp_version >= 3.5) ? 'Show Visual and Text editors' : 'Show Visual and HTML editors')
    )
  ));

  piklist('field', array(
    'type' => 'radio'
    ,'field' => 'default_editor'
    ,'label' => 'Set Default Editor'
    ,'value' =>  'tinymce'
    ,'list' => true
    ,'choices' => array(
      'tinymce' => 'Visual'
      ,'html' => 'HTML'
    )
    ,'conditions' => array(
      array(
        'field' => 'disable_visual_editor'
        ,'value' => 'false'
      )
    )
  ));

  piklist('field', array(
    'type' => 'number'
    ,'field' => 'excerpt_box_height'
    ,'label' => 'Height of excerpt box'
    ,'description' => 'px'
    ,'value' => ''
    ,'attributes' => array(
      'class' => 'small-text'
    )
  ));

  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'disable_autosave'
    ,'label' => 'Disable Autosave'
    ,'description' => '"Preview mode" depends on Autosave. Disabling Autosave will also disable Preview.'
    ,'choices' => array(
      'true' => 'Stop WordPress from autosaving posts.'
    )
  ));
  
  piklist('field', array(
    'type' => 'number'
    ,'field' => 'edit_posts_per_page'
    ,'label' => 'Posts per page on edit screen.'
    ,'value' => ''
    ,'attributes' => array(
      'class' => 'small-text'
    )
  ));

if ($wp_version >= 3.5)
{

  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'xml_rpc'
    ,'label' => 'XML-RPC'
    ,'choices' => array(
      'true' => 'Disable XML RPC'
    )
  ));

}
  
?>