<?php

/**
 * Description of BPMediaAddon
 *
 * @package BuddyPressMedia
 * @subpackage Admin
 *
 * @author Gagandeep Singh <gagandeep.singh@rtcamp.com>
 * @author Joshua Abenazer <joshua.abenazer@rtcamp.com>
 */
if (!class_exists('BPMediaAddon')) {

    class BPMediaAddon {

        public $enquiry_link = 'http://rtcamp.com/contact/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media';

        public function coming_soon_div() {
            return
                    '<div class="coming-soon coming-soon-l"></div>
				<a class="coming-soon coming-soon-r" href="' . $this->enquiry_link . '" target="_blank">'
                    //<a></a>
                    . '</a>';
        }

        public function get_addons() {
            $addons = array(
				 array(
                    'title' => __('BuddyPress-Media Photo Tagging', 'buddypress-media'),
                    'img_src' => 'http://rtcamp.com/wp-content/uploads/2013/04/bpm-photo-tagging.png',
                    'product_link' => 'http://rtcamp.com/store/buddypress-media-photo-tagging/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media',
                    'desc' => '<p>' . __('BuddyPress Media Instagram adds Instagram like filters to images uploaded with BuddyPress Media.', 'buddypress-media') . '</p>
                    <p><strong>' . __('Important', 'buddypress-media') . ':</strong> ' . __('You need to have ImageMagick installed on your server for this addon to work.', 'buddypress-media') . '</p>',
                    'price' => '$19',
                    'demo_link' => 'http://demo.rtcamp.com/buddypress-media/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media',
                    'buy_now' => 'http://rtcamp.com/store/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media&add-to-cart=37506'
                ),
                array(
                    'title' => __('BuddyPress-Media Instagram', 'buddypress-media'),
                    'img_src' => 'http://cdn.rtcamp.com/wp-content/uploads/2013/03/BuddyPressMedia-Instagram.png',
                    'product_link' => 'http://rtcamp.com/store/buddypress-media-instagram/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media',
                    'desc' => '<p>' . __('BuddyPress Media Instagram adds Instagram like filters to images uploaded with BuddyPress Media.', 'buddypress-media') . '</p>
                    <p><strong>' . __('Important', 'buddypress-media') . ':</strong> ' . __('You need to have ImageMagick installed on your server for this addon to work.', 'buddypress-media') . '</p>',
                    'price' => '$19',
                    'demo_link' => 'http://demo.rtcamp.com/buddypress-media/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media',
                    'buy_now' => 'http://rtcamp.com/store/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media&add-to-cart=34379'
                ),
                array(
                    'title' => __('BuddyPress-Media Kaltura Add-on', 'buddypress-media'),
                    'img_src' => 'http://cdn.rtcamp.com/files/2012/10/new-buddypress-media-kaltura-logo-240x184.png',
                    'product_link' => 'http://rtcamp.com/store/buddypress-media-kaltura/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media',
                    'desc' => '<p>' . __('Add support for more video formats using Kaltura video solution.', 'buddypress-media') . '</p>
                    <p>' . __('Works with Kaltura.com, self-hosted Kaltura-CE and Kaltura-on-premise.', 'buddypress-media') . '</p>',
                    'price' => '$99',
                    'demo_link' => 'http://demo.rtcamp.com/bpm-kaltura/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media',
                    'buy_now' => 'http://rtcamp.com/store/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media&add-to-cart=15446'
                ),
                array(
                    'title' => __('BuddyPress-Media FFMPEG Add-on', 'buddypress-media'),
                    'img_src' => 'http://cdn.rtcamp.com/files/2012/09/ffmpeg-logo-240x184.png',
                    'product_link' => 'http://rtcamp.com/store/buddypress-media-ffmpeg-converter/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media',
                    'desc' => '<p>' . __('Add supports for more audio & video formats using open-source media-node.', 'buddypress-media') . '</p>
                        <p>' . __('Media node comes with automated setup script for Ubuntu/Debian.', 'buddypress-media') . '</p>',
                    'price' => '$49',
                    'demo_link' => 'http://demo.rtcamp.com/bpm-media/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media',
                    'buy_now' => 'http://rtcamp.com/store/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media&add-to-cart=13677'
                )
            );
            $addons = apply_filters('bp_media_addons', $addons);
            foreach ($addons as $addon) {
                $this->addon($addon);
            }
        }

        /**
         *
         * @global type $bp_media
         * @param type $args
         */
        public function addon($args) {
            global $bp_media;

            $defaults = array(
                'title' => '',
                'img_src' => '',
                'product_link' => '',
                'desc' => '',
                'price' => '',
                'demo_link' => '',
                'buy_now' => '',
                'coming_soon' => false,
            );
            $args = wp_parse_args($args, $defaults);
            extract($args);

            $coming_soon ? ' coming-soon' : '';

            $coming_soon_div = ($coming_soon) ? $this->coming_soon_div() : '';
            $addon = '<div class="bp-media-addon">
                <a href="' . $product_link . '"  title="' . $title . '" target="_blank">
                    <img width="240" height="184" title="' . $title . '" alt="' . $title . '" src="' . $img_src . '">
                </a>
                <h4><a href="' . $product_link . '"  title="' . $title . '" target="_blank">' . $title . '</a></h4>
                <div class="product_desc">
                    ' . $desc . '
                </div>
                <div class="product_footer">
                    <span class="price alignleft"><span class="amount">' . $price . '</span></span>
                    <a class="add_to_cart_button  alignright product_type_simple"  href="' . $buy_now . '" target="_blank">' . __('Buy Now', 'buddypress-media') . '</a>
                    <a class="alignleft product_demo_link"  href="' . $demo_link . '" title="' . $title . '" target="_blank">' . __('Live Demo', 'buddypress-media') . '</a>
                </div>'
                    . $coming_soon_div .
                    '</div>';
            echo $addon;
        }

    }

}
?>
