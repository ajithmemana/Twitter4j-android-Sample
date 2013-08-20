package com.pigmal.android.ex.twitter4j;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitterApp extends Activity implements OnClickListener {
	private static final String TAG = "TWITTER4J";

	private Button buttonLogin;
	private Button readStreamButton;
	private TextView tweetText;
	Button tweetButton;
	Button hashTagButton;
	Button userTagButton;

	private static Twitter twitter;
	private static RequestToken requestToken;
	private static SharedPreferences mSharedPreferences;
	private boolean running = false;
	ListView streamList;
	EditText filterText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSharedPreferences = getSharedPreferences(Const.PREFERENCE_NAME, MODE_PRIVATE);
		tweetText = (TextView) findViewById(R.id.tweetText);
		readStreamButton = (Button) findViewById(R.id.readStream);
		readStreamButton.setOnClickListener(this);
		buttonLogin = (Button) findViewById(R.id.twitterLogin);
		buttonLogin.setOnClickListener(this);
		streamList = (ListView) findViewById(R.id.streamListView);
		tweetButton = (Button) findViewById(R.id.tweetAMessageButton);
		tweetButton.setOnClickListener(this);
		userTagButton = (Button) findViewById(R.id.filterSelectoruser);
		hashTagButton = (Button) findViewById(R.id.filterSelectorhash);
		userTagButton.setOnClickListener(this);
		hashTagButton.setOnClickListener(this);
		filterText = (EditText) findViewById(R.id.filterText);
		filterText.setText("twitter");

		/**
		 * Handle OAuth Callback
		 */

		Uri uri = getIntent().getData();
		if (uri != null && uri.toString().startsWith(Const.CALLBACK_URL)) {
			Toast.makeText(getBaseContext(), "Logged In " + uri.toString(), 1).show();
			String verifier = uri.getQueryParameter(Const.IEXTRA_OAUTH_VERIFIER);
			try {
				// retrieve access token using verifier & Keep the access tokens
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				Editor e = mSharedPreferences.edit();
				e.putString(Const.PREF_KEY_TOKEN, accessToken.getToken());
				e.putString(Const.PREF_KEY_SECRET, accessToken.getTokenSecret());
				e.commit();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}

	}

	protected void onResume() {
		super.onResume();

		if (isConnected()) {
			// If already connected read the Stored Access token and Access
			// secret from Pref.
			String oauthAccessToken = mSharedPreferences.getString(Const.PREF_KEY_TOKEN, "");
			String oAuthAccessTokenSecret = mSharedPreferences.getString(Const.PREF_KEY_SECRET, "");

			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
			Configuration conf = confbuilder.setOAuthConsumerKey(Const.CONSUMER_KEY)
					.setOAuthConsumerSecret(Const.CONSUMER_SECRET).setOAuthAccessToken(oauthAccessToken)
					.setOAuthAccessTokenSecret(oAuthAccessTokenSecret).build();
			// Initialize a new twitter object with the stored consumer keys(x2)
			// & access keys(x2)
			twitter = new TwitterFactory(conf).getInstance();
			buttonLogin.setText(R.string.label_disconnect);
			readStreamButton.setEnabled(true);
			tweetButton.setEnabled(true);
			userTagButton.setEnabled(true);
			hashTagButton.setEnabled(true);
			filterText.setEnabled(true);

		} else {
			// Disable if not logged In
			buttonLogin.setText(R.string.label_connect);
			readStreamButton.setEnabled(false);
			// TODO add more buttons here
			tweetButton.setEnabled(false);
			userTagButton.setEnabled(false);
			hashTagButton.setEnabled(false);
			filterText.setEnabled(false);

		}
	}

	/**
	 * check if the account is authorized
	 * 
	 * @return
	 */
	private boolean isConnected() {
		return mSharedPreferences.getString(Const.PREF_KEY_TOKEN, null) != null;
	}
	// requests a fresh oauth
	private void askOAuth() {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(Const.CONSUMER_KEY);
		configurationBuilder.setOAuthConsumerSecret(Const.CONSUMER_SECRET);
		Configuration configuration = configurationBuilder.build();
		twitter = new TwitterFactory(configuration).getInstance();

		try {
			requestToken = twitter.getOAuthRequestToken(Const.CALLBACK_URL);
			// Toast.makeText(this, "Please authorize this app!",
			// Toast.LENGTH_LONG).show();
			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove Token, Secret from preferences
	 */
	private void disconnectTwitter() {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.remove(Const.PREF_KEY_TOKEN);
		editor.remove(Const.PREF_KEY_SECRET);

		editor.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.twitterLogin :
				if (isConnected()) {
					disconnectTwitter();
					buttonLogin.setText(R.string.label_connect);
				} else {
					askOAuth();
				}
				break;
			case R.id.readStream :
				readStream(0);
			case R.id.tweetAMessageButton :
				tweetAStatus("This is just another test message at " + System.currentTimeMillis());
				break;
			case R.id.filterSelectorhash :
				// Read feed by hash
				readStream(1);
				break;
			case R.id.filterSelectoruser :
				// Read feed by user
				readStream(2);

				break;
		}
	}

	public void tweetAStatus(String tweetMessage) {
		Status tweetedMessage = null;
		try {
			tweetedMessage = twitter.updateStatus(tweetMessage);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (tweetedMessage != null) {
			tweetText.setText(tweetedMessage.getText());
			
		} else
			tweetText.setText("Tweet Not done !");

	}
	public void readStream(int mode) {
		Paging paging = new Paging();
		paging.setPage(1);
		paging.setCount(18);
		List<Status> statusList = null;
		try {
			switch (mode) {
				case 0 :
					statusList = twitter.getHomeTimeline(paging);
					break;
				case 1 :
					statusList = twitter.search(new Query("#" + filterText.getText().toString())).getTweets();
					break;
				case 2 :
					statusList = twitter.getUserTimeline(filterText.getText().toString());
					break;

				default :
					break;
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		Toast.makeText(getBaseContext(), statusList.size() + " tweets read", 0).show();
		// Populating list
		ArrayList<String> tweetMessages = new ArrayList<String>();
		for (Status status : statusList) {
			tweetMessages.add(status.getText());
		}
		streamList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tweetMessages));

	}

}
