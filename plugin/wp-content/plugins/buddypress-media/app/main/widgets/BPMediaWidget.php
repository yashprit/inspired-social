<?php
/**
 * Creates the BuddyPress Media Widget that is used to display media in sidebars.
 *
 * @package BuddyPressMedia
 * @subpackage Widgets
 *
 * @author Faishal Saiyed <faishal.saiyed@rtcamp.com>
 * @author Umesh Nevase <umesh.nevase@rtcamp.com>
 * @author Joshua Abenazer <joshua.abenazer@rtcamp.com>
 */
if ( ! class_exists( 'BPMediaWidget' ) ) {

	class BPMediaWidget extends WP_Widget {

		/**
		 * Constructs the BPMedia Widget as a child of WP_Widget
		 */
		function __construct() {
			$widget_ops = array( 'classname' => 'buddypress-media-widget', 'description' => __( "The most recent/popular media uploaded on your site", 'buddypress-media' ) );
			parent::__construct( 'buddypress-media-wid', __( 'BuddyPress Media Widget', 'buddypress-media' ), $widget_ops );
		}

		/**
		 * Displays widget in the frontend (sidebar)
		 *
		 * @param array $args Arguments passed to the widget instance
		 * @param array $instance The title, etc of the specific widget instance
		 */
		function widget( $args, $instance ) {
			extract( $args );
			echo $before_widget;
			$title = apply_filters( 'widget_title', empty( $instance[ 'title' ] ) ? __( 'BuddyPress Media', 'buddypress-media' ) : $instance[ 'title' ], $instance, $this->id_base );
			$allow = array( );
			$allowed = array( );
			if ( empty( $instance[ 'number' ] ) || ! $number = absint( $instance[ 'number' ] ) ) {
				$number = 10;
			}
			$wdType = isset( $instance[ 'wdType' ] ) ? esc_attr( $instance[ 'wdType' ] ) : 'recent';
			if ( isset( $instance[ 'allow_all' ] ) && (bool) $instance[ 'allow_all' ] === true )
				$allow[ ] = 'all';
			if ( isset( $instance[ 'allow_image' ] ) && (bool) $instance[ 'allow_image' ] === true )
				$allow[ ] = 'image';
			if ( isset( $instance[ 'allow_audio' ] ) && (bool) $instance[ 'allow_audio' ] === true )
				$allow[ ] = 'audio';
			if ( isset( $instance[ 'allow_video' ] ) && (bool) $instance[ 'allow_video' ] === true )
				$allow[ ] = 'video';

			global $bp_media;
			$enabled = $bp_media->enabled();
			unset( $enabled[ 'album' ] );
			unset( $enabled[ 'upload' ] );
			foreach ( $allow as $type ) {

				if ( $type != 'all' ) {
					echo '<br>';
					if ( $enabled[ $type ] ) {
						$allowed[ ] = $type;
					}
				} else {
					$allowed[ ] = $type;
				}
			}
			echo $before_title . $title . $after_title;
			if ( $wdType == "popular" ) {
				$orderby = 'comment_count';
			} else {
				$orderby = 'date';
			}

			$strings = array(
				'all' => __( 'All', 'buddypress-media' ),
				'audio' => __( 'Music', 'buddypress-media' ),
				'video' => __( 'Videos', 'buddypress-media' ),
				'image' => __( 'Photos', 'buddypress-media' )
			);
			$widgetid = $args[ 'widget_id' ];
			if ( ! is_array( $allowed ) || count( $allowed ) < 1 ) {
				echo '<p>';
				printf(
						__(
								'Please configure this widget
									<a href="%s" target="_blank"
									title="Configure BuddyPress Media Widget">
									here</a>.', 'rtPanel'
						), admin_url( '/widgets.php' )
				);
				echo '</p>';
			} else {
				if ( count( $allowed ) > 3 ) {
					unset( $allowed[ 'all' ] );
				}
				$allowMimeType = array( );
				?>
				<div id="<?php echo $wdType; ?>-media-tabs" class="media-tabs-container media-tabs-container-tabs">
					<ul><?php
				foreach ( $allowed as $type ) {
					if ( $type != 'all' ) {
						array_push( $allowMimeType, $type );
					}
					?>
							<li><a href="#<?php echo $wdType; ?>-media-tabs-<?php echo $type; ?>-<?php echo $widgetid; ?>">
							<?php echo $strings[ $type ]; ?>
								</a></li><?php }
						?>
					</ul><?php foreach ( $allowed as $type ) { ?>
						<div id="<?php echo $wdType; ?>-media-tabs-<?php echo $type; ?>-<?php echo $widgetid; ?>" class="bp-media-tab-panel"><?php
					$value = 0;
					if ( is_user_logged_in() ) {
						$value = 2;
					}
					$privacy_query = array(
						array(
							'key' => 'bp_media_privacy',
							'value' => $value,
							'compare' => '<='
						)
					);
					$args = array(
						'post_type' => 'attachment',
						'post_status' => 'any',
						'meta_query' => $privacy_query,
						'posts_per_page' => $number
					);
					if ( $type != 'all' )
						$args[ 'post_mime_type' ] = $type;
					$bp_media_widget_query = new WP_Query( $args );
					if ( $bp_media_widget_query->have_posts() ) {
								?>
								<ul class="widget-item-listing"><?php
						while ( $bp_media_widget_query->have_posts() ) {
							$bp_media_widget_query->the_post();
							try {
								$entry = new BPMediaHostWordpress( get_the_ID() );
								echo $entry->get_media_gallery_content();
							} catch ( Exception $e ) {
								echo '<li>';
								echo $e->getMessage();
								echo '<h3><a>Private</h3>';
								echo '</li>';
							}
						}
								?>
								</ul><?php
					} else {
						$media_string = $type;
						if ( $type === 'all' ) {
							$media_string = 'media';
						}
						_e( 'No ' . $wdType . ' ' . $media_string . ' found', 'buddypress-media' );
					}
					wp_reset_query();
							?>

						</div><?php }
						?>

				</div><?php
			}
			echo $after_widget;
		}

		/**
		 * Processes the widget form
		 *
		 * @param array/object $new_instance The new instance of the widget
		 * @param array/object $old_instance The default widget instance
		 * @return array/object filtered and corrected instance
		 */
		function update( $new_instance, $old_instance ) {
			$instance = $old_instance;
			$instance[ 'wdType' ] = strip_tags( $new_instance[ 'wdType' ] );
			$instance[ 'title' ] = strip_tags( $new_instance[ 'title' ] );
			$instance[ 'number' ] = (int) $new_instance[ 'number' ];
			$instance[ 'allow_audio' ] = ! empty( $new_instance[ 'allow_audio' ] ) ? 1 : 0;
			$instance[ 'allow_video' ] = ! empty( $new_instance[ 'allow_video' ] ) ? 1 : 0;
			$instance[ 'allow_image' ] = ! empty( $new_instance[ 'allow_image' ] ) ? 1 : 0;
			$instance[ 'allow_all' ] = ! empty( $new_instance[ 'allow_all' ] ) ? 1 : 0;

			return $instance;
		}

		/**
		 * Displays the form for the widget settings on the Widget screen
		 *
		 * @param object/array $instance The widget instance
		 */
		function form( $instance ) {
			$wdType = isset( $instance[ 'wdType' ] ) ? esc_attr( $instance[ 'wdType' ] ) : '';
			$title = isset( $instance[ 'title' ] ) ? esc_attr( $instance[ 'title' ] ) : '';
			$number = isset( $instance[ 'number' ] ) ? absint( $instance[ 'number' ] ) : 10;
			$allowAudio = isset( $instance[ 'allow_audio' ] ) ? (bool) $instance[ 'allow_audio' ] : true;
			$allowVideo = isset( $instance[ 'allow_video' ] ) ? (bool) $instance[ 'allow_video' ] : true;
			$allowImage = isset( $instance[ 'allow_image' ] ) ? (bool) $instance[ 'allow_image' ] : true;
			$allowAll = isset( $instance[ 'allow_all' ] ) ? (bool) $instance[ 'allow_all' ] : true;
			?>
			<p><label for="<?php echo $this->get_field_id( 'wdType' ); ?>"><?php _e( 'Widget Type:', 'buddypress-media' ); ?></label>
				<select  class="widefat" id="<?php echo $this->get_field_id( 'wdType' ); ?>" name="<?php echo $this->get_field_name( 'wdType' ); ?>">
					<option value="recent" <?php if ( $wdType == "recent" ) echo 'selected="selected"'; ?>><?php _e( 'Recent Media', 'buddypress-media' ); ?></option>
					<option value="popular" <?php if ( $wdType == "popular" ) echo 'selected="selected"'; ?>><?php _e( 'Popular Media', 'buddypress-media' ); ?></option>
				</select>
			</p>
			<p><label for="<?php echo $this->get_field_id( 'title' ); ?>"><?php _e( 'Title:', 'buddypress-media' ); ?></label>
				<input class="widefat" id="<?php echo $this->get_field_id( 'title' ); ?>" name="<?php echo $this->get_field_name( 'title' ); ?>" type="text" value="<?php echo $title; ?>" /></p>

			<p><label for="<?php echo $this->get_field_id( 'number' ); ?>"><?php _e( 'Number of posts to show:', 'buddypress-media' ); ?></label>
				<input id="<?php echo $this->get_field_id( 'number' ); ?>" name="<?php echo $this->get_field_name( 'number' ); ?>" type="text" value="<?php echo $number; ?>" size="3" /></p>

			<p>
				<input role="checkbox" type="checkbox" name="<?php echo $this->get_field_name( 'allow_all' ); ?>" id="<?php echo $this->get_field_id( 'allow_all' ); ?>" <?php checked( $allowAll ); ?> /><label for="<?php echo $this->get_field_id( 'allow_all' ); ?>"><?php _e( 'Show All', 'buddypress-media' ); ?></label>
			</p>
			<p>
				<input role="checkbox" type="checkbox" name="<?php echo $this->get_field_name( 'allow_image' ); ?>" id="<?php echo $this->get_field_id( 'allow_image' ); ?>" <?php checked( $allowImage ); ?> /><label for="<?php echo $this->get_field_id( 'allow_image' ); ?>"><?php _e( 'Show Photos', 'buddypress-media' ); ?></label>
			</p>
			<p>
				<input role="checkbox" type="checkbox" name="<?php echo $this->get_field_name( 'allow_audio' ); ?>" id="<?php echo $this->get_field_id( 'allow_audio' ); ?>" <?php checked( $allowAudio ); ?> /> <label for="<?php echo $this->get_field_id( 'allow_audio' ); ?>"><?php _e( 'Show Music', 'buddypress-media' ); ?></label>
			</p>
			<p>
				<input role="checkbox" type="checkbox" name="<?php echo $this->get_field_name( 'allow_video' ); ?>" id="<?php echo $this->get_field_id( 'allow_video' ); ?>" <?php checked( $allowVideo ); ?> />
				<label for="<?php echo $this->get_field_id( 'allow_video' ); ?>"><?php _e( 'Show Videos', 'buddypress-media' ); ?></label>
			</p>

			<?php
		}

	}

}
?>
