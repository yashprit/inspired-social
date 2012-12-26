// tipsy.hovercard, twitter style hovercards for tipsy
// version 0.1.1
// (c) 2010 René Föhring rf@bamaru.de
// Modified by Mike Martel mike@trenvo.nl
// released under the MIT license

(function($) {
  $.fn.tipsyHoverCard = function(options) {
    if ( ! window.bphc_cache )
	window.bphc_cache = new Array();

    var opts = $.extend({}, $.fn.tipsyHoverCard.defaults, options, $.fn.tipsyHoverCard.forcedOptions);
    this.tipsy(opts);

    function clearHideTimeout(ele) {
      if( ele.data('timeoutId') ) clearTimeout(ele.data('timeoutId'));
      ele.data('timeoutId', null);
    }
    function setHideTimeout(ele) {
      clearHideTimeout(ele);
      var options = ele.tipsy(true).options;
      timeoutId = setTimeout(function() { $(ele).tipsy('hide'); }, options.hideDelay);
      ele.data('timeoutId', timeoutId);
    }

    function show(ele) {
      clearHideTimeout(ele);
      ele.tipsy(opts).tipsy('show');

      var tip = ele.tipsy('tip');
      tip.addClass('tipsy-hovercard');
      tip.data('tipsyAnchor', ele);
      tip.hover(tipEnter, tipLeave);
      ele.data('visible', true);
    }
    function hide(ele) {
      setHideTimeout(ele);
      ele.data('visible', false);
    }

    function enter() {
      var a = $(this);
      var url = ajaxurl;
      var user_id = a.attr('class').split('-')[1];
      if( url && !a.data('ajax-success') ) {
        if ( window.bphc_cache[user_id] ) {
            a.data('ajax-success', true);
            a.attr('title', window.bphc_cache[user_id]);
            a.data('tipsyAnchor');
            if ( a.data('visible') )
                show(a);
        } else {
            var data = {
                    action: 'buddypress_hovercard',
                    userid: user_id
            };
            jQuery.post(url, data, function(response) {
                    a.data('ajax-success', true);
                    a.attr('title', response);
                    a.data('tipsyAnchor');
                    window.bphc_cache[user_id] = response;
                    if ( a.data('visible') )
                        show(a);
            });
        }
      }
      show(a);
    }
    function leave() {
      hide($(this));
    }

    function tipEnter() {
      var a = $(this).data('tipsyAnchor');
      clearHideTimeout(a);
    }
    function tipLeave() {
      var a = $(this).data('tipsyAnchor');
      setHideTimeout(a);
    }

    if( $.fn.hoverIntent && opts.hoverIntent ) {
      // 'out' is called with a latency, even if 'timeout' is set to 0
      // therefore, we're using good ol' mouseleave for out-handling
      var config = $.extend({over: enter, out: function(){}}, opts.hoverIntentConfig);
      this.hoverIntent(config).mouseleave(leave);
    } else {
      this.live("mouseenter", enter)
      this.live("mouseleave", leave)
    }
    return this;
  }
  $.fn.tipsyHoverCard.forcedOptions = {live: false, trigger: 'manual'};
  $.fn.tipsyHoverCard.defaults = {
    gravity: $.fn.tipsy.autoBounds(350,'nw'),
    fade: true,
    fallback: '...',
    html: true,
    hideDelay: 600,
    opacity: 1,
    hoverIntent: true,
    hoverIntentConfig: {
      sensitivity: 3,
      interval: 300,
      timeout: 0
    }
  };
})(jQuery);

jQuery(document).ready(function() {
    // Add hovercards to our avatars
    jQuery("img[class^='avatar user']").tipsyHoverCard();

    // Make our hovercards hoverable (so you can add links inside them)
    jQuery('.tipsy').live('mouseover',function(e){
        clearTimeout(timeoutId);
    });
    jQuery('.tipsy').live('mouseleave',function(e){
        jQuery(this).fadeOut();
    });
// Reload the hovercards after an AJAX call is made
}).ajaxComplete( function(e, xhr, settings) {
    // OK, to make sure it always works: reload on every AJAX response with data - except for Live Notifications
	if ( typeof settings.data !== "undefined" && settings.data.indexOf("action=bpln_check_notification") == -1 )
        // To make (kindof) sure everything is loaded, set a timeout before reloading the hovercards after an ajax call
        setTimeout ( 'jQuery("img[class^=\'avatar user\']").tipsyHoverCard()', 1000 );
});