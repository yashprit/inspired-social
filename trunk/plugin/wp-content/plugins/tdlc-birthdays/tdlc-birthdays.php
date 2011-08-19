<?php
/*
Plugin Name: TDLC Birthdays
Plugin URI: http://www.t0m.fr/2009/wordpress/plugin-buddypress-tdlc-birthdays.html
Description: This simple Buddypress Widget uses a Birthday field from Buddypress extended profile to display a list of upcoming birthdays of the user's friends. English, French, German, Hungarian, Italian, Japanese, Polish, Russian and Spanish languages available.
Author: Tom Granger
Version: 0.3.1
Author URI: http://www.t0m.fr/
Licensed under the The GNU General Public License 3.0 (GPL) http://www.gnu.org/licenses/gpl.html
*/

class BuddyWithABirthdate {
	// property declaration
	public $birthdate;
	public $id;

	// constructor (PHP4 style for backwards compatibility)
	function BuddyWithABirthdate($id, $b) {
		$this->birthdate = $b;
		$this->id = $id;
	}

	public function isBirthdayToday() {
		if ((date_i18n("n")==date("n",$this->birthdate))&&(date_i18n("j")==date("j",$this->birthdate)))
			return true;
		else
			return false;
	}

	public function isBirthdayWithinNDays($offset) {
		// Lets calculate the max date according to the offset setting
  		$upcoming = mktime(0, 0, 0, date_i18n("m"), date_i18n("d")+$offset, date_i18n("Y"));
		// Now perform the check and return the answer.
		$bday = $this->getNextBirthday();
		if (($bday > time()) && ($bday <= $upcoming))
			return true;
		else 
			return false;
	}

	public function getNextBirthday() {
		if (
			(date("n",$this->birthdate) < date_i18n("n"))
			|| 
			(
				(date("n",$this->birthdate) == date_i18n("n"))
 				&& 
 				(date("j",$this->birthdate) < date_i18n("j"))
 			)
		)
			return mktime(0, 0, 0, date("m", $this->birthdate), date("d", $this->birthdate), date_i18n("Y")+1);
		else
			return mktime(0, 0, 0, date("m", $this->birthdate), date("d", $this->birthdate), date_i18n("Y"));
	}

	// Returns the age the buddy is going to celebrate next (ie NOT current age, except if the birthday is today)
	public function getAge() {
		return date("Y",$this->getNextBirthday())-date("Y",$this->birthdate);
	}
}

class TDLCBirthday {
	// Property declaration
	private $bdfid;
	private $offset;
	private $hideage;
	private $friendsarray;

	// constructor (PHP4 style for backwards compatibility)
	function TDLCBirthday($options) {
		if (is_array($options)) {
			if(is_numeric($options['bdfid']))
				$this->bdfid = $options['bdfid'];
			else
				$this->bdfid = xprofile_get_field_id_from_name($options['bdfid']);
			$this->offset = $options['offset'];
			$this->hideage = $options['hide_age'];
		}
	}

	public function printFriendsBirthdays($userid) {
		// Fill out the friends array with the connected user's friends
		if (!empty($userid)) {
			$this->friendsarray = BP_Friends_Friendship::get_friend_user_ids($userid);
			// user's got some friends, let's print their birthdays
			if (!empty($this->friendsarray)){ 
				$this->printBirthdays();
			}
			// user is logged in but has no friends
			else echo __('You should make some friends to see their birthday here!', 'tdlc-birthdays');
		}
		// user is not logged in
		else echo __('You must be logged in to see the birthday of your friends.', 'tdlc-birthdays');
	}
	
	public function printFollowedBirthdays($userid, $friendstoo) {
		// Fill out the friends array with followed people
		if (!empty($userid)) {
			if (function_exists('bp_follow_get_following')) {
				$this->friendsarray = array();
				// Fill out the friends array with every followed user and friends if desired
				if($friendstoo) {
					$this->friendsarray = array_unique(array_merge((array)bp_follow_get_following(array( 'user_id' => $userid )), (array)BP_Friends_Friendship::get_friend_user_ids($userid)));
				}
				else $this->friendsarray = bp_follow_get_following(array( 'user_id' => $userid ));
				if (!empty($this->friendsarray)){ 
					$this->printBirthdays();
				}
				// user is logged in but does not follow people
				else echo __('You should follow people to see their birthday here!', 'tdlc-birthdays');
			}
			// Followers plugin not activated
			else echo __('Followers plugin should be enabled to see the birthday of followed people', 'tdlc-birthdays'); 
		}
		// user is not logged in
		else echo __('You must be logged in to see the birthday of the people you follow.', 'tdlc-birthdays');
	}

	public function printAllBirthdays() {
		$this->printBirthdays(true);
	}	
	
	private function printBirthdays($showeveryone = false) {
		global $wpdb, $bp;
		
		// Build birthdates query depending on widget settings (friends or everyone) and availability of the spam flag in the database
		if($showeveryone) {
			$spam_column_test = $wpdb->get_row("SELECT ID FROM $wpdb->users WHERE ID=1 AND spam != 1");
			if(empty($spam_column_test)) 
				$sql = $wpdb->prepare( "SELECT user_id, value FROM {$bp->profile->table_name_data} WHERE field_id = %d", $this->bdfid); 
			else
				$sql = $wpdb->prepare( "SELECT profile.user_id, profile.value FROM {$bp->profile->table_name_data} profile INNER JOIN $wpdb->users users ON profile.user_id = users.id AND spam != 1 WHERE profile.field_id = %d", $this->bdfid);
		}
		else {
			$sql = $wpdb->prepare( "SELECT user_id, value FROM {$bp->profile->table_name_data} WHERE field_id = %d AND user_id IN (".implode(',', $this->friendsarray).")", $this->bdfid); 
		}
		
		$profiledata = $wpdb->get_results($sql);
		
		foreach ($profiledata as $userobj) {
			$bdate = $userobj->value + $this->serveroffset;
			$buddy = new BuddyWithABirthdate($userobj->user_id, $bdate);
			// If it's this buddy's birthday today, push it in the array.
			if($buddy->isBirthdayToday()) {
				$happybday[] = $buddy;
			}
			// Or if this buddy's birthday is soon, push it in this other array.
			else if($buddy->isBirthdayWithinNDays($this->offset)) {
				$upcoming[] = $buddy;
			}
		}
		// If there are no birthdays to announce...
		if (empty($happybday)&&empty($upcoming)) {
			switch ($this->offset) {
				case 0:
					echo __('No birthday today...', 'tdlc-birthdays');
					break;
				case 1:
					echo __('No birthdays today or tomorrow...', 'tdlc-birthdays');
					break;
				default:
					printf(__('No birthdays in the next %d days...', 'tdlc-birthdays'), $this->offset);
					break;
			}
		}
		// Else just print the lists out !
		else {
			if (!empty($happybday)) $this->happy_bday($happybday); 
			if (!empty($upcoming)) $this->upcoming_bdays($upcoming);
		}
	}

	// This short function handles the printing of the Today's Birthdays list
	private function happy_bday($list) {
		echo '<ul>';
		foreach($list as $buddy) {
			$bpbuddy = new BP_Core_User($buddy->id);
			?>
				<li style="min-height:60px;*height:60px">
					<div style="float:left; padding:0 5px 0 0;">
					<a href="<?php echo $bpbuddy->user_url ?>"><?php echo $bpbuddy->avatar_thumb ?></a></div>
					<?php
					echo __('<strong>Happy birthday to ', 'tdlc-birthdays').$bpbuddy->user_link;
					if ($this->hideage==1 || $buddy->getAge()==0)
						echo __('!</strong>', 'tdlc-birthdays');
					else
						echo __(' who turned ', 'tdlc-birthdays').$buddy->getAge().__(' today!</strong>', 'tdlc-birthdays');
					?>
				</li>
			<?php
		}
		echo "</ul>";
	}

	// This other short function handles the printing of the Upcoming Birthdays list
	private function upcoming_bdays($list) {
		// Sort buddies by closest birthday
		usort($list, "compareBuddiesBirthdays");
		echo __('Upcoming birthdays:', 'tdlc-birthdays');
		echo '<ul>';
		foreach($list as $buddy) {
			$bpbuddy = new BP_Core_User($buddy->id);
			?>
				<li>
					<a href="<?php echo $bpbuddy->user_url ?>"><?php echo $bpbuddy->avatar_mini ?></a>
				 	<div style="display:block;margin-left:40px;padding:7px 0 10px 0">
					<?php 
					if ($this->hideage==1 || $buddy->getAge()==0) 
						echo $bpbuddy->user_link.sprintf(__(' (%1$d/%2$d)', 'tdlc-birthdays'), date("m",$buddy->birthdate), date("j",$buddy->birthdate));
					else
						echo $bpbuddy->user_link.sprintf(__(' (%1$d/%2$d, %3$d years old)', 'tdlc-birthdays'), date("m",$buddy->birthdate), date("j",$buddy->birthdate), $buddy->getAge());
					?>
					</div>
				</li>
			<?php
		}
		echo "</ul>";
	}
}

// Comparaison function for sorting birthdays
function compareBuddiesBirthdays($buddya, $buddyb) {
	if ( $buddya->getNextBirthday() == $buddyb->getNextBirthday() )
		return 0;
	return ( $buddya->getNextBirthday() < $buddyb->getNextBirthday() ) ? -1 : 1;
}

// Get all WP users ids - deprecated
function getAllWPUsersIds() {
	global $wpdb;
	$has_spam_column = $wpdb->query("SELECT ID, display_name FROM $wpdb->users WHERE spam != 1 ORDER BY ID");	  
	if ($has_spam_column === False) {
		$wp_user_search = $wpdb->get_results("SELECT ID, display_name FROM $wpdb->users ORDER BY ID");	
	}
	else {
		$wp_user_search = $wpdb->get_results("SELECT ID, display_name FROM $wpdb->users WHERE spam != 1 ORDER BY ID");	
	}
	foreach ( $wp_user_search as $userid ) {
		$userids[] = (int) $userid->ID;
	}
	return $userids;
}

// Standard WP widget function
function widget_tdlcBirthdays($args) {
	extract($args);
	$options = get_option("widget_tdlcBirthdays");
	echo $before_widget;
	// Check for the required settings
	if (!is_array( $options )||empty($options['bdfid']))
		echo __('Please configure mandatory settings in the control panel.', 'tdlc-birthdays');
	// Run the main func
	else {
		echo $before_title.$options['title'].$after_title;
		$tdlcBirthdays = new TDLCBirthday($options);
		global $bp;
		switch ($options['show_all_bdays']) {
		case 1:
			// everyone
			$tdlcBirthdays->printAllBirthdays();
			break;
		case 2:
			// followed 
			$tdlcBirthdays->printFollowedBirthdays($bp->loggedin_user->id, false);
			break;
		case 3:
			// followed & friends
			$tdlcBirthdays->printFollowedBirthdays($bp->loggedin_user->id, true);
			break;
		default:
			$tdlcBirthdays->printFriendsBirthdays($bp->loggedin_user->id);
		}
	}
	echo $after_widget;
}

// Standard WP Widget Settings panel
function tdlcBirthdays_control() {
	$options = get_option("widget_tdlcBirthdays");
	if (function_exists('bp_follow_get_following')) {
		$bpfollowenabled = true;
	}
	if (!is_array( $options )) {
		$options = array(
				'title' => 'Birthdays',
				'bdfid' => '',
				'offset' => '7',
				'hide_age' => '',
				'show_all_bdays' => ''
		);
	}
	if ($_POST['tdlcBirthdays-Submit']) {
		$options['title'] = htmlspecialchars($_POST['tdlcBirthdays-WidgetTitle']);
		$options['bdfid'] = htmlspecialchars($_POST['tdlcBirthdays-BirthdayFieldId']);
		$submittedOffset = (int)abs($_POST['tdlcBirthdays-Offset']);
		if (is_numeric($submittedOffset)) {
			if ($submittedOffset > 364) $submittedOffset = 364;
			$options['offset'] = $submittedOffset;
		}
		$options['hide_age'] = htmlspecialchars($_POST['tdlcBirthdays-HideAge']);
		$options['show_all_bdays'] = htmlspecialchars($_POST['tdlcBirthdays-ShowAllBdays']);
		update_option("widget_tdlcBirthdays", $options);
	}
?>
	<p>
		<label for="tdlcBirthdays-WidgetTitle"><?php echo __('Widget Title: ', 'tdlc-birthdays'); ?>
		<input type="text" id="tdlcBirthdays-WidgetTitle" name="tdlcBirthdays-WidgetTitle" value="<?php echo $options['title'];?>" />
		</label><br />
	</p>
	<p>
		<label for="tdlcBirthdays-BirthdayFieldId"><?php echo __('Birthday field ID or exact Name: ', 'tdlc-birthdays'); ?>
		<input type="text" id="tdlcBirthdays-BirthdayFieldId" name="tdlcBirthdays-BirthdayFieldId" value="<?php echo $options['bdfid'];?>" />
		</label><br />
		<?php echo __('<small>Create a <strong>datebox</strong> in Buddypress Profile Configuration to store the birthday dates and paste its ID or Name here.</small>', 'tdlc-birthdays'); ?>
	</p>
	<p>
		<?php echo __('Display birthdays of :', 'tdlc-birthdays'); ?>
				<ul>
					<li>
						<label for="tdlcBirthdays-ShowAllBdays">
						<input type="radio" id="tdlcBirthdays-ShowAllBdays" name="tdlcBirthdays-ShowAllBdays" value="1" <?php if($options['show_all_bdays']==1) echo 'checked="checked"'; ?> />
						<?php echo __(' everyone ', 'tdlc-birthdays'); ?>
						</label>
					</li>
					<li>
						<label for="tdlcBirthdays-ShowFriendsBdays">
						<input type="radio" id="tdlcBirthdays-ShowFriendsBdays" name="tdlcBirthdays-ShowAllBdays" value="0" <?php if($options['show_all_bdays']==0 || $options['show_all_bdays']=='') echo 'checked="checked"'; ?> />
						<?php echo __(' friends only ', 'tdlc-birthdays'); ?>
						</label>
					</li>
					<li>
						<label for="tdlcBirthdays-ShowFollowedBdays">
						<input type="radio" id="tdlcBirthdays-ShowFollowedBdays" name="tdlcBirthdays-ShowAllBdays" value="2" <?php if($options['show_all_bdays']==2) echo 'checked="checked"'; if(!$bpfollowenabled) echo ' disabled="disabled"'; ?> />
						<?php echo __(' followed people only ', 'tdlc-birthdays'); ?>
						</label>
					</li>
					<li>
						<label for="tdlcBirthdays-ShowFollowedAndFriendsBdays">
						<input type="radio" id="tdlcBirthdays-ShowFollowedAndFriendsBdays" name="tdlcBirthdays-ShowAllBdays" value="3" <?php if($options['show_all_bdays']==3) echo 'checked="checked"'; if(!$bpfollowenabled) echo ' disabled="disabled"'; ?> />
						<?php echo __(' friends and followed people ', 'tdlc-birthdays'); ?>
						</label>
					</li>
				</ul>
		<?php echo __('<small>For \'friends only\', the user must be registered, connected and have some friends!</small>', 'tdlc-birthdays'); ?>
			<br />
		<?php if(!$bpfollowenabled){
			echo __('<small>For \'followed people\' you must install Andy Peatling\'s <a href="http://wordpress.org/extend/plugins/buddypress-followers/">Followers plugin</a></small>', 'tdlc-birthdays'); 
			}
			else echo __('<small>For \'followed people\', the user must be registered, connected and follow people!</small>','tdlc-birthdays');
		?>
	</p>
	<p>
		<label for="tdlcBirthdays-Offset"><?php echo __('Display birthdays happening in the next: ', 'tdlc-birthdays'); ?>
		<input type="text" id="tdlcBirthdays-Offset" name="tdlcBirthdays-Offset" size="2" value="<?php echo $options['offset'];?>" /><?php echo __(' days.', 'tdlc-birthdays'); ?></label><br />
		<?php echo __('<small>Set to 0 if you don\'t want to display upcoming birthdays.</small>', 'tdlc-birthdays'); ?>
	</p>
	<p>
		<label for="tdlcBirthdays-HideAge"><?php echo __('Hide ages', 'tdlc-birthdays'); ?>
		<input type="checkbox" id="tdlcBirthdays-HideAge" name="tdlcBirthdays-HideAge" value="1" <?php if($options['hide_age']==1) echo 'checked="checked"'; ?> /></label><br />
		<?php echo __('<small>Check this if you don\'t want the widget to tell how old people are!</small>', 'tdlc-birthdays'); ?>
		<input type="hidden" id="tdlcBirthdays-Submit" name="tdlcBirthdays-Submit" value="1" />
	</p>
<?php
}

// Fire up !
function tdlcBirthdays_init() {
	load_plugin_textdomain( 'tdlc-birthdays', 'wp-content/plugins/tdlc-birthdays/languages');
	register_sidebar_widget(__('Birthdays'), 'widget_tdlcBirthdays', 'tdlc-birthdays');    
	register_widget_control(   'Birthdays', 'tdlcBirthdays_control', 300, 200 );
}

add_action("bp_init", "tdlcBirthdays_init");
 
?>
