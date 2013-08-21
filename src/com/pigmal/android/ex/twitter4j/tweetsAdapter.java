package com.pigmal.android.ex.twitter4j;

import java.util.List;
import java.util.regex.Pattern;

import twitter4j.Status;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class tweetsAdapter extends ArrayAdapter<String> {
	LayoutInflater layoutInflater;
	List<Status> tweetMessages;
	TextView tweetMessageText;
	public tweetsAdapter(Context context, int resource, LayoutInflater inflater, List<Status> statusList) {
		super(context, resource);
		layoutInflater = inflater;
		this.tweetMessages = statusList;

	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.tweet_cell, null);
			tweetMessageText = (TextView) convertView.findViewById(R.id.tweetTextview);
			tweetMessageText.setText(Html.fromHtml(tweetMessages.get(position).getText()));

			//tweetMessageText.setAutoLinkMask(Linkify.WEB_URLS);
			// add links to the tweets
			if (tweetMessages.get(position).getURLEntities().length != 0) {
			//	Pattern pattern = Pattern.compile("https");
				//Linkify.addLinks(tweetMessageText, pattern, tweetMessages.get(position).getURLEntities()[0].getURL());
			//	tweetMessageText.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
		return convertView;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return tweetMessages.size();
	}

}
