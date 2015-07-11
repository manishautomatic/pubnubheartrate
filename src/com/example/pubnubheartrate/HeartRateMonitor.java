package com.example.pubnubheartrate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;



public class HeartRateMonitor extends Activity {

    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static View image = null;
    private static TextView text = null;
    private static String beatsPerMinuteValue="";
    private static WakeLock wakeLock = null;
    private final String PUBNUB_PUBLISH_KEY="pub-c-1b4f0648-a1e6-4aa1-9bae-aebadf76babe";
	private final String PUBNUB_SUBSCRIBE_KEY="sub-c-e9fadae6-f73a-11e4-af94-02ee2ddab7fe";
	private final String PUBNUB_DEFAULT_CHANNEL_NAME="demo";
	private static Pubnub pubnub;
	private static TextView mTxtVwStopWatch;
    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];
    private static String strSavedDoctorID=""; 
    private static Context parentReference = null;

    public static enum TYPE {
        GREEN, RED
    };

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }

    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;
    private static Vibrator v ;
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
         v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
         parentReference=this;
        strSavedDoctorID= HeartRateMonitor.this.getSharedPreferences("app_prefs", MODE_PRIVATE)
        				.getString("doc_id", "---");
        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        mTxtVwStopWatch=(TextView)findViewById(R.id.txtvwStopWatch);
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        image = findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        pubnub = new Pubnub(PUBNUB_PUBLISH_KEY, PUBNUB_SUBSCRIBE_KEY);
        prepareCountDownTimer();
        configurePubNubClient();
        pubnubSubscribe();
        
    }
    
    
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        camera = Camera.open();
        startTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        wakeLock.release();

        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        text.setText("---");
        camera = null;
    }

    private static PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!processing.compareAndSet(false, true)) return;

            int width = size.width;
            int height = size.height;

            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
            // Log.i(TAG, "imgAvg="+imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }

            int averageArrayAvg = 0;
            int averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i];
                    averageArrayCnt++;
                }
            }

            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
            TYPE newType = currentType;
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED;
                if (newType != currentType) {
                    beats++;
                    // Log.d(TAG, "BEAT!! beats="+beats);
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN;
            }

            if (averageIndex == averageArraySize) averageIndex = 0;
            averageArray[averageIndex] = imgAvg;
            averageIndex++;

            // Transitioned from one state to another to the same
            if (newType != currentType) {
                currentType = newType;
                image.postInvalidate();
            }

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d;
            if (totalTimeInSecs >= 10) {
                double bps = (beats / totalTimeInSecs);
                int dpm = (int) (bps * 60d);
                if (dpm < 30 || dpm > 180) {
                    startTime = System.currentTimeMillis();
                    beats = 0;
                    processing.set(false);
                    return;
                }

                // Log.d(TAG,
                // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);

                if (beatsIndex == beatsArraySize) beatsIndex = 0;
                beatsArray[beatsIndex] = dpm;
                beatsIndex++;

                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < beatsArray.length; i++) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                text.setText(String.valueOf(beatsAvg));
                beatsPerMinuteValue=String.valueOf(beatsAvg);
                makePhoneVibrate();
                dispatchPubNubEvent(String.valueOf(beatsAvg));
                showReadingCompleteDialog();
                startTime = System.currentTimeMillis();
                beats = 0;
            }
            processing.set(false);
        }
    };
    
    
    private static void makePhoneVibrate(){
    	 v.vibrate(500);
    }

    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }
    
    
    
    private static void prepareCountDownTimer(){
    	mTxtVwStopWatch.setText("---");
    	new CountDownTimer(10000, 1000) {

    	     public void onTick(long millisUntilFinished) {
    	    	 mTxtVwStopWatch.setText("seconds remaining: " + (millisUntilFinished) / 1000);
    	     }

    	     public void onFinish() {
    	    	 mTxtVwStopWatch.setText("done!");
    	     }
    	  }.start();
    }
    
    private static void dispatchPubNubEvent(String data){
    	pubnubPublish(data);
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
    
    
    private static void pubnubPublish(String message){
    	//Toast.makeText(getApplicationContext(), "publishing", Toast.LENGTH_LONG).show();
		Callback callback = new Callback() {
			   public void successCallback(String channel, Object response) {
			     Log.d("PUBNUB",response.toString());
			   }
			   public void errorCallback(String channel, PubnubError error) {
			   Log.d("PUBNUB",error.toString());
			   }
			 };
			 DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
			 String date = df.format(Calendar.getInstance().getTime());
			 pubnub.publish(strSavedDoctorID+"heartbeat_alert", "Heart beat alert at :: "+message+" for Test User @ "+date , callback);
	}

    
    
    private static void showReadingCompleteDialog(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(parentReference);
    	builder.setTitle("PubNub-HeartRate");
    	builder.setMessage("Reading taken Succesfully at- "+beatsPerMinuteValue+" beats per minute.")
    	   .setCancelable(false)
    	   .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
    	       public void onClick(DialogInterface dialog, int id) {
    	    	 ( (Activity) parentReference).finish();
    	       }
    	   })
    	   .setNegativeButton("Take Another", new DialogInterface.OnClickListener() {
    	       public void onClick(DialogInterface dialog, int id) {
    	    	   text.setText("---");
    	    	   prepareCountDownTimer();
    	            dialog.cancel();
    	       }
    	   });
    	AlertDialog alert = builder.create();
    	alert.show();
    }


}

