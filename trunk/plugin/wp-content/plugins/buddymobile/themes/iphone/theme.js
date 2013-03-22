var $j = jQuery.noConflict();

$j(document).ready(function(){

		$j('#rightnav a').live('click', function(event){
   			$j('#mobileNav').toggleClass('show');
   			$j('#loginNav').removeClass('show');
   			$j('#notifications-header').removeClass('show');		
	});   
	
		$j('#mobileNav ul li a').live('click', function(event){
   			$j(this).addClass('navLoad');
   					
	});

		$j('#leftnav-login a').live('click', function(event){
   			$j('#loginNav').toggleClass('show');	
   			$j('#mobileNav').removeClass('show');	
   			$j('#notifications-header').removeClass('show');
	}); 
	
		$j('#content').live('click', function(event){
   			$j('#mobileNav').removeClass('show');
   			$j('#loginNav').removeClass('show');
   			$j('#notificationsheader').removeClass('show');		
	});   
	
	$j('#notifications-badge').live('touchstart', function(event){
   			$j('#notifications-header').toggleClass('show');
   			$j('#loginNav').removeClass('show');
   			$j('#mobileNav').removeClass('show');	
   				
	}); 
	


		$j('#theme-switch').live('click', function(event){
			$j.cookie( 'bpthemeswitch', 'normal', {path: '/'} );			
	}); 		
	
		$j('#theme-switch-site').live('click', function(event){
			$j.cookie( 'bpthemeswitch', 'mobile', {path: '/'} );			
	});   
		
});

(function(document,navigator,standalone) {
    // prevents links from apps from oppening in mobile safari
    // this javascript must be the first script in your <head>
    if ((standalone in navigator) && navigator[standalone]) {
        var curnode, location=document.location, stop=/^(a|html)$/i;
        document.addEventListener('click', function(e) {
            curnode=e.target;
            while (!(stop).test(curnode.nodeName)) {
                curnode=curnode.parentNode;
            }
            // Condidions to do this only on links to your own app
            // if you want all links, use if('href' in curnode) instead.
            if('href' in curnode && ( curnode.href.indexOf('http') || ~curnode.href.indexOf(location.host) ) ) {
                e.preventDefault();
                location.href = curnode.href;
            }
        },false);
    }
})(document,window.navigator,'standalone');