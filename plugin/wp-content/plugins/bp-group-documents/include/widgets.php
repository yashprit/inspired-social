<?php
// Exit if accessed directly
if (!defined('ABSPATH'))
    exit;

/**
 * @todo, 1/5/2013 check if we really don't need the plugins loaded action
 * @todo, 1/5/2013, add the icon into the stylesheet
 */
//function bp_group_documents_register_widgets() {
//    add_action('widgets_init', create_function('', 'register_widget("BP_Group_Documents_Popular_Widget");'));
//}
//add_action('plugins_loaded', 'bp_group_documents_register_widgets');

add_action('widgets_init', create_function('', 'register_widget( "BP_Group_Documents_Newest_Widget" );'));
add_action('widgets_init', create_function('', 'register_widget("BP_Group_Documents_Popular_Widget");'));
add_action('widgets_init', create_function('', 'register_widget("BP_Group_Documents_Usergroups_Widget");'));



if ((is_active_widget(false, false, 'bp_group_documents_newest_widget')) || (is_active_widget(false, false, 'bp_group_documents_popular_widget'))) {
    add_action('wp_enqueue_scripts', 'bp_group_documents_add_my_stylesheet');
}

/**
 * Enqueue plugin style-file
 */
function bp_group_documents_add_my_stylesheet() {
    // Respects SSL, Style.css is relative to the current file
    wp_register_style('bp-group-documents', WP_PLUGIN_URL . '/' . BP_GROUP_DOCUMENTS_DIR . '/css/style.css', false, BP_GROUP_DOCUMENTS_VERSION);
    wp_enqueue_style('bp-group-documents');
}

/**
 * @version 2, 1/5/2013, stergatu
 *  
 */
class BP_Group_Documents_Newest_Widget extends WP_Widget {

    var $bp_group_documents_name;

    public function __construct() {
        global $bp;
        $nav_page_name = get_option('bp_group_documents_nav_page_name');
        $this->bp_group_documents_name = mb_convert_case(!empty($nav_page_name) ? $nav_page_name : __('Documents', 'bp-group-documents'), MB_CASE_LOWER);
        parent::__construct(
                'bp_group_documents_newest_widget', '(BP Group Documents) ' . sprintf(__('Recent Group %s', 'bp-group-documents'), $this->bp_group_documents_name), // Name
                array('description' => sprintf(__('The most recently uploaded group %s. Only from public groups', 'bp-group-documents'), $this->bp_group_documents_name),
            'classname' => 'bp_group_documents_widget',) // Args
        );
    }

    /**
     * @version 2, 1/5/2013, stergatu
     *  
     */
    function widget($args, $instance) {
        global $bp;
        extract($args);
        $title = apply_filters('widget_title', empty($instance['title']) ? sprintf(__('Recent Group %s', 'bp-group-documents'), $this->bp_group_documents_name) : $instance['title']);
        echo $before_widget;
        echo $before_title .
        $title .
        $after_title;

        do_action('bp_group_documents_newest_widget_before_html');

//	eleni comment on 1/5/2013
        /*        $group_id = $bp->groups->current_group->id;
          //        if ($group_id > 0) {
          //            $instance['group_filter'] = $group_id;
          //        }
         */
        $document_list = BP_Group_Documents::get_list_for_newest_widget($instance['num_items'], $instance['group_filter'], $instance['featured']);
        if ($document_list && count($document_list) >= 1) {
            echo '<ul id="bp-group-documents-recent" class="bp-group-documents-list" >';
            foreach ($document_list as $item) {
                $document = new BP_Group_Documents($item['id']);
                $group = new BP_Groups_Group($document->group_id);
                echo '<li>';
                if (get_option('bp_group_documents_display_icons')) {
                    $document->icon();
                }
                ?>
                                <a class="bp-group-documents-title" id="group-document-link-<?php echo $document->id; ?>" href="<?php $document->url(); ?>" target="_blank">
                    <?php echo $document->name; ?></a>
                <?php
                if (!$instance['group_filter']) {
                    echo sprintf(__('posted in %s', 'bp-group-documents'), '<a href="' . bp_get_group_permalink($group) . '">' . esc_attr($group->name) . '</a>');
                }
                echo '</li>';
            }
            echo '</ul>';
        } else {
            echo '<div class="widget-error">' . sprintf(__('There are no %s to display.', 'bp-group-documents'), $this->bp_group_documents_name) . '</div></p>';
        }
        echo $after_widget;
    }

    /**
     * 
     * @param type $new_instance
     * @param type $old_instance
     * @return type
     * @todo, 25/4/2013, stergatu, add functionality for documents_category selection (minor)
     */
    function update($new_instance, $old_instance) {
        do_action('bp_group_documents_widget_update');

        $instance = $old_instance;
        $instance['title'] = strip_tags($new_instance['title']);
        $instance['group_filter'] = strip_tags($new_instance['group_filter']);
        $instance['featured'] = strip_tags($new_instance['featured']);
        $instance['num_items'] = strip_tags($new_instance['num_items']);

        return $instance;
    }

    /**
     * 
     * @param type $instance
     * @todo, 25/4/2013, stergatu, add functionality for documents_category selection (minor)
     */
    function form($instance) {
        do_action('bp_group_documents_newest_widget_form');

        $instance = wp_parse_args((array) $instance, array('title' => '', 'num_items' => 5));

        $title = esc_attr($instance['title']);
        $group_filter = esc_attr($instance['group_filter']);
        $featured = esc_attr($instance['featured']);
        $num_items = esc_attr($instance['num_items']);
        ?>
                <p><label><?php _e('Title:', 'bp-group-documents'); ?></label><input class="widefat" id="<?php echo $this->get_field_id('title'); ?>" name="<?php echo $this->get_field_name('title'); ?>" type="text" value="<?php echo $title; ?>" /></p>
        <?php if (BP_GROUP_DOCUMENTS_WIDGET_GROUP_FILTER) { ?>
            <p><label><?php _e('Filter by Group:', 'bp-group-documents'); ?></label>
                <select id="<?php echo $this->get_field_id('group_filter'); ?>" name="<?php echo $this->get_field_name('group_filter'); ?>" >
                    <option value="0"><?php _e('Select Group...', 'bp-group-documents'); ?></option>
                    <?php
                    $groups_list = BP_Groups_Group::get('alphabetical');
                    foreach ($groups_list['groups'] as $group) {
                        echo '<option value="' . $group->id . '" ';
                        if ($group->id == $group_filter)
                            echo 'selected="selected"';
                        echo '>' . stripslashes($group->name) . '</option>';
                    }
                    ?>
                                        </select></p>
                        <?php
                    }

                    if (BP_GROUP_DOCUMENTS_FEATURED) {
                        ?>
                        <p><input type="checkbox" id="<?php echo $this->get_field_id('featured'); ?>" name="<?php echo $this->get_field_name('featured'); ?>" value="1" <?php
                                  if ($featured)
                                      echo 'checked="checked"';
                ?>>
                                        <label><?php printf(__('Show featured %s only', 'bp-group-documents'), $this->bp_group_documents_name); ?></label></p>
                    <?php } ?>
                    <p><label><?php _e('Number of items to show:', 'bp-group-documents'); ?></label> <input class="widefat" id="<?php echo $this->get_field_id('num_items'); ?>" name="<?php echo $this->get_field_name('num_items'); ?>" type="text" value="<?php echo esc_attr($num_items); ?>" style="width: 30%" /></p>
                    <?php
                }

            }

/**
 * @version 3, 13/5/2013, stergatu
 */
class BP_Group_Documents_Popular_Widget extends WP_Widget {

    var $bp_group_documents_name;

    function __construct() {
        global $bp;
        $nav_page_name = get_option('bp_group_documents_nav_page_name');
        $this->bp_group_documents_name = mb_convert_case(!empty($nav_page_name) ? $nav_page_name : __('Documents', 'bp-group-documents'), MB_CASE_LOWER);
        parent::__construct(
                'bp_group_documents_popular_widget', '(BP Group Documents) ' . sprintf(__('Popular Group %s', 'bp-group-documents'), $this->bp_group_documents_name), // Name
                array('description' => sprintf(__('The most commonly downloaded group %s. Only for public groups', 'bp-group-documents'), $this->bp_group_documents_name),
            'classname' => 'bp_group_documents_widget')
        );

        if (is_active_widget(false, false, $this->id_base)) {
            add_action('', 'bp_group_documents_add_my_stylesheet');
        }
    }

    function widget($args, $instance) {
        global $bp;

        extract($args);
        $title = apply_filters('widget_title', empty($instance['title']) ? sprintf(__('Popular Group %s', 'bp-group-documents'), $this->bp_group_documents_name) : $instance['title']);

        echo $before_widget . $before_title . $title . $after_title;

                do_action('bp_group_documents_popular_widget_before_html');

                /*                 * *
                 * Main HTML Display
                 */
        //	eleni comment on 1/5/2013 
//        $group_id = $bp->groups->current_group->id;
//        if ($group_id > 0) {
//            $instance['group_filter'] = $group_id;
//        }

        $document_list = BP_Group_Documents::get_list_for_popular_widget($instance['num_items'], $instance['group_filter'], $instance['featured']);

        if ($document_list && count($document_list) >= 1) {
            echo '<ul id="bp-group-documents-recent" class="bp-group-documents-list">';
            foreach ($document_list as $item) {
                $document = new BP_Group_Documents($item['id']);
                $group = new BP_Groups_Group($document->group_id);
                echo '<li>';
                if (get_option('bp_group_documents_display_icons')) {
                    $document->icon();
                }
                ?>
        <a class="bp-group-documents-title" id="group-document-link-<?php echo $document->id; ?>" href="<?php $document->url(); ?>" target="_blank">
                            <?php echo $document->name; ?></a>

        <br>
                        <?php
                        if (!$instance['group_filter']) {
                            echo sprintf(__('posted in %s', 'bp-group-documents'), '<a href="' . bp_get_group_permalink($group) . '">' . esc_attr($group->name) . '</a>.');
                }
                if ($instance['download_count']) {
                                    echo ' <span class="group-documents-download-count">' .
                                    $document->download_count . ' ' . __('downloads', 'bp-group-documents') .
                                    '</span>';
                                }
                                echo '</li>';
            }
            echo '</ul>';
        } else {
            echo '<div class="widget-error">' . sprintf(__('There are no %s to display.', 'bp-group-documents'), $this->bp_group_documents_name) . '</div></p>';
        }
        echo $after_widget;
                    }

                    function update($new_instance, $old_instance) {
                        do_action('bp_group_documents_newest_widget_update');

        $instance = $old_instance;
        $instance['title'] = strip_tags($new_instance['title']);
        $instance['group_filter'] = strip_tags($new_instance['group_filter']);
        $instance['featured'] = strip_tags($new_instance['featured']);
        $instance['num_items'] = strip_tags($new_instance['num_items']);
        $instance['download_count'] = strip_tags($new_instance['download_count']);

        return $instance;
    }

    function form($instance) {
        do_action('bp_group_documents_newest_widget_form');

        $instance = wp_parse_args((array) $instance, array('num_items' => 5));
        $title = esc_attr($instance['title']);
        $group_filter = esc_attr($instance['group_filter']);
        $featured = esc_attr($instance['featured']);
        $num_items = esc_attr($instance['num_items']);
        $download_count = esc_attr($instance['download_count']);
                        ?>

                <p><label><?php _e('Title:', 'bp-group-documents'); ?></label><input class="widefat" id="<?php echo $this->get_field_id('title'); ?>" name="<?php echo $this->get_field_name('title'); ?>" type="text" value="<?php echo $title; ?>" /></p>
                        <?php if (BP_GROUP_DOCUMENTS_WIDGET_GROUP_FILTER) { ?>
                            <p><label><?php _e('Filter by Group:', 'bp-group-documents'); ?></label>
                                <select id="<?php echo $this->get_field_id('group_filter'); ?>" name="<?php echo $this->get_field_name('group_filter'); ?>" >
                                    <option value="0"><?php _e('Select Group...', 'bp-group-documents'); ?></option>
                                    <?php
                                    $groups_list = BP_Groups_Group::get('alphabetical');
//                                get_alphabetically();
                    foreach ($groups_list['groups'] as $group) {
                        echo '<option value="' . $group->id . '" ';
                        if ($group->id == $group_filter)
                            echo 'selected="selected"';
                        echo '>' . stripslashes($group->name) . '</option>';
                    }
                    ?>
                                        </select></p>
                    <?php }
                    if (BP_GROUP_DOCUMENTS_FEATURED) {
                        ?>
                        <p><input type="checkbox" id="<?php echo $this->get_field_id('featured'); ?>" name="<?php echo $this->get_field_name('featured'); ?>" value="1" <?php
                                  if ($featured)
                                      echo 'checked="checked"';
                ?>>
                                        <label><?php printf(__('Show featured %s only', 'bp-group-documents'), $this->bp_group_documents_name); ?></label></p>
                    <?php } ?>

            <p><label><?php _e('Number of items to show:', 'bp-group-documents'); ?></label> <input class="widefat" id="<?php echo $this->get_field_id('num_items'); ?>" name="<?php echo $this->get_field_name('num_items'); ?>" type="text" value="<?php echo esc_attr($num_items); ?>" style="width: 30%" /></p>
                    <p><input type="checkbox" id="<?php echo $this->get_field_id('download_count'); ?>" name="<?php echo $this->get_field_name('download_count'); ?>" value="1" <?php
                        if ($download_count)
                            echo 'checked="checked"';
                        ?>>
                        <label><?php printf(__('Show downloads', 'bp-group-documents'), $this->bp_group_documents_name); ?></label></p>
                    <?php
                }

            }

/**
 * @version 3, 13/5/2013, stergatu
 */
class BP_Group_Documents_Usergroups_Widget extends WP_Widget {

    var $bp_group_documents_name;

    function __construct() {
        global $bp;
        $nav_page_name = get_option('bp_group_documents_nav_page_name');
        $this->bp_group_documents_name = !empty($nav_page_name) ? $nav_page_name : __('Documents', 'bp-group-documents');
        parent::__construct(
                'bp_group_documents_usergroups_widget', '(BP Group Documents) ' . sprintf(__('%s in your groups', 'bp-group-documents'), $this->bp_group_documents_name), // Name
                        array('description' => sprintf(__('%s for a logged in user\'s groups.', 'bp-group-documents'), $this->bp_group_documents_name),
                    'classname' => 'bp_group_documents_widget')
                );

        if (is_active_widget(false, false, $this->id_base)) {
            add_action('', 'bp_group_documents_add_my_stylesheet');
        }
    }

    function widget($args, $instance) {
        global $bp;
        //only show widget to logged in users
        if (!is_user_logged_in())
            return;

        //get the groups the user is part of
        $results = groups_get_user_groups(get_current_user_id());
        //don't show widget if user doesn't have any groups
        if ($results['total'] == 0)
            return;
        extract($args);
        $title = apply_filters('widget_title', empty($instance['title']) ? sprintf(__('Recent %s from your Groups', 'bp-group-documents'), $this->bp_group_documents_name) : $instance['title']);

                echo $before_widget . $before_title . $title . $after_title;

                do_action('bp_group_documents_usergroups_widget_before_html');
                $document_list = BP_Group_Documents::get_list_for_usergroups_widget($instance['num_items'], $instance['featured']);

                if ($document_list && count($document_list) >= 1) {
            echo '<ul id="bp-group-documents-usergroups" class="bp-group-documents-list">';
            foreach ($document_list as $item) {
                $document = new BP_Group_Documents($item['id']);
                $group = new BP_Groups_Group($document->group_id);
                echo '<li>';
                if (get_option('bp_group_documents_display_icons')) {
                    $document->icon();
                }
                ?>
        <a class="bp-group-documents-title" id="group-document-link-<?php echo $document->id; ?>" 
                           href="<?php $document->url(); ?>" target="_blank">
                            <?php echo $document->name; ?></a>
                        <?php
                        echo sprintf(__('posted in %s', 'bp-group-documents'), '<a href="' . bp_get_group_permalink($group) . '">' . esc_attr($group->name) . '</a>');

                        echo '</li>';
            }
            echo '</ul>';
        } else {
            echo '<div class="widget-error">' . sprintf(__('There are no %s to display.', 'bp-group-documents'), $this->bp_group_documents_name) . '</div></p>';
                }
                echo $after_widget;
            }

            function update($new_instance, $old_instance) {
                do_action('bp_group_documents_usergroups_widget_update');

        $instance = $old_instance;
        $instance['title'] = strip_tags($new_instance['title']);
        $instance['featured'] = strip_tags($new_instance['featured']);
        $instance['num_items'] = strip_tags($new_instance['num_items']);

        return $instance;
    }

    function form($instance) {
        do_action('bp_group_documents_usergroups_widget_form');

        $instance = wp_parse_args((array) $instance, array('num_items' => 5));
        $title = esc_attr($instance['title']);
        $featured = esc_attr($instance['featured']);
        $num_items = esc_attr($instance['num_items']);
        ?>

                        <p><label><?php _e('Title:', 'bp-group-documents'); ?></label><input class="widefat" id="<?php echo $this->get_field_id('title'); ?>" name="<?php echo $this->get_field_name('title'); ?>" type="text" value="<?php echo $title; ?>" /></p>
                <?php if (BP_GROUP_DOCUMENTS_FEATURED) { ?>
                    <p><input type="checkbox" id="<?php echo $this->get_field_id('featured'); ?>" name="<?php echo $this->get_field_name('featured'); ?>" value="1" <?php
                        if ($featured)
                            echo 'checked="checked"';
                        ?>>
                        <label><?php printf(__('Show featured %s only', 'bp-group-documents'), $this->bp_group_documents_name); ?></label></p>
                <?php } ?>

            <p><label><?php _e('Number of items to show:', 'bp-group-documents'); ?></label> <input class="widefat" id="<?php echo $this->get_field_id('num_items'); ?>" name="<?php echo $this->get_field_name('num_items'); ?>" type="text" value="<?php echo esc_attr($num_items); ?>" style="width: 30%" /></p>
                    <?php
                }

            }