
ALERT_COUNT=0;
DOCTOR_ID="";



function sendasap(){
	
	PUBNUB_demo.publish({                                  //30.660009, 76.860564   
             channel : "heartbeat_alert",
             message : "{\"ResNo\":\"091999800000\",\"lat\":\"30.660009\",\"lon\":\"76.860564\"}",
             callback: function(m){ console.log(m) }
        });
}

function getChannelHistory(){
	
	PUBNUB_demo.history({
     channel: DOCTOR_ID+'heartbeat_alert',
     callback: function(m){
		 
		 console.log(JSON.stringify(m));
		 var history = JSON.stringify(m).split('],')[0].split('[[')[1];
		 history = "["+history+"]";
		 var formattedHistory="";
		 var currentItem=0;
		 var parsedArray=JSON.parse(history);
		 for(currentItem in parsedArray){
			 formattedHistory=formattedHistory+"&#13;&#10;"+parsedArray[currentItem];
		 }
			document.getElementById("historyContainer").innerHTML = formattedHistory;
		 
		 
		 }
	 
	 ,
     count: 100, // 100 is the default
     reverse: false // false is the default
 });
	
}


function clearHistoryView(){
	document.getElementById("historyContainer").innerHTML = "";
}

function getDoctorId(){
	var doctorId = document.getElementById("doctorID").value;
	if(doctorId){
		PUBNUB_demo.subscribe({
    channel: doctorId+'heartbeat_alert',
    message: function(message){
		 console.log(message);
		 console.log(new Date());
		DOCTOR_ID=doctorId;
		
		var payload= "<h3>"+'<br>'+message+"</h3>";
		document.getElementById("latestNotificationDiv").innerHTML = payload;
		ALERT_COUNT++;
		document.getElementById("alertCountbOX").innerHTML ="<h3>CRITICAL-TRIGGERS:: " +ALERT_COUNT+"</h3>";
	}
});
	}else{
			alert("please enter doctor id");
	}
	
}

