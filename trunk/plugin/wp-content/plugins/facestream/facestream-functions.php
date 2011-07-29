<?php

##############################################
##                                          ##
##             plugin stuff                 ##
##                                          ##
##############################################

define ( 'BP_FACESTREAM_VERSION', '1.1' );
define ( 'BP_FACESTREAM_IS_INSTALLED', 1 );

##############################################
##                                          ##
##             local language               ##
##                                          ##
##############################################

add_action('plugins_loaded', 'facestream_textdomain', 9);

function facestream_textdomain() {
    $locale = apply_filters( 'facestream_textdomain', get_locale() );
    $mofile = WP_PLUGIN_DIR . "/facestream/languages/$locale.mo";

    if ( file_exists( $mofile ) ){
        load_textdomain( 'facestream_lang', $mofile );
    }
}

################################################
##                                            ##
##  add extra filter option in dropdown       ##
##                                            ##
################################################

add_action('bp_activity_filter_options', 'facestream_addFilter',1);
add_action('bp_member_activity_filter_options', 'facestream_addFilter',1);

function facestream_addFilter() {
    echo '<option value="facebook">'.__( 'Show Facebook', 'facestream_lang' ).'</option>';
}

################################################
##                                            ##
##  add action to add checkbox to update form ##
##                                            ##
################################################

add_action ('bp_activity_post_form_options', 'facestream_addCheckbox');

//add facebook checkbox to form
function facestream_addCheckbox() {

    global $bp;

    $user_id = $bp->loggedin_user->id;

    if(get_site_option("facestream_api_key")){
        if(get_usermeta ( $user_id, 'facestream_session_key' )){

            //add css and js
            echo'<script type="text/javascript" src="'.plugins_url("facestream/js/facestream.js").'"></script>';
            $checkbox_on = get_usermeta ( $user_id, 'facestream_checkboxon' );

            if($checkbox_on==1){
                echo'<div class="facestream_checkbox_container"><input type="checkbox" name="activity_to_facebook" id="activity_to_facebook" value="1" checked> '.__( 'To facebook', 'facestream_lang' ).'</div>';
            }else{
                echo'<div class="facestream_checkbox_container"><input type="checkbox" name="activity_to_facebook" id="activity_to_facebook" value="1"> '.__( 'To facebook', 'facestream_lang' ).'</div>';
            }

        }else{
            if(get_site_option('facestream_user_settings_message')==1){
                echo ' <i>'.__( 'Want to send this message to facebook to? Check your ', 'facestream_lang' ).'<a href="'.$bp->loggedin_user->domain.'settings/facestream">'.__( 'facestream settings', 'facestream_lang' ).'</a>.</i>';
            }
        }
    }

    //memory cleanup
    unset($checkbox_on);
    unset($user_id);
}

################################################
##                                            ##
##  add action to add checkbox to forum form  ##
##                                            ##
################################################

add_action ('groups_forum_new_topic_after', 'facestream_addTopicCheckbox' );
add_action ('bp_after_group_forum_post_new', 'facestream_addTopicCheckbox' );

//add facebook checkbox to form
function facestream_addTopicCheckbox() {
    global $bp;

    $user_id = $bp->loggedin_user->id;

    if(get_site_option("facestream_api_key")){

        if(get_usermeta ( $user_id, 'facestream_session_key' )){

            //add css and js
            echo'<script type="text/javascript" src="'.plugins_url("facestream/js/facestream.js").'"></script>';

            $checkbox_on = get_usermeta ( $user_id, 'facestream_checkboxon' );

            if($checkbox_on==1){
                echo'<br><br><div class="facestream_checkbox_container"><input type="checkbox" name="topic_to_facebook" id="topic_to_facebook" value="1" checked> '.__( 'To facebook', 'facestream_lang' ).'</div>';
            }else{
                echo'<br><br><div class="facestream_checkbox_container"><input type="checkbox" name="topic_to_facebook" id="topic_to_facebook" value="1" > '.__( 'To facebook', 'facestream_lang' ).'</div>';
            }

        }else{
            if(get_site_option('facestream_user_settings_message')==1){
                echo ' <i>'.__( 'Want to tweet this message to? Check your ', 'facestream_lang' ).'<a href="'.$bp->loggedin_user->domain.'settings/facestream">'.__( 'facestream settings', 'facestream_lang' ).'</a>.</i>';
            }
        }
    }

    //memory cleanup
    unset($checkbox_on);
    unset($user_id);
}


################################################
##                                            ##
##  add action to add checkbox to forum reply ##
##                                            ##
################################################

add_action ('groups_forum_new_reply_after', 'facestream_addTopicReplyCheckbox' );

//add facebook checkbox to form
function facestream_addTopicReplyCheckbox() {

    global $bp;
    $user_id = $bp->loggedin_user->id;

    if(get_site_option("facestream_api_key")){

        if(get_usermeta ( $user_id, 'facestream_session_key' )){

            //add css and js
            echo'<script type="text/javascript" src="'.plugins_url("facestream/js/facestream.js").'"></script>';

            $checkbox_on = get_usermeta ( $user_id, 'facestream_checkboxon' );

            if($checkbox_on==1){
                echo'<div class="facestream_checkbox_container"><input type="checkbox" name="topicreply_to_facebook" id="topicreply_to_facebook" value="1" checked> '.__( 'To facebook', 'facestream_lang' ).'</div><br><br>';
            }else{
                echo'<div class="facestream_checkbox_container"><input type="checkbox" name="topicreply_to_facebook" id="topicreply_to_facebook" value="1"> '.__( 'To facebook', 'facestream_lang' ).'</div><br><br>';
            }

        }else{
            if(get_site_option('facestream_user_settings_message')==1){
                echo ' <i>'.__( 'Want to tweet this message to? Check your ', 'facestream_lang' ).'<a href="'.$bp->loggedin_user->domain.'settings/facestream">'.__( 'facestream settings', 'facestream_lang' ).'</a>.</i>';
            }
        }
    }

    //memory cleanup
    unset($checkbox_on);
    unset($user_id);
}

##############################################
##                                          ##
##   add facebook item when new topic       ##
##                                          ##
##############################################

add_filter( 'group_forum_topic_title_before_save', 'facestream_topic');

//function with params from bp_activity_add
function facestream_topic() {

    global $bp;

    //get loged in user id
    $user_id = $bp->loggedin_user->id;
    $title = $_POST['topic_title'];

    //check if #FACEBOOK is there if so then kick the content to facebook!
    $pos = strpos($title,"#FACEBOOK");
    $title = facestream_filterTags($title);

    //facebook tag found
    if($pos>0){
        add_filter('bp_forums_new_topic', 'facestream_topicSaveId',9);
    }

    //memory cleanup
    unset($pos);
    unset($user_id);

    return $title;
}

function facestream_topicSaveId($id){
    facestream_topicToFacebook($id);
    return $id;
}

function facestream_topicToFacebook($id){

    global $bp;

    $topic_info = bp_forums_get_topic_details($id);

    //get loged in user id
    $user_id = $bp->loggedin_user->id;
    $content = $_POST['topic_title'];

    //check if #FACEBOOK is there if so then kick the content to facebook!
    $pos = strpos($content,"#FACEBOOK");
    $content = facestream_filterTags($content);

    //we found #FACEBOOK tag so push it to facebook
    if($pos >0) {

        //filter for url ceation
        $backlink = bp_get_group_permalink( $bp->groups->current_group ) . 'forum/topic/' . $topic_info->topic_slug . '/';

        //create short url
        $backlink = facestream_getShortUrl($backlink);
        $content .= " ".$backlink;

        facestream_facebookIt($content);
    }

    //memory cleanup
    unset($content);
    unset($topic_info);
    unset($pos);
    unset($backlink);

}

##############################################
##                                          ##
##   add facebook item when new reply       ##
##                                          ##
##############################################

add_filter('group_forum_post_text_before_save', 'facestream_topicReply',9);

function facestream_topicReply() {

    global $bp;

    //get loged in user id
    $user_id = $bp->loggedin_user->id;
    $content = $_POST['reply_text'];
    if($content==""){
        $content = $_POST['post_text'];
    }

    //check if #FACEBOOK is there if so then kick the content to facebook!
    $pos = strpos($content,"#FACEBOOK");
    $content = facestream_filterTags($content);

    //facebook tag found
    if($pos>0){
        add_filter('group_forum_post_topic_id_before_save', 'facestream_topicReplySaveId',9);
    }

    //memory cleanup
    unset($pos);
    unset($user_id);

    return $content;
}

function facestream_topicReplySaveId($id){

    facestream_topicReplyToFacebook($id);
    return $id;
}

function facestream_topicReplyToFacebook($id){
    global $bp;

    $topic_info = bp_forums_get_topic_details($id);

    //filter for url ceation
    $backlink = bp_get_group_permalink( $bp->groups->current_group ) . 'forum/topic/' . $topic_info->topic_slug . '/';

    //create short url
    $backlink = facestream_getShortUrl($backlink);
    $content = $topic_info->topic_title;
    $content = __( 'Just responded to:', 'facestream_lang' )." ".$content." ".$backlink;

    facestream_facebookIt($content);

    //memory cleanup
    unset($topic_info);
    unset($content);
    unset($backlink);

}


##############################################
##                                          ##
##   add facebook item when new activity    ##
##                                          ##
##############################################

add_filter( 'bp_activity_content_before_save', 'facestream_activityToFacebook' );

//function with params from bp_activity_add
function facestream_activityToFacebook($content) {

    global $bp;

    //get loged in user id
    $user_id = $bp->loggedin_user->id;

    //check if #FACEBOOK is there if so then kick the content to facebook!
    $pos = strpos($content,"#FACEBOOK");
    $content = facestream_filterTags($content);

    //we found #FACEBOOK tag so push it to facebook
    if($pos >0) {

        // out all html
        $content = strip_tags($content);
        $content =  stripslashes($content);

        //how long is user domain
        if(get_usermeta ( $user_id, 'facestream_profilelink' )==1){
            $content .= " ".facestream_getShortUrl($bp->loggedin_user->domain);
        }

        facestream_facebookIt($content);

    }

    //memory cleanup
    unset($pos);
    unset($user_id);

    return $content;
}

##############################################
##                                          ##
##  function to send content to facebook     ##
##                                          ##
##############################################

function facestream_facebookIt($content) {

    global $bp;

    //get loged in user id
    $user_id = $bp->loggedin_user->id;

    //content related things
    //$content = mb_convert_encoding($content, 'HTML-ENTITIES', "UTF-8");
    $content = str_replace("@","",$content);

    //filter for other plugins
    $content = str_replace("#FACEBOOK","",$content);
    $content = str_replace("#TWITTER","",$content);
    $content = str_replace("#MYSPACE","",$content);
    $content = str_replace("#LINKEDIN","",$content);

    //create new facebook instance and login
    $session_key = get_usermeta ( $user_id, 'facestream_session_key' );
    $facestream_userid = get_usermeta ( $user_id, 'facestream_userid' );

    if($session_key){

        require_once("facebook/facebook.php");
        $fb = new Facebook(get_site_option("facestream_api_key"),get_site_option("facestream_application_secret"));
        $fb->api_client->session_key = $session_key;
        $fb->api_client->users_setStatus($content,$facestream_userid);

        //memory cleanup
        unset($content);
        unset($user_id);
        unset($session_key);
        unset($facestream_userid);
        unset($fb);

        return true;
    }else{
        return false;
    }

}

##############################################
##                                          ##
##  add facebook items to activity stream   ##
##                                          ##
##############################################

add_action( 'wp', 'facestream_lightbox', 9 );

function facestream_lightbox(){
    // (not working BAO) wp_enqueue_script( 'thickbox' );
    wp_enqueue_style( 'thickbox' );
}

add_action('wp','facestream_runCron');

function facestream_runCron(){

    global $wpdb,$bp;

    //every 10 minutes we need to update
    $cron_run    = 0;
    $last_update = get_site_option('facestream_cron');
    $now         = date('dmYhmi');
    $date_diff   = $now-$last_update;

    $min = get_site_option('facestream_cronrun');
    if($min==""){$min = 10;}

    //may we run the cron?
    if ($date_diff >= $min){ $cron_run = 1; }

    if($cron_run == 1){
        // get all usermeta with facebook authorisation
        if(get_site_option('facestream_user_settings_syncbp')==0){
            $user_metas = $wpdb->get_results($wpdb->prepare("SELECT user_id FROM $wpdb->usermeta WHERE meta_key='facestream_session_key';"));

            if($user_metas){
                foreach ($user_metas as $user_meta){
                    facestream_getFacebook($user_meta->user_id);
                }
            }
        }
        //set new date stamp for cron
        update_site_option( 'facestream_cron',trim(date('dmYhmi')));
    }

    //memory cleanup
    unset($cron_run);
    unset($last_update);
    unset($now);
    unset($date_diff);
    unset($user_metas);

}


//////////////////////
//                  //
// FILTER FUNCTION  //
//                  //
//////////////////////

function facestream_filterText($text,$filters){

    $return = 0;
    $text =  strip_tags($text);
    $text = strtolower($text);
    $text = " ".$text." ";

    if($filters){
        $arrFilters = explode(",",$filters);
        foreach ($arrFilters as $filter){
            if(strpos($text,$filter) >0){
                $return = 1;
            }
        }
    }else{
        $return = 2;
    }

    //keeping memory clean
    unset($pos_filter);
    unset($arrFilters);
    return $return;
}


function facestream_getFacebook($user_id) {

    global $bp,$wpdb;

    require_once("facebook/facebook.php");

    //allowed to import at all?
    if(get_site_option('facestream_user_settings_syncbp')==0){

        //set user fields
        $user_data       = $wpdb->get_results($wpdb->prepare("SELECT user_login FROM $wpdb->users WHERE id=$user_id;"));
        $user_login      = $user_data[0]->user_login;
        $user_fullname   = bp_core_get_user_displayname($user_id);

        //is there a maximum inport
        $max_reached = 0;
        $max_import = get_site_option('facestream_user_settings_maximport');
        if($max_import!=''){
            if(get_usermeta ( $user_id, 'facestream_daycounter>=$max_import')){
                $max_reached = 1;
            }
            else{
                $max_reached = 0;
            }
        }

        if($max_reached==0){

            //check if user has filled in a facebook
            if (get_usermeta ( $user_id, 'facestream_session_key')) {
                if(get_usermeta ( $user_id, 'facestream_synctoac')==1){

                    //facebook object
                    $session_key = get_usermeta ( $user_id, 'facestream_session_key');
                    $facestream_userid = get_usermeta ( $user_id, 'facestream_userid');


                    $fb = new Facebook(get_site_option("facestream_api_key"),get_site_option("facestream_application_secret"));
                    $fb->api_client->session_key = $session_key;

                    //get users items
                    $stream = $fb->api_client->stream_get($facestream_userid);
                    $items = $stream['posts'];


                    //get admin filters
                    $admin_filter = get_site_option('facestream_filter');
                    $admin_filterexplicit = get_site_option('facestream_filterexplicit');

                    //get user filters
                    $user_filtergood = get_usermeta ( $user_id, 'facestream_filtergood');
                    $user_filterbad = get_usermeta ( $user_id, 'facestream_filterbad');

                    if ($items) {
                        foreach ( $items as $item ) {

                            //set defaults
                            $exist = 0;
                            $link = "";
                            $event = "";
                            $image = "";
                            $link_description = "";
                            $update_type = "update";

                    		$sender = $fb->api_client->users_getInfo($item['source_id'], 'name');
                    		$sender_name = $sender[0]['name'];
							if ($sender_name == "") {$sender_name = "unknown"}


                            //set text and filter some stuff out
                            $item_text = $item['message'];
                            $item_text = strip_tags($item_text);

                            //create new activity instance
                            $activity = new BP_Activity_Activity();

                            //lets make something better for filters to cleanup code
                            $filter1 = facestream_filterText($tweet_text,get_site_option('facestream_filter'));
                            $filter2 = facestream_filterText($tweet_text,get_site_option('facestream_filterexplicit'));
                            $filter3 = facestream_filterText($tweet_text,get_usermeta($user_id,'facestream_filtergood'));
                            $filter4 = facestream_filterText($tweet_text,get_usermeta($user_id,'facestream_filterbad'));

                            $filter_pass = 0;
                            if($filter1==1 or $filter1 == 2){$filter_pass = 1;}
                            if($filter_pass != 0 && $filter2==1){$filter_pass = 0;}
                            if($filter_pass != 0 && $filter3==1){$filter_pass = 1;}
                            if($filter_pass != 0 && $filter4==1){$filter_pass = 0;}

                            //mentions filtering
                            if(get_usermeta ( $user_id, 'facestream_filtermentions')==1 && $filter_pass==1){
                                $pattern = '/[@]+([A-Za-z0-9-_]+)/';
                                $found_mention = preg_match( $pattern, $item_text );

                                if($found_mention){
                                    $filter_pass = 0;
                                }
                            }

                            //are we allowed to import this item type
                            //from user
                            if($item['type']==46){
                                if(get_usermeta ( $user_id, 'facestream_syncupdatestoac')==1){
                                    $filter_pass = 0;
                                }
                            }

                            elseif($item['type']==80){
                                if(get_usermeta ( $user_id, 'facestream_synclinkstoac')==1){
                                    $filter_pass = 0;
                                }
                            }

                            elseif($item['type']==247){
                                if(get_usermeta ( $user_id, 'facestream_syncphotostoac')==1){
                                    $filter_pass = 0;
                                }
                            }

                            elseif($item['type']==128){
                                if(get_usermeta ( $user_id, 'facestream_syncvideostoac')==1){
                                    $filter_pass = 0;
                                }
                            }

                            //from admin
                            if($item['type']==46){
                                if(get_site_option('facestream_user_settings_syncupdatesbp')==1){
                                    $filter_pass = 0;
                                }
                            }

                            elseif($item['type']==80){
                                if(get_site_option('facestream_user_settings_synclinksbp')==1){
                                    $filter_pass = 0;
                                }
                            }

                            elseif($item['type']==247){
                                if(get_site_option('facestream_user_settings_syncphotosbp')==1){
                                    $filter_pass = 0;
                                }
                            }

                            elseif($item['type']==128){
                                if(get_site_option('facestream_user_settings_syncvideosbp')==1){
                                    $filter_pass = 0;
                                }
                            }else{

                            }

                            //item public
                            if($item['privacy']['value']!="EVERYONE"){
                                $filter_pass = 0;
                            }

                            //type filtering
                            //if($filter_pass == 1){

                                //check if we already have this item
                                $activity_info = bp_activity_get( array( 'filter' => array( 'secondary_id' => $item['post_id'])));
                                if($activity_info['activities'][0]->id){
                                    $exist =1;
                                }

                                if($item['app_id']!=''){
                                    $exist = 1;
                                }

                                //new activity!
                                if ($exist == 0 ) {

                                    //convert tweet time to timestamp
                                    $date_recorded = $item['created_time'];
                                    $date_recorded  = gmdate('Y-m-d H:i:s',$date_recorded);

                                    //photo 247
                                    if($item['type']==247){
                                        $image = '<a href="'.str_replace("_s.jpg","_n.jpg",$item['attachment']['media'][0]['src']).'" class="thickbox"><img src="'.$item['attachment']['media'][0]['src'].'" alt="'.$item['attachment']['media'][0]['alt'].'"></a>';
                                        $update_type = "photo";
                                    }

                                    //video 128
                                    if($item['type']==128){
                                        $image = '<a href="'.$item['attachment']['href'].'" target="_blanc"><img src="'.$item['attachment']['media'][0]['src'].'" alt="'.$item['attachment']['media'][0]['alt'].'"></a>';
                                        $update_type = "video";
                                    }

                                    //link 80
                                    if($item['type']==80){

                                        if($item['attachment']['media']){
                                            $image = '<a href="'.$item['attachment']['href'].'" target="new"><img src="'.$item['attachment']['media'][0]['src'].'" alt="'.$item['attachment']['media'][0]['alt'].'"></a>';
                                        }

                                        $link = '<a href="'.$item['attachment']['href'].'" target="new">'.$item['attachment']['name'].'</a>&nbsp;';
                                        $link_description = $item['attachment']['description'];
                                        $update_type = "link";
                                    }


                                    $content = '<div class="facebook_container">';

                                    if($image){
                                        $content .= '<div class="facebook_container_image">'.$image.'</div>';
                                    }

                                    $content .= '<div class="facebook_container_message">'.$item_text.''.$link.''.$link_description.''.$event.'</div>';
                                    $content .='</div>';


                                    $activity = new BP_Activity_Activity ();
                                    $activity->user_id = $user_id;
                                    $activity->component = "facestream";
                                    $activity->type = "facebook";
                                    $activity->action = '<a href="'.$bp->root_domain.'/members/'.$user_login.'/" title="'.$user_login.'">'.$user_fullname.'</a><a href="http://www.facebook.com/profile.php?id='.get_usermeta ( $user_id, 'facestream_userid' ).'"><img src="'.plugins_url("facestream/images/icon-small.png").'"></a> '.__( 'posted a', 'facestream_lang' ).' <a target="new" href="http://www.facebook.com/profile.php?id='.$item['source_id'].'">'.__( 'facebook ', 'facestream_lang' ).$update_type.' from '.$sender_name.'</a>:';
                                    $activity->content = $content;
                                    $activity->primary_link = "";
                                    $activity->secondary_item_id = $item['post_id'];
                                    $activity->date_recorded = $date_recorded;
                                    $activity->hide_sitewide = 0;
                                    $activity->save ();

                                    //update day counter
                                    if(get_usermeta ( $user_id, 'facestream_counterdate')!=date('d-m-Y'))
                                    {
                                        update_usermeta ((int) $user_id, 'facestream_daycounter',0);
                                        update_usermeta ( ( int ) $user_id, 'facestream_counterdate',date('d-m-Y'));
                                    }

                                    $cur_counter = get_usermeta ( $user_id, 'facestream_daycounter');
                                    update_usermeta ( ( int ) $user_id, 'facestream_daycounter',$cur_counter+1);
                                }
                            //}


                            //memory cleanup
                            unset($date_recorded);
                            unset($exist);
                            unset($link_description);
                            unset($link);
                            unset($event);
                            unset($image);
                            unset($link_description);
                            unset($update_type);
                            unset($item_text);
                            unset($item);
                            unset($cur_counter);
                            unset($activity);
                            unset($content);
                        }

                        //memory cleanup
                        unset($items);
                    }

                }
            }
        }
    }
}

##############################################
##                                          ##
##      Facestream user settings page       ##
##                                          ##
##############################################

function facestream_settings_screen() {

    global $bp;

    //security fix
    if($bp->displayed_user->id!=$bp->loggedin_user->id){
        header('location:'.$bp->root_domain);
    }

    add_action( 'bp_template_title', 'facestream_settings_screen_title' );
    add_action( 'bp_template_content', 'facestream_settings_screen_content' );
    bp_core_load_template( apply_filters( 'bp_core_template_plugin', 'members/single/plugins' ) );
}


function facestream_settings_screen_title() {
    __( 'Facestream', 'facestream_lang' );
}

function facestream_settings_screen_content() {

    global $bp;

    $user_id = $bp->loggedin_user->id;
    if($_POST){

        update_usermeta ( $user_id, 'facestream_checkboxon',$_POST['facestream_checkboxon']);
        update_usermeta ( $user_id, 'facestream_synctoac',$_POST['facestream_synctoac']);
        update_usermeta ( $user_id, 'facestream_syncupdatestoac',$_POST['facestream_syncupdatestoac']);
        update_usermeta ( $user_id, 'facestream_synclinkstoac',$_POST['facestream_synclinkstoac']);
        update_usermeta ( $user_id, 'facestream_syncphotostoac',$_POST['facestream_syncphotostoac']);
        update_usermeta ( $user_id, 'facestream_syncvideostoac',$_POST['facestream_syncvideostoac']);
        update_usermeta ( $user_id, 'facestream_profilelink',$_POST['facestream_profilelink']);
        update_usermeta ( $user_id, 'facestream_filtermentions',$_POST['facestream_filtermentions']);
        update_usermeta ( $user_id, 'facestream_filtergood',$_POST['facestream_filtergood']);
        update_usermeta ( $user_id, 'facestream_filterbad',$_POST['facestream_filterbad']);

        echo' <div id="message" class="updated fade">
				<p>'.__( 'Settings saved', 'facestream_lang' ).'</p>
			</div>
		';
    }

    $facestream_checkboxon = get_usermeta ( $user_id, 'facestream_checkboxon' );
    $facestream_synctoac = get_usermeta ( $user_id, 'facestream_synctoac' );
    $facestream_syncupdatestoac = get_usermeta ( $user_id, 'facestream_syncupdatestoac' );
    $facestream_synclinkstoac = get_usermeta ( $user_id, 'facestream_synclinkstoac' );
    $facestream_syncphotostoac = get_usermeta ( $user_id, 'facestream_syncphotostoac' );
    $facestream_syncvideostoac = get_usermeta ( $user_id, 'facestream_syncvideostoac' );
    $facestream_profilelink = get_usermeta ( $user_id, 'facestream_profilelink' );
    $facestream_filtermentions = get_usermeta ( $user_id, 'facestream_filtermentions' );
    $facestream_filtergood = get_usermeta ( $user_id, 'facestream_filtergood' );
    $facestream_filterbad = get_usermeta ( $user_id, 'facestream_filterbad' );


    ?>


    <?


    if(get_usermeta ( $user_id, 'facestream_session_key' )){

        echo '<form id="settings_form" action="'.$bp->loggedin_user->domain.'settings/facestream/" method="post">
        <h3>'.__( 'Facestream setting', 'facestream_lang' ).'</h3>
        ';
        echo '<b>'.__( 'Permission', 'facestream_lang' ).'</b><br>'.__( 'You already gave permission.', 'facestream_lang' ).'<br/> '.__( 'To disallow click the button below and choose "Deny"', 'facestream_lang' ).'</b><br><br>';

			?>

		<a href="http://www.facebook.com/login.php?api_key=<?php echo get_site_option('facestream_api_key');?>&display=popup&extern=1&fbconnect=1&req_perms=offline_access,read_stream,publish_stream,status_update&return_session=1&v=1.0&next=<?php echo $bp->root_domain;?>/?type=facebook&fb_connect=1&cancel_url=<?php echo $bp->root_domain;?>/?type=facebook"><?php echo __( 'Authorize with facebook', 'facestream_lang' );?></a><br/><br/>


		<table id="activity-notification-settings" class="notification-settings" >
    		<tr class="alt">
    			<th class="icon"></th>
    			<th class="title"><?php echo __( 'Options', 'facestream_lang' );?></th>
    			<th class="yes"><?php echo __( 'Yes', 'facestream_lang' );?></th>
    			<th class="no"><?php echo __( 'No', 'facestream_lang' );?></th>
    		</tr>

    		<?php  if(get_site_option('facestream_user_settings_syncbp')==0){ ?>

    		<tr class="alt">
    			<td></td>
    			<td><?php echo __( 'Always check checkbox "To facebook"'  , 'facestream_lang' );?></td>
    			<td class="yes"><input type="radio" name="facestream_checkboxon" id="facestream_checkboxon" value="1" <?php if($facestream_checkboxon==1){echo'checked';}?>></td>
    			<td class="no"><input type="radio" name="facestream_checkboxon" id="facestream_checkboxon" value="0" <?php if($facestream_checkboxon==0){echo'checked';}?>></td>
    		</tr>

    		<tr>
    			<td></td>
    			<td><?php echo __( 'Synchronize facebook items to my activity'  , 'facestream_lang' );?></td>
    			<td class="yes"><input type="radio" name="facestream_synctoac" id="facestream_synctoac" value="1" <?php if($facestream_synctoac==1){echo'checked';}?>></td>
    			<td class="no"><input type="radio" name="facestream_synctoac" id="facestream_synctoac" value="0" <?php if($facestream_synctoac==0){echo'checked';}?>></td>
    		</tr>


    		<?php if(get_site_option('facestream_user_settings_syncupdatesbp')==0){ ?>
    		<tr>
    			<td></td>
    			<td><?php echo __( 'Synchronize updates to my activity'  , 'facestream_lang' );?></td>
    			<td class="yes"><input type="radio" name="facestream_syncupdatestoac" id="facestream_syncupdatestoac" value="0" <?php if($facestream_syncupdatestoac==0){echo'checked';}?>></td>
    			<td class="no"><input type="radio" name="facestream_syncupdatestoac" id="facestream_syncupdatestoac" value="1" <?php if($facestream_syncupdatestoac==1){echo'checked';}?>></td>
    		</tr>
    		<?php } ?>



    		<?php if(get_site_option('facestream_user_settings_synclinksbp')==0){ ?>
    		<tr>
    			<td></td>
    			<td><?php echo __( 'Synchronize links to my activity'  , 'facestream_lang' );?></td>
    			<td class="yes"><input type="radio" name="facestream_synclinkstoac" id="facestream_synclinkstoac" value="0" <?php if($facestream_synclinkstoac==0){echo'checked';}?>></td>
    			<td class="no"><input type="radio" name="facestream_synclinkstoac" id="facestream_synclinkstoac" value="1" <?php if($facestream_synclinkstoac==1){echo'checked';}?>></td>
    		</tr>
    		<?php } ?>

    		<?php if(get_site_option('facestream_user_settings_syncphotosbp')==0){ ?>
    		<tr>
    			<td></td>
    			<td><?php echo __( 'Synchronize photo\'s to my activity'  , 'facestream_lang' );?></td>
    			<td class="yes"><input type="radio" name="facestream_syncphotostoac" id="facestream_syncphotostoac" value="0" <?php if($facestream_syncphotostoac==0){echo'checked';}?>></td>
    			<td class="no"><input type="radio" name="facestream_syncphotostoac" id="facestream_syncphotostoac" value="1" <?php if($facestream_syncphotostoac==1){echo'checked';}?>></td>
    		</tr>
    		<?php } ?>

    		<?php if(get_site_option('facestream_user_settings_syncvideosbp')==0){ ?>
    		<tr>
    			<td></td>
    			<td><?php echo __( 'Synchronize video\'s to my activity'  , 'facestream_lang' );?></td>
    			<td class="yes"><input type="radio" name="facestream_syncvideostoac" id="facestream_syncvideostoac" value="0" <?php if($facestream_syncvideostoac==0){echo'checked';}?>></td>
    			<td class="no"><input type="radio" name="facestream_syncvideostoac" id="facestream_syncvideostoac" value="1" <?php if($facestream_syncvideostoac==1){echo'checked';}?>></td>
    		</tr>
    		<?php } ?>





    		<?php } ?>

    		<tr>
    			<td></td>
    			<td><?php echo __( 'Add my profile link after my facebook item'  , 'facestream_lang' );?></td>
    			<td class="yes"><input type="radio" name="facestream_profilelink" id="facestream_profilelink" value="1" <?php if($facestream_profilelink==1){echo'checked';}?>></td>
    			<td class="no"><input type="radio" name="facestream_profilelink" id="facestream_profilelink" value="0" <?php if($facestream_profilelink==0){echo'checked';}?>></td>
    		</tr>
		</table>


		<?php  if(get_site_option('facestream_user_settings_syncbp')==0){ ?>

		<b><?php echo __( 'Filters', 'facestream_lang' );?></b><br/>
		<?php echo __( 'With filter you can decide what will be imported and what not. ', 'facestream_lang' );?><br/>
		<?php echo __( 'By adding words in the "Good" filter only items with those words in it will be imported.', 'facestream_lang' );?><br/>
		<?php echo __( 'By adding words in the "Bad" filter items with those words won\'t be imported.', 'facestream_lang' );?><br/>

		<br/>

		<table id="activity-notification-settings" class="notification-settings" >
    		<tr>
    			<th><?php echo __( 'Filters (items to activity)', 'facestream_lang' );?></th>
    			<th></th>
    			<th></th>
    		</tr>

    	<tr>
        	<td></td>
        	<td><?php echo __( 'Good filter (comma seperated)', 'facestream_lang' );?></td>
        	<td><input type="text" name="facestream_filtergood" value="<?php echo $facestream_filtergood ;?>" size="50"/></td>
        </tr>

        <tr class="alt">
        	<td></td>
            <td><?php echo __( 'Bad filter (comma seperated)', 'facestream_lang' );?></td>
            <td><input type="text" name="facestream_filterbad" value="<?php echo $facestream_filterbad ;?>" size="50"/></td>
        </tr>
	</table>

	<?php } ?>

			<input type="submit" value="<?php echo __( 'Save settings', 'facestream_lang' );?>">

			<?php

			echo '</form>';
    }else{
        echo'<b>'.__( 'Permission', 'facestream_lang' ).'</b><br>'.__( 'You can setup your facestream over here.', 'facestream_lang' ).'<br>
			'.__( 'Before u can see al settings please authorize on facebook, to do so click on the link below.', 'facestream_lang' ).'<br><br>';
        echo '
        <a href="http://www.facebook.com/login.php?api_key='.get_site_option('facestream_api_key').'&display=popup&extern=1&fbconnect=1&req_perms=offline_access,read_stream,publish_stream,status_update&return_session=1&v=1.0&next='.$bp->root_domain.'/?type=facebook&fb_connect=1&cancel_url='.$bp->root_domain.'/?type=facebook">
            '.__( 'Authorize with facebook', 'facestream_lang' ).'
        </a>
        ';
    }

    //memory cleanup
    unset($facestream_checkboxon);
    unset($facestream_synctoac);
    unset($facestream_syncupdatestoac);
    unset($facestream_synclinkstoac);
    unset($facestream_syncphotostoac);
    unset($facestream_syncvideostoac);
    unset($facestream_profilelink);
    unset($facestream_filtermentions);
    unset($facestream_filtergood);
    unset($facestream_filterbad);

}

##############################################
##                                          ##
##             setup navigation             ##
##                                          ##
##############################################

function facestream_setup_nav() {
    global $bp;

    if(get_site_option("facestream_api_key")){
        bp_core_new_subnav_item( array ('name' => __ ( 'Facestream', 'facestream_lang' ), 'slug' => 'facestream', 'parent_url' => $bp->loggedin_user->domain . 'settings/', 'parent_slug' => 'settings', 'screen_function' => 'facestream_settings_screen', 'position' => 40 ) );
    }
}

facestream_setup_nav();

##############################################
##                                          ##
##               admin page                 ##
##                                          ##
##############################################

add_action('admin_menu', 'facestream_admin');

function facestream_admin() {
    if(is_admin()){
        add_menu_page(__( 'Facestream plugin settings', 'facestream_lang'), 'Facestream', 'administrator', __FILE__, 'facestream_settings',plugins_url('images/icon-small.png', __FILE__));
    }
}

function facestream_settings() {
    global $bp,$wpdb;

    if($_POST){

        update_site_option( 'facestream_filter',trim(strip_tags(strtolower($_POST['facestream_filter']))));
        update_site_option( 'facestream_filter_show',trim(strip_tags($_POST['facestream_filter_show'])));
        update_site_option( 'facestream_api_key',trim(strip_tags($_POST['facestream_api_key'])));
        update_site_option( 'facestream_application_secret',trim(strip_tags($_POST['facestream_application_secret'])));
        update_site_option( 'facestream_user_settings_message',trim(strip_tags($_POST['facestream_user_settings_message'])));
        update_site_option( 'facestream_filterexplicit',trim(strip_tags(strtolower($_POST['facestream_filterexplicit']))));
        update_site_option( 'facestream_user_settings_syncbp',trim(strip_tags(strtolower($_POST['facestream_user_settings_syncbp']))));
        update_site_option( 'facestream_user_settings_syncupdatesbp',trim(strip_tags(strtolower($_POST['facestream_user_settings_syncupdatesbp']))));
        update_site_option( 'facestream_user_settings_synclinksbp',trim(strip_tags(strtolower($_POST['facestream_user_settings_synclinksbp']))));
        update_site_option( 'facestream_user_settings_syncvideosbp',trim(strip_tags(strtolower($_POST['facestream_user_settings_syncvideosbp']))));
        update_site_option( 'facestream_user_settings_syncphotosbp',trim(strip_tags(strtolower($_POST['facestream_user_settings_syncphotosbp']))));
        update_site_option( 'facestream_user_settings_maximport',trim(strip_tags(strtolower($_POST['facestream_user_settings_maximport']))));
        update_site_option( 'facestream_cronrun',$_POST['facestream_cronrun']);


        echo '<div class="updated" style="margin-top:50px;"><p><strong>'.__( 'Settings saved.', 'facestream_lang' ).'</strong></p></div>';
    }

?>
<div class="wrap">

<br/><img src="<?php echo plugins_url('images/icon.png', __FILE__);?>" style="float:left;"><h2 style="float:left; line-height:5px; padding-left:5px;"><?php echo __( 'Facestream');?></h2><br/>

<form method="post" action="">
    <table class="form-table">
        <?php echo __( '', 'facestream_lang' );?>
        <tr>
            <td colspan="2" scope="row">
            <b><?php echo __( 'Facebook API', 'facestream_lang' );?></b><br/>

            <?php echo __( 'For the plugin to work you need to get an API key from facebook.', 'facestream_lang' );?><br/><br/>
            <?php echo __( 'To get one follow the next steps:', 'facestream_lang' );?><br/>
            <?php echo __( '&nbsp;&nbsp;&nbsp;1. Go to ', 'facestream_lang' );?>"<a href="http://developers.facebook.com/setup.php" target="_blank">http://developers.facebook.com/setup.php</a>" <?php echo __( ' and login with your facebook account.', 'facestream_lang' );?><br/>
            <?php echo __( '&nbsp;&nbsp;&nbsp;2. Create a new app ', 'facestream_lang' );?><br/>
            <?php echo __( '&nbsp;&nbsp;&nbsp;3. Fill in the API key and application secret below.', 'facestream_lang' );?><br/>

            </td>
        </tr>
    <tr valign="top">
        <th scope="row"><?php echo __( 'API key:', 'facestream_lang' );?></th>
        <td><input type="text" name="facestream_api_key" value="<?php echo get_site_option('facestream_api_key'); ?>" size="50"/></td>
        </tr>

        <tr valign="top">
        <th scope="row"><?php echo __( 'Application secret secret key:', 'facestream_lang' );?></th>
        <td><input type="text" name="facestream_application_secret" value="<?php echo get_site_option('facestream_application_secret'); ?>" size="50"/></td>
        </tr>

         <tr valign="top">
        <th scope="row"><?php echo __( 'Run import every:', 'facestream_lang' );?></th>
        <td><input type="text" name="facestream_cronrun" value="<?php echo get_site_option('facestream_cronrun'); ?>" size="4"/> minutes</td>
        </tr>

          <tr>
            <td colspan="2">
            <h2><?php echo __( 'Filters (optional)', 'facestream_lang' );?></h2><br>
            <?php echo __( 'Filters preventing to get a really messy activity streams.', 'facestream_lang' );?><br>
            <?php echo __( 'Example: You have an social network which focus is on soccer, you don\'t want all facebook items of your users showing up that hasn\'t to do anything with soccer.', 'facestream_lang' );?><br>
            <?php echo __( 'you can set the filter to "soccer", now only facebook items with "soccer" will be shown of the users tweets".', 'facestream_lang' );?><br>
            <?php echo __( 'By comma seperating words you can set-up multiple filters.', 'facestream_lang' );?> (<?php echo __( 'No filter = all tweets.', 'facestream_lang' );?>)<br>
            <?php echo __( 'The explicit words filter blocks messages with those words in it.', 'facestream_lang' );?>

            </td>
        </tr>

        <tr valign="top">
        <th scope="row"><?php echo __( 'Filters (comma seperated)', 'facestream_lang' );?></th>
        <td><input type="text" name="facestream_filter" value="<?php echo get_site_option('facestream_filter'); ?>" size="50"/></td>
        </tr>

        <tr valign="top">
            <th scope="row"><?php echo __( 'Show filters in tweets.', 'facestream_lang' );?></th>
            <th>
            <input type="radio" name="facestream_filter_show" id="facestream_filter_show" value="1" <?php if(get_site_option('facestream_filter_show')==1){echo'checked';}?>> <?php echo __( 'Yes', 'facestream_lang' );?>
			<input type="radio" name="facestream_filter_show" id="facestream_filter_show" value="0" <?php if(get_site_option('facestream_filter_show')==0){echo'checked';}?>> <?php echo __( 'No', 'facestream_lang' );?>
            </th>
        </tr>

        <tr valign="top">
        <th scope="row"><?php echo __( 'Explicit words (comma seperated)', 'facestream_lang' );?></th>
        <td><input type="text" name="facestream_filterexplicit" value="<?php echo get_site_option('facestream_filterexplicit'); ?>" size="50"/></td>
        </tr>

         <tr valign="top">
        <th scope="row"><h2><?php echo __( 'User options', 'facestream_lang' );?></h2></th>
        <td></td>
        </tr>

        <tr valign="top">
            <th><?php echo __( 'Show "Want send this message to facebook to?..." message on user page.', 'facestream_lang' );?></th>
            <th>
            <input type="radio" name="facestream_user_settings_message" id="facestream_user_settings_message" value="1" <?php if(get_site_option('facestream_user_settings_message')==1){echo'checked';}?>> <?php echo __( 'Yes', 'facestream_lang' );?>
			<input type="radio" name="facestream_user_settings_message" id="facestream_user_settings_message" value="0" <?php if(get_site_option('facestream_user_settings_message')==0){echo'checked';}?>> <?php echo __( 'No', 'facestream_lang' );?>
            </th>
        </tr>

        <tr valign="top">
            <th><?php echo __( 'Allow users to sync to buddypress.', 'facestream_lang' );?></th>
            <th>
            <input type="radio" name="facestream_user_settings_syncbp" id="facestream_user_settings_syncbp" value="0" <?php if(get_site_option('facestream_user_settings_syncbp')==0){echo'checked';}?>> <?php echo __( 'Yes', 'facestream_lang' );?>
			<input type="radio" name="facestream_user_settings_syncbp" id="facestream_user_settings_syncbp" value="1" <?php if(get_site_option('facestream_user_settings_syncbp')==1){echo'checked';}?>> <?php echo __( 'No', 'facestream_lang' );?>
            </th>
        </tr>

        <tr valign="top">
            <th><?php echo __( 'Allow users to sync updates to buddypress.', 'facestream_lang' );?></th>
            <th>
            <input type="radio" name="facestream_user_settings_syncupdatesbp" id="facestream_user_settings_syncupdatesbp" value="0" <?php if(get_site_option('facestream_user_settings_syncupdatesbp')==0){echo'checked';}?>> <?php echo __( 'Yes', 'facestream_lang' );?>
			<input type="radio" name="facestream_user_settings_syncupdatesbp" id="facestream_user_settings_syncupdatesbp" value="1" <?php if(get_site_option('facestream_user_settings_syncupdatesbp')==1){echo'checked';}?>> <?php echo __( 'No', 'facestream_lang' );?>
            </th>
        </tr>

        <tr valign="top">
            <th><?php echo __( 'Allow users to sync links to buddypress.', 'facestream_lang' );?></th>
            <th>
            <input type="radio" name="facestream_user_settings_synclinksbp" id="facestream_user_settings_synclinksbp" value="0" <?php if(get_site_option('facestream_user_settings_synclinksbp')==0){echo'checked';}?>> <?php echo __( 'Yes', 'facestream_lang' );?>
			<input type="radio" name="facestream_user_settings_synclinksbp" id="facestream_user_settings_synclinksbp" value="1" <?php if(get_site_option('facestream_user_settings_synclinksbp')==1){echo'checked';}?>> <?php echo __( 'No', 'facestream_lang' );?>
            </th>
        </tr>

        <tr valign="top">
            <th><?php echo __( 'Allow users to sync photo\'s to buddypress.', 'facestream_lang' );?></th>
            <th>
            <input type="radio" name="facestream_user_settings_syncphotosbp" id="facestream_user_settings_syncphotosbp" value="0" <?php if(get_site_option('facestream_user_settings_syncphotosbp')==0){echo'checked';}?>> <?php echo __( 'Yes', 'facestream_lang' );?>
			<input type="radio" name="facestream_user_settings_syncphotosbp" id="facestream_user_settings_syncphotosbp" value="1" <?php if(get_site_option('facestream_user_settings_syncphotosbp')==1){echo'checked';}?>> <?php echo __( 'No', 'facestream_lang' );?>
            </th>
        </tr>

        <tr valign="top">
            <th><?php echo __( 'Allow users to sync video\'s to buddypress.', 'facestream_lang' );?></th>
            <th>
            <input type="radio" name="facestream_user_settings_syncvideosbp" id="facestream_user_settings_syncvideosbp" value="0" <?php if(get_site_option('facestream_user_settings_syncvideosbp')==0){echo'checked';}?>> <?php echo __( 'Yes', 'facestream_lang' );?>
			<input type="radio" name="facestream_user_settings_syncvideosbp" id="facestream_user_settings_syncvideosbp" value="1" <?php if(get_site_option('facestream_user_settings_syncvideosbp')==1){echo'checked';}?>> <?php echo __( 'No', 'facestream_lang' );?>
            </th>
        </tr>

          <tr valign="top">
            <th><?php echo __( 'Max facebook items import per user, per day (empty = unlimited).', 'facestream_lang' );?></th>
            <th>
           	<input type="text" name="facestream_user_settings_maximport" value="<?php echo get_site_option('facestream_user_settings_maximport'); ?>" size="5"/>
            </th>
        </tr>

          <tr valign="top">
            <th><?php echo __( 'Last Facebook import.', 'facestream_lang' );?></th>
            <th>
           	<?php echo get_site_option('facestream_cron'); ?>
            </th>
        </tr>

          <tr valign="top">
            <th><?php echo __( 'Next Facebook import.', 'facestream_lang' );?></th>
            <th>
           	<?php echo get_site_option('facestream_cron') + (get_site_option('facestream_cronrun')) ?>
            </th>
        </tr>
    </table>
    <p class="submit">
    <input type="submit" class="button-primary" value="<?php echo __('Save Changes') ?>" />
    </p>
</form>

</div>
<?php }

##############################################
##                                          ##
##          oauth back from facebook         ##
##                                          ##
##############################################

add_action('wp','facestream_oauthcheck');

function facestream_oauthcheck(){
    global $bp;

    if($_GET['session']){

        $session = $_GET['session'];
        $session= stripslashes($session);
        $json_result = json_decode($session);

        $session_key  = $json_result->session_key;
        $facebook_uid = $json_result->uid;

        update_usermeta ( ( int ) $bp->loggedin_user->id, 'facestream_session_key',$session_key );
        update_usermeta ( ( int ) $bp->loggedin_user->id, 'facestream_userid',$facebook_uid );
        update_usermeta ( ( int ) $bp->loggedin_user->id, 'facestream_synctoac',1);

        //memory cleanup
        unset($session);
        unset($json_result);
        unset($session_key);
        unset($facebook_uid);

        //redirect to facestream settings
        header('location:'.$bp->loggedin_user->domain."settings/facestream");
    }

    if($_GET['denied']){

        update_usermeta ( ( int ) $bp->loggedin_user->id, 'facestream_session_key','');
        update_usermeta ( ( int ) $bp->loggedin_user->id, 'facestream_userid','');
        update_usermeta ( ( int ) $bp->loggedin_user->id, 'facestream_synctoac','');

        //redirect to facestream settings
        header('location:'.$bp->loggedin_user->domain."settings/facestream");


    }


}


//filter tags
add_filter('bp_get_activity_latest_update','facestream_filterTags',9);
function facestream_filterTags($content){
    $content = str_replace("#FACEBOOK","",$content);
    return $content;
}

##############################################
##                                          ##
##      shorten url functions               ##
##                                          ##
##############################################

function facestream_getShortUrl($url){
    global $bp;


    if($url){
        $input = date('dmyhis');
        $index = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        $base  = strlen($index);

        for ($t = floor(log($input, $base)); $t >= 0; $t--) {
            $bcp = bcpow($base, $t);
            $a   = floor($input / $bcp) % $base;
            $out = $out . substr($index, $a, 1);
            $input  = $input - ($a * $bcp);
        }

        $shortId = strrev($out);

        update_usermeta ($bp->loggedin_user->id,'facestream_'.$shortId,$url);
        $url = $bp->root_domain.'/'.$shortId;

        return $url;
    }else{
        return false;
    }
}

add_action('wp','facestream_resolveShortUrl');

function facestream_resolveShortUrl($url){

    global $wpdb;

    //resolving hooked to 404
    if(is_404()){
        $short_id = str_replace("/","",$_SERVER['REQUEST_URI']);
        if($short_id){
            $usermeta = $wpdb->get_row("SELECT * FROM {$wpdb->usermeta} WHERE meta_key='facestream_".$short_id."'");
            $url = $usermeta->meta_value;
            if($url){
                header('location:'.$url);
            }
        }
    }
}


//add styles
add_action('wp_print_styles', 'add_facebook_style');

function add_facebook_style() {
    $myStyleUrl = WP_PLUGIN_URL . '/facestream/css/style.css';
    $myStyleFile = WP_PLUGIN_DIR . '/facestream/css/style.css';
    if ( file_exists($myStyleFile) ) {
        wp_register_style('facestreamcss', $myStyleUrl);
        wp_enqueue_style( 'facestreamcss');
    }
}

?>