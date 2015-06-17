package com.example.pubnubheartrate;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements OnClickListener {

	private final String PUBNUB_PUBLISH_KEY="pub-c-1b4f0648-a1e6-4aa1-9bae-aebadf76babe";
	private final String PUBNUB_SUBSCRIBE_KEY="sub-c-e9fadae6-f73a-11e4-af94-02ee2ddab7fe";
	private final String PUBNUB_DEFAULT_CHANNEL_NAME="demo";
	private Pubnub pubnub;
	private Button mBtnPublishAction;
	private ImageView mImgVwLaunchBeatMonitor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pubnub = new Pubnub(PUBNUB_PUBLISH_KEY, PUBNUB_SUBSCRIBE_KEY);
		setContentView(R.layout.activity_main);
		initializeLayout();
		configurePubNubClient();
		pubnubSubscribe();


	}


	private void initializeLayout() {
		mBtnPublishAction=(Button)findViewById(R.id.btnPublishAction);
		mBtnPublishAction.setOnClickListener(this);
		mImgVwLaunchBeatMonitor=(ImageView)findViewById(R.id.imgvwLaunchHeartRater);
		mImgVwLaunchBeatMonitor.setOnClickListener(this);


	}


	private void configurePubNubClient() {
		Callback callback = new Callback() {
			public void successCallback(String channel, Object response) {
				System.out.println(response.toString());
			}
			public void errorCallback(String channel, PubnubError error) {
				System.out.println(error.toString());
			}
		};
		pubnub.time(callback);



	}


	private void  pubnubSubscribe(){
		try {
			pubnub.subscribe(PUBNUB_DEFAULT_CHANNEL_NAME, new Callback() {

				@Override
				public void connectCallback(String channel, Object message) {
					System.out.println("SUBSCRIBE : CONNECT on channel:" + channel
							+ " : " + message.getClass() + " : "
							+ message.toString());
				}

				@Override
				public void disconnectCallback(String channel, Object message) {
					System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
							+ " : " + message.getClass() + " : "
							+ message.toString());
				}


				public void reconnectCallback(String channel, Object message) {
					System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
							+ " : " + message.getClass() + " : "
							+ message.toString());
				}

				@Override
				public void successCallback(String channel, Object message) {
					System.out.println("SUBSCRIBE : " + channel + " : "
							+ message.getClass() + " : " + message.toString());
				}

				@Override
				public void errorCallback(String channel, PubnubError error) {
					System.out.println("SUBSCRIBE : ERROR on channel " + channel
							+ " : " + error.toString());
				}

			}
					);
		} catch (PubnubException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void pubnubPublish(String message){
		Callback callback = new Callback() {
			   public void successCallback(String channel, Object response) {
			     Log.d("PUBNUB",response.toString());
			   }
			   public void errorCallback(String channel, PubnubError error) {
			   Log.d("PUBNUB",error.toString());
			   }
			 };
			 pubnub.publish("my_channel", "Free Gift, sign up now" , callback);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.btnPublishAction){
			Toast.makeText(MainActivity.this, "publish action", Toast.LENGTH_LONG).show();
		}if(v.getId()==R.id.imgvwLaunchHeartRater){
			startActivity(new Intent(MainActivity.this,HeartRateMonitor.class));
		}
		
	}
}
