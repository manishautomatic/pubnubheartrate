
ALERT_COUNT=0;

PUBNUB_demo.subscribe({
    channel: 'heartbeat_alert',
    message: function(message){
		 console.log(message);
		 console.log(new Date());
		
		var date = new Date();
		var payload= "<h3>"+date+'<br>'+message+"</h3>";
		document.getElementById("latestNotificationDiv").innerHTML = payload;
		ALERT_COUNT++;
		document.getElementById("alertCountbOX").innerHTML ="<h3>CRITICAL-TRIGGERS:: " +ALERT_COUNT+"</h3>";
	}
});


function sendasap(){
	
	PUBNUB_demo.publish({                                  //30.660009, 76.860564   
             channel : "heartbeat_alert",
             message : "{\"ResNo\":\"091999800000\",\"lat\":\"30.660009\",\"lon\":\"76.860564\"}",
             callback: function(m){ console.log(m) }
        });
}

function getChannelHistory(){
	
	PUBNUB_demo.history({
     channel: 'heartbeat_alert',
     callback: function(m){
		 
		 console.log(JSON.stringify(m));
		 document.getElementById("historyContainer").innerHTML = m;
		 
		 
		 }
	 
	 ,
     count: 100, // 100 is the default
     reverse: false // false is the default
 });
	
}

