jQuery(document).ready(function(){

    /* Linkback */
    jQuery('#spread-the-word').on('click','#bp-media-add-linkback',function(){
        var data = {
            action: 'bp_media_linkback',
            linkback: jQuery('#bp-media-add-linkback:checked').length
        };
        jQuery.post(bp_media_admin_ajax,data,function(response){
            });
    })

    /* Fetch Feed */
    var bp_media_news_section = jQuery('#latest-news');
    if(bp_media_news_section.length>0){
        var data = {
            action: 'bp_media_fetch_feed'
        };
        jQuery.post(bp_media_admin_ajax,data,function(response){
            bp_media_news_section.find('.inside').html(response);
        });
    }

    /* Select Request */
    jQuery('#bp-media-settings-boxes').on('change', '#select-request', function(){
        if(jQuery(this).val()){
            jQuery('#bp_media_settings_form .bp-media-metabox-holder').html()
            jQuery('#bp_media_settings_form .bp-media-metabox-holder').html('<div class="support_form_loader"></div>');
            var data = {
                action: 'bp_media_select_request',
                form: jQuery(this).val()
            };

            // since 2.8 ajaxurl is always defined in the admin header and points to admin-ajax.php
            jQuery.post(ajaxurl, data, function(response) {
                jQuery('#bp_media_settings_form .bp-media-metabox-holder').html()
                jQuery('#bp_media_settings_form .bp-media-metabox-holder').html(response).fadeIn('slow');
            });
        }
    });

    /* Cancel Request */
    jQuery('#bp-media-settings-boxes').on('click', '#cancel-request', function(){
        if(jQuery(this).val()){
            jQuery('#bp_media_settings_form .bp-media-metabox-holder').html()
            jQuery('#bp_media_settings_form .bp-media-metabox-holder').html('<div class="support_form_loader"></div>');
            var data = {
                action: 'bp_media_cancel_request'
            };

            // since 2.8 ajaxurl is always defined in the admin header and points to admin-ajax.php
            jQuery.post(ajaxurl, data, function(response) {
                jQuery('#bp_media_settings_form .bp-media-metabox-holder').html()
                jQuery('#bp_media_settings_form .bp-media-metabox-holder').html(response).fadeIn('slow');
            });
        }
    });

    /* Submit Request */
    jQuery('.bp-media-support').on('submit', '#bp_media_settings_form', function(e){
        e.preventDefault();
        var data = {
            action: 'bp_media_submit_request',
            form_data: jQuery('form').serialize()
        };

        // since 2.8 ajaxurl is always defined in the admin header and points to admin-ajax.php
        jQuery.post(ajaxurl, data, function(response) {
            jQuery('#bp_media_settings_form .bp-media-metabox-holder').html()
            jQuery('#bp_media_settings_form .bp-media-metabox-holder').html(response).fadeIn('slow');
        });
    });
    
    function fireRequest(data) {
        return jQuery.post(ajaxurl, data, function(response){
            if(response != 0){
                var redirect = false;
                var progw = Math.ceil((((parseInt(response)*20)+parseInt(data.values['finished']))/parseInt(data.values['total'])) *100);
                if(progw>100){
                    progw=100;
                    redirect=true
                };
                jQuery('#rtprogressbar>div').css('width',progw+'%');
                finished = jQuery('#rtprivacyinstaller span.finished').html();
                jQuery('#rtprivacyinstaller span.finished').html(parseInt(finished)+data.count);
                if ( redirect ) {
                    jQuery.post(ajaxurl, {
                        action: 'bp_media_privacy_redirect'
                    }, function(response){
                        window.location = settings_url;
                    });
                }
            } else {
                jQuery('#map_progress_msgs').html('<div class="map_mapping_failure">Row '+response+' failed.</div>');
            }
        });
    }
    
    jQuery('#bpmedia-bpalbumimporter').on('change','#bp-album-import-accept',function(){
        jQuery('.bp-album-import-accept').toggleClass('i-accept');
        jQuery('.bp-album-importer-wizard').slideToggle();
    });

    jQuery('#rtprivacyinstall').click(function(e){
        e.preventDefault();
        $progress_parent = jQuery('#rtprivacyinstaller');
        $progress_parent.find('.rtprivacytype').each(function(){
            $type=jQuery(this).attr('id');
            if($type=='total'){
                $values=[];
                jQuery(this).find('input').each(function(){

                    $values [jQuery(this).attr('name')]=[jQuery(this).val()];

                });
                $data = {};
                for(var i=1;i<=$values['steps'][0];i++ ){
                    $count=20;
                    if(i==$values['steps'][0]){
                        $count=parseInt($values['laststep'][0]);
                        if($count==0){
                            $count=20
                        };
                    }
                    newvals = {
                        'page':i,
                        'action':'bp_media_privacy_install',
                        'count':$count,
                        'values':$values
                    }
                    $data[i] = newvals;
                }
                var $startingpoint = jQuery.Deferred();
                $startingpoint.resolve();
                jQuery.each($data, function(i, v){
                    $startingpoint = $startingpoint.pipe( function() {
                        return fireRequest(v);
                    });
                });


            }
        });
    });

    function fireimportRequest(data) {
        return jQuery.getJSON(ajaxurl, data, function(response){
            favorites = false;
            if(response){
                var redirect = false;
                var media_progw = Math.ceil((((parseInt(response.page)*5)+parseInt(data.values['finished']))/parseInt(data.values['total'])) *100);
                comments_total = jQuery('#bpmedia-bpalbumimporter .bp-album-comments span.total').html();
                users_total = jQuery('#bpmedia-bpalbumimporter .bp-album-users span.total').html();
                media_total = jQuery('#bpmedia-bpalbumimporter .bp-album-media span.total').html();
                comments_finished = jQuery('#bpmedia-bpalbumimporter .bp-album-comments span.finished').html();
                users_finished = jQuery('#bpmedia-bpalbumimporter .bp-album-users span.finished').html();
                var comments_progw = Math.ceil((((parseInt(response.comments))+parseInt(comments_finished))/parseInt(comments_total)) *100);
                var users_progw = Math.ceil((parseInt(response.users)/parseInt(users_total)) *100);
                if(media_progw>100 || media_progw==100 ){
                    media_progw=100;
                    favorites=true
                };
                jQuery('.bp-album-media #rtprogressbar>div').css('width',media_progw+'%');
                jQuery('.bp-album-comments #rtprogressbar>div').css('width',comments_progw+'%');
                jQuery('.bp-album-users #rtprogressbar>div').css('width',users_progw+'%');
                media_finished = jQuery('#bpmedia-bpalbumimporter .bp-album-media span.finished').html();
                if (parseInt(media_finished)<parseInt(media_total))
                    jQuery('#bpmedia-bpalbumimporter .bp-album-media span.finished').html(parseInt(media_finished)+data.count);
                jQuery('#bpmedia-bpalbumimporter .bp-album-comments span.finished').html(parseInt(response.comments)+parseInt(comments_finished));
                jQuery('#bpmedia-bpalbumimporter .bp-album-users span.finished').html(parseInt(response.users));
                if ( favorites ) {
                    favorite_data = {
                        'action':'bp_media_bp_album_import_favorites'
                    }
                    jQuery.post(ajaxurl,favorite_data,function(response){
                        if(response.favorites!==0||response.favorites!=='0'){
                            if(!jQuery('.bp-album-favorites').length)
                                jQuery('.bp-album-comments').after('<br /><div class="bp-album-favorites"><strong>User\'s Favorites: <span class="finished">0</span> / <span class="total">'+response.users+'</span></strong><div id="rtprogressbar"><div style="width:0%"></div></div></div>');
                            $favorites = {};
                            if (response.offset != 0 || response.offset != '0')
                                start = response.offset*1+1;
                            else
                                start = 1
                            for(var i=start;i<=response.users;i++ ){
                                $count=1;
                                if(i==response.users){
                                    $count=parseInt(response.users % $count);
                                    if($count==0){
                                        $count=1;
                                    }
                                }
                                
                                newvals = {
                                    'action':'bp_media_bp_album_import_step_favorites',
                                    'offset':(i-1)*1,
                                    'redirect':i==response.users
                                }
                                $favorites[i] = newvals;
                            }
                            var $startingpoint = jQuery.Deferred();
                            $startingpoint.resolve();
                            jQuery.each($favorites, function(i, v){
                                $startingpoint = $startingpoint.pipe( function() {
                                    return fireimportfavoriteRequest(v);
                                });
                            });
                            
                        } else {
                            window.setTimeout(reload_url, 2000);
                        }
                    },'json');
                }
            } else {
                jQuery('#map_progress_msgs').html('<div class="map_mapping_failure">Row '+response.page+' failed.</div>');
            }
        });
    }

    function fireimportfavoriteRequest(data) {
        return jQuery.post(ajaxurl, data, function(response){
            redirect=false;
            favorites_total = jQuery('#bpmedia-bpalbumimporter .bp-album-favorites span.total').html();
            favorites_finished = jQuery('#bpmedia-bpalbumimporter .bp-album-favorites span.finished').html();
            jQuery('#bpmedia-bpalbumimporter .bp-album-favorites span.finished').html(parseInt(favorites_finished)+1);
            var favorites_progw = Math.ceil((parseInt(favorites_finished+1)/parseInt(favorites_total)) *100);
            if(favorites_progw>100 || favorites_progw==100 ){
                favorites_progw=100;
                redirect=true;
            }
            jQuery('.bp-album-favorites #rtprogressbar>div').css('width',favorites_progw+'%');
            if(redirect){
                window.setTimeout(reload_url, 2000);
            } 
        });
    }
    
    function reload_url(){
        window.location = document.URL;
    }

    jQuery('#bpmedia-bpalbumimport-cleanup').click(function(e){
        e.preventDefault();
        jQuery.post(ajaxurl, {
            action: 'bp_media_bp_album_cleanup'
        }, function(response){
            window.location = settings_bp_album_import_url;
        });

    });

    jQuery('#bpmedia-bpalbumimporter').on('click','#bpmedia-bpalbumimport',function(e){
        e.preventDefault();
        if(!jQuery('#bp-album-import-accept').prop('checked')){
            jQuery('html, body').animate({
                scrollTop: jQuery( '#bp-album-import-accept' ).offset().top
            }, 500);
            var $el = jQuery('.bp-album-import-accept'),
            x = 500,
            originalColor = '#FFEBE8',
            i = 3; //counter

            (function loop() { //recurisve IIFE
                $el.css("background-color", "#EE0000");    
                setTimeout(function () {
                    $el.css("background-color", originalColor);
                    if (--i) setTimeout(loop, x); //restart loop
                }, x);
            }());
            return;
        } else {
            jQuery(this).prop('disabled', true);
        }
        wp_admin_url = ajaxurl.replace('admin-ajax.php','');
        if (!jQuery('.bpm-ajax-loader').length)
            jQuery(this).after(' <img class="bpm-ajax-loader" src="'+wp_admin_url+'images/wpspin_light.gif" /> <strong>'+bp_media_admin_strings.no_refresh+'</strong>');
        
        
        $progress_parent = jQuery('#bpmedia-bpalbumimport');
        $values=[];
        jQuery(this).parent().find('input').each(function(){
            $values [jQuery(this).attr('name')]=[jQuery(this).val()];

        });
        
        if ( $values['steps'][0] == 0 )
            $values['steps'][0]=1;

        $data = {};
        for(var i=1;i<=$values['steps'][0];i++ ){
            $count=5;
            if(i==$values['steps'][0]){
                $count=parseInt($values['laststep'][0]);
                if($count==0){
                    $count=5
                };
            }
            newvals = {
                'page':i,
                'action':'bp_media_bp_album_import',
                'count':$count,
                'values':$values
            }
            $data[i] = newvals;
        }
        var $startingpoint = jQuery.Deferred();
        $startingpoint.resolve();
        jQuery.each($data, function(i, v){
            $startingpoint = $startingpoint.pipe( function() {
                return fireimportRequest(v);
            });
        });


    });

    jQuery('#bp-media-settings-boxes').on('click','.interested',function(){
        jQuery('.interested-container').removeClass('hidden');
        jQuery('.choice-free').attr('required','required');
    });
    jQuery('#bp-media-settings-boxes').on('click','.not-interested',function(){
        jQuery('.interested-container').addClass('hidden');
        jQuery('.choice-free').removeAttr('required');
    });

    jQuery('#video-transcoding-main-container').on('click','.video-transcoding-survey',function(e){
        e.preventDefault();
        var data = {
            action: 'bp_media_convert_videos_form',
            email: jQuery('.email').val(),
            url: jQuery('.url').val(),
            choice: jQuery('input[name="choice"]:checked').val(),
            interested: jQuery('input[name="interested"]:checked').val()
        }
        jQuery.post(ajaxurl, data, function(response){
            jQuery('#video-transcoding-main-container').html('<p><strong>'+response+'</strong></p>');
        });
        return false;
    });
    
    jQuery('#bpmedia-bpalbumimporter').on('click','.deactivate-bp-album',function(e){
        e.preventDefault();
        $bpalbum = jQuery(this);
        var data = {
            action: 'bp_media_bp_album_deactivate'
        }
        jQuery.get(ajaxurl, data, function(response) {
            if(response)
                location.reload();
            else
                $bpalbum.parent().after('<p>'+bp_media_admin_strings.something_went_wrong+'</p>');
        });
    });


});
