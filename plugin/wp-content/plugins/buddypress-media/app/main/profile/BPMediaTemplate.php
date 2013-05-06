<?php

/**
 * Contains methods for template functions
 *
 * @package BuddyPressMedia
 * @subpackage Profile
 *
 * @author Saurabh Shukla <saurabh.shukla@rtcamp.com>
 * @author Gagandeep Singh <gagandeep.singh@rtcamp.com>
 */
class BPMediaTemplate {

    /**
     *
     * @global type $bp_media_current_album
     */
    function upload_form_multiple() {
        global $bp_media_current_album, $bp_media_query;
        $post_max_size = ini_get('post_max_size');
        $upload_max_filesize = ini_get('upload_max_filesize');
        $numeric_pms = (int)str_replace(array('K', 'M', 'G'), array('000', '000000', '000000000'), $post_max_size);
        $numeric_umf = (int)str_replace(array('K', 'M', 'G'), array('000', '000000', '000000000'), $upload_max_filesize);
        if ( $numeric_pms < $numeric_umf ) {
            $size_limit = $post_max_size;
        } else {
            $size_limit = $upload_max_filesize;
        }
        $post_wall = __('Wall Posts', 'buddypress-media');
        if ($bp_media_query && $bp_media_query->have_posts()) {
            $hidden = ' style="display:none;"';
            ?>
            &nbsp;<input id="bp-media-upload-button" type="button" value="Upload" class="button"><?php
        } else {
            $hidden = '';
        }
        ?>
        <div id="bp-media-upload-ui" class="hide-if-no-js drag-drop"<?php echo $hidden; ?>>
            <div id="drag-drop-area">
                <?php if (isset($bp_media_current_album)) { ?>
                    <input type="hidden" id="bp-media-selected-album" value="<?php echo $bp_media_current_album->get_id(); ?>"/>
        <?php } else { ?>
                    <div id="bp-media-album-prompt" title="Album">
                        <span><?php _e('Select Album: ', 'buddypress-media'); ?></span>
                        <span class="bp-media-album-content">
                            <select id="bp-media-selected-album"><?php
            if (bp_is_current_component('groups')) {
                $albums = new WP_Query(array(
                            'post_type' => 'bp_media_album',
                            'posts_per_page' => -1,
                            'meta_key' => 'bp-media-key',
                            'meta_value' => -bp_get_current_group_id(),
                            'meta_compare' => '='
                        ));
            } else {
                $albums = new WP_Query(array(
                            'post_type' => 'bp_media_album',
                            'posts_per_page' => -1,
                            'author' => get_current_user_id(),
                            'meta_key' => 'bp-media-key',
                            'meta_value' => get_current_user_id(),
                            'meta_compare' => '='
                    
                        ));
            }
            if (isset($albums->posts) && is_array($albums->posts) && count($albums->posts) > 0) {
                foreach ($albums->posts as $album) {
                    if ($album->post_title == $post_wall)
                        echo '<option value="' . $album->ID . '" selected="selected">' . $album->post_title . '</option>';
                    else
                        echo '<option value="' . $album->ID . '">' . $album->post_title . '</option>';
                };
            }else {
                $album = new BPMediaAlbum();
                if (bp_is_current_component('groups')) {
                    $current_group = new BP_Groups_Group(bp_get_current_group_id());
                    $album->add_album($post_wall, $current_group->creator_id, bp_get_current_group_id());
                } else {
                    $album->add_album($post_wall, bp_loggedin_user_id());
                }
                echo '<option value="' . $album->get_id() . '" selected="selected">' . $album->get_title() . '</option>';
            }
            echo '<option id="create-new" value="create_new" >' . __('+ Create New Album', 'buddypress-media') . '</option>';
            ?>
                            </select>
                        </span>
                        <div class="hide">
                            <input type="text" id="bp_media_album_new" value="" placeholder="Album Name" /><br/>
                            <input type="button" class="button" id="btn-create-new" value="<?php _e('Create', 'buddypress-media'); ?>"/>
                            <input type="button" class="button" id="btn-create-cancel" value="<?php _e('Cancel', 'buddypress-media'); ?>"/>
                        </div>
                    </div>
                    <div id="bp-media-album-in"><span><?php _e('&', 'buddypress-media'); ?></span></div>    
        <?php } ?>
                <div class="drag-drop-inside">
                    <span class="drag-drop-info"><?php _e('Drop files here', 'buddypress-media'); ?></span> 
                    <span id="bp-media-album-or"><?php _e(' or ', 'buddypress-media'); ?></span> 
                    <span class="drag-drop-buttons"><input id="bp-media-upload-browse-button" type="button" value="<?php _e('Upload Media', 'buddypress-media'); ?>" class="button" />(<?php _e('Max Upload Size', 'buddypress-media');echo ': '.$size_limit; ?>)</span>
                </div>
            </div>
            <div id="bp-media-uploaded-files"></div>
        </div>
        <?php
    }

    /**
     *
     * @param type $id
     * @return boolean
     */
    function get_permalink($id = 0) {
        if (is_object($id))
            $media = $id;
        else
            $media = &get_post($id);
        if (empty($media->ID))
            return false;
        if (!$media->post_type == 'bp_media')
            return false;
        switch (get_post_meta($media->ID, 'bp_media_type', true)) {
            case 'video' :
                return trailingslashit(bp_displayed_user_domain() . BP_MEDIA_VIDEOS_SLUG . '/' . BP_MEDIA_VIDEOS_VIEW_SLUG . '/' . $media->ID);
                break;
            case 'audio' :
                return trailingslashit(bp_displayed_user_domain() . BP_MEDIA_AUDIO_SLUG . '/' . BP_MEDIA_AUDIO_VIEW_SLUG . '/' . $media->ID);
                break;
            case 'image' :
                return trailingslashit(bp_displayed_user_domain() . BP_MEDIA_IMAGES_SLUG . '/' . BP_MEDIA_IMAGES_VIEW_SLUG . '/' . $media->ID);
                break;
            default :
                return false;
        }
    }

    function the_permalink() {
        echo apply_filters('the_permalink', array($this, 'get_permalink'));
    }

    /**
     *
     * @param type $id
     * @return boolean
     */
    function the_content($id = 0) {
        if (is_object($id))
            $media = $id;
        else
            $media = &get_post($id);
        if (empty($media->ID))
            return false;
        if ($media->post_type != 'attachment')
            return false;
        try {
            $media = new BPMediaHostWordpress($media->ID);
            echo $media->get_media_gallery_content();
        } catch (Exception $e) {
            echo '';
        }
    }

    /**
     *
     * @param type $id
     * @return boolean
     */
    function the_album_content($id = 0) {
        if (is_object($id))
            $album = $id;
        else
            $album = &get_post($id);
        if (empty($album->ID))
            return false;
        if (!$album->post_type == 'bp_media_album')
            return false;
        try {
            $album = new BPMediaAlbum($album->ID);
            echo $album->get_album_gallery_content();
        } catch (Exception $e) {
            echo '';
        }
    }

    /**
     *
     * @global type $bp_media_query
     * @global type $bp_media_albums_query
     * @param type $type
     */
    function show_more($type = 'media') {
        $showmore = false;
        global $bp_media;
        $count = $bp_media->default_count();
        switch ($type) {
            case 'media':
                global $bp_media_query;
                //found_posts
                if (bp_is_my_profile() || BPMediaGroupLoader::can_upload()) {
                    if (isset($bp_media_query->found_posts) && $bp_media_query->found_posts > ($count - 1))
                        $showmore = true;
                } else {
                    if (isset($bp_media_query->found_posts) && $bp_media_query->found_posts > $count)
                        $showmore = true;
                }
                break;
            case 'albums':
                global $bp_media_albums_query;
                if (isset($bp_media_albums_query->found_posts) && $bp_media_albums_query->found_posts > $count) {
                    $showmore = true;
                }
                break;
        }
        if ($showmore) {
            echo '<div class="bp-media-actions"><a href="#" class="button" id="bp-media-show-more">' . __('Show More', 'buddypress-media') . '</a></div>';
        }
    }

    /**
     *
     */

    /**
     *
     * @param type $mediaconst
     */
    function redirect($mediaconst) {
        bp_core_redirect(trailingslashit(bp_displayed_user_domain() . constant('BP_MEDIA_' . $mediaconst . '_SLUG')));
    }

    function loader() {
        bp_core_load_template(apply_filters('bp_core_template_plugin', 'members/single/plugins'));
    }

    /**
     *
     * @global type $bp
     * @global type $bp_media_default_excerpts
     * @return type
     */
    function upload_form_multiple_activity() {
        global $bp, $bp_media_default_excerpts;
        if ($bp->current_component != 'activity')
            return;
        ?>
        <div id="bp-media-album-prompt" title="Select Album">
            <div class="bp-media-album-title">
                <span><?php _e('Select Album', 'buddypress-media'); ?></span>
                <span id="bp-media-close"><?php _e('x', 'buddypress-media'); ?></span>
            </div>
            <div class="bp-media-album-content">
                <select id="bp-media-selected-album"><?php
        $albums = new WP_Query(array(
                    'post_type' => 'bp_media_album',
                    'posts_per_page' => -1,
                    'author' => get_current_user_id()
                ));
        if (isset($albums->posts) && is_array($albums->posts) && count($albums->posts) > 0) {
            foreach ($albums->posts as $album) {
                if ($album->post_title == $post_wall)
                    echo '<option value="' . $album->ID . '" selected="selected">' . $album->post_title . '</option>';
                else
                    echo '<option value="' . $album->ID . '">' . $album->post_title, 'buddypress-media' . '</option>';
            };
        }
        ?></select>
            </div>
            <div class="select-btn-div">
                <input id="selected-btn" type="button" class="btn" value="<?php _e('Select', 'buddypress-media'); ?>" />
                <input id="create-btn" type="button" class="btn" value="<?php _e('Create Album', 'buddypress-media'); ?>" />
                <div style="clear: both;"></div>
            </div>
        </div>
        <div id="bp-media-album-new" title="Create New Album">
            <div class="bp-media-album-title">
                <span><?php _e('Create Album', 'buddypress-media'); ?></span>
                <span id="bp-media-create-album-close"><?php _e('x', 'buddypress-media'); ?></span>
            </div>
            <div class="bp-media-album-content">
                <label for="bp_media_album_name"><?php _e('Album Name', 'buddypress-media'); ?></label>
                <input id="bp_media_album_name" type="text" name="bp_media_album_name" />
            </div>
            <div class="select-btn-div">
                <input id="create-album" type="button" class="btn" value="<?php _e('Create', 'buddypress-media'); ?>" />
            </div>
        </div>
        <div id="bp-media-upload-ui" class="hide-if-no-js drag-drop activity-component">
            <p class="drag-drop-buttons"><input id="bp-media-upload-browse-button" type="button" value="<?php _e('Add Media', 'buddypress-media'); ?>" class="button" /></p>
            <div id="bp-media-uploaded-files"></div>
        </div>
        <?php
    }

}
?>
