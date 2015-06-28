var Engine = famous.core.Engine;
var Modifier = famous.core.Modifier;
var Transform = famous.core.Transform;
var Surface = famous.core.Surface;
var StateModifier = famous.modifiers.StateModifier;
var Transitionable = famous.transitions.Transitionable;
var SpringTransition =famous.transitions.SpringTransition;
 var SequentialLayout = famous.views.SequentialLayout;
 
 
Transitionable.registerMethod('spring', SpringTransition);

var mainContext = Engine.createContext();

var originModifierUserSelection = new StateModifier({
  origin: [0, 0],
  align: [0, 0]
});

var surface = new Surface({
	content:'<h1>ToDO Famo.us</h1>',
    size: [, 80],
    properties: {
        backgroundColor: '#0000ff',
		textAlign:"center",
		color:'white'
    }
});

var surfaceProjectBio = new Surface({
	content:'<div id="projectBioContainer" class="summaryTable">'
					+'<table>'
					+'<tbody id="projectDataTable">'
					+'	<tr>'
					+'		<td><b>Project Name:</b></td>'					
					+'		<td> Todo Demo</td>'
					+'	</tr>'
					+'	<tr>'
					+'		<td><b>Collaborators</b></td>'
					+'		<td> Peter, Eric, Sam</td>'
					+'	</tr>'
					+'	<tr>'
					+'		<td><b>Last Update:</b></td>'
					+'		<td id="LastUpdateTimeStamp"> </td>'
					+'	</tr>'
					+'	</tbody>'
					+'</table>'
					+'<button id="btnToggleTaskList" class="btnToogleTasks" onclick="toggleListVisibility(1)">ToDo(0/0)</button>'
					+'</div>',
    size: [, 80],
    properties: {
        backgroundColor: '#0000ff',
		textAlign:"center",
		color:'white'
    }
});

var spring = {
  method: 'spring',
  period: 1000,
  dampingRatio: 0.3
};

var userSelectionSurface = new Surface({
	content:'<div id="userSelectionContainer" class="userselectionStyle">'
			+	'<!-- select user  -->'
			+	'<select id="selectUser">'
			+	'<option value="0">Select User</option>'
			+	'<option value="Peter">Peter</option>'
			+	'<option value="Eric">Eric</option>'
			+	'<option value="Sam">Sam</option></select>'
			+	'<button id="submitUserSelection" onclick="markUserSelected()">Submit</button></div> ',
	size: [,50 ],
    properties: {
        
		textAlign:"center",
		color:'white',
		padding:'10px'
    }
	
});

mainContext.add(surface);
mainContext.add(originModifierUserSelection).add(userSelectionSurface);
originModifierUserSelection.setTransform(
  Transform.translate(0, 300, 0), spring
);


/**
global variables...
**/

var CURRENT_USER_ =0;
var TOTAL_TASKS =0;
var COMPLETED_TASKS =0;
var CURRENT_SELECTED_TASK_ID="";


PUBNUB_demo.subscribe({
    channel: 'demo_tutorial',
    message: function(message){
		if(message['action_type']==='new'){
		
				var newTaskRow = '<tr style="margin:10px; border-bottom:1pt solid black;" onclick="modifyTask(\''
				+message['task_id']+'\')" id='+message['task_id']+'>'
				+'<td style="width:60% ; ; height:40px; padding:10px">'
				+'<div>'+message["task_description"]+'</div>'
				+'<div>'+message["timestamp"]+'</div>'
				+'</td>'
				+'<td style="width:40%;  height:40px; padding:10px; float:right">'
				+'<div>('+message["owner"]+')</div>'
				+'<div> <input type="checkbox" '+message["is_checked"]+'  disabled="true">Done</input></div>'
				+'<div style="display:none" id=dump_'+message['task_id']+'>'+JSON.stringify(message)+'</div>'
				+'</td></tr>';
			
				//tasksLists 
				TOTAL_TASKS++;
				if(message["is_checked"]==="checked")
						COMPLETED_TASKS++;
				document.getElementById("tasksLists").innerHTML += newTaskRow;	
				document.getElementById("LastUpdateTimeStamp").innerHTML = message["timestamp"];	
				document.getElementById("btnToggleTaskList").innerHTML = "To Do ("+COMPLETED_TASKS+"/"+TOTAL_TASKS+")";	
				
		}if(message['action_type']==='modify'){
			
			
			var modifiedcontent=+'<td style="width:60% ; ; height:40px; padding:10px">'
				+'<div>'+message["task_description"]+'</div>'
				+'<div>'+message["timestamp"]+'</div>'
				+'</td>'
				+'<td style="width:40%;  height:40px; padding:10px; float:right">'
				+'<div>('+message["owner"]+')</div>'
				+'<div> <input type="checkbox" '+message["is_checked"]+'  disabled="true">Done</input></div>'
				+'<div style="display:none" id=dump_'+message['task_id']+'>'+JSON.stringify(message)+'</div>'
				+'</td>';
				
				document.getElementById(message['task_id']).innerHTML = modifiedcontent;
				hideDialog();
		}
	
	}
});


// on page load, selecting a user is mandatory
function markUserSelected(){
	if(document.getElementById('selectUser').value==0){
		alert("please select a user");
	}else{
		CURRENT_USER_=document.getElementById('selectUser').value;
		//
		document.getElementById('projectBioContainer').style.display='block';
	}
}


function performAddNewTaskAction(){
document.getElementById('addNewTaskDiv').style.display='block';
document.getElementById('fade').style.display='block';
document.getElementById('taskDescription').style.pointerEvents = 'auto';
document.getElementById('selectedOwner').style.pointerEvents = 'auto';
document.getElementById('addTaskButton').style.pointerEvents = 'auto'; 
	document.getElementById("taskDescription").value="";
	document.getElementById("newTaskCheckStatus").checked = true;
	document.getElementById('selectedOwner').value="0";
	document.getElementById("actionTypeTitle").innerHTML="Add new ";
	document.getElementById("addTaskButton").innerHTML="Add";
	
}



// add a new task to the listStyleType
function addNewTask(){
		 
if(document.getElementById("addTaskButton").innerHTML==="Add"){
	document.getElementById('addNewTaskDiv').style.display='none';
	document.getElementById('fade').style.display='none'
	var createdBy=CURRENT_USER_;
	var currentTime=new Date();
	var milliseconds = (new Date).getTime();
	var isChecked = "";
	var taskDescription  = document.getElementById("taskDescription").value ;
	if(document.getElementById("newTaskCheckStatus").checked)
	var isChecked = "checked";
	var ownerIs = document.getElementById("selectedOwner").value;
	
												
												
	PUBNUB_demo.publish({
    channel: 'demo_tutorial',
    message: {"created_by":CURRENT_USER_,
					"is_checked":isChecked,
					"timestamp":currentTime,
					"task_description":taskDescription,
					"task_id":milliseconds+"_"+CURRENT_USER_+"_"+ownerIs,
					"action_type":"new",
					"owner":ownerIs}
});	
}else{
	var createdBy=CURRENT_USER_;
	var currentTime=new Date();
	var milliseconds = (new Date).getTime();
	var isChecked = "";
	var taskDescription  = document.getElementById("taskDescription").value ;
	if(document.getElementById("newTaskCheckStatus").checked)
	var isChecked = "checked";
	var ownerIs = document.getElementById("selectedOwner").value;
	
	// send packet to pubnub
	PUBNUB_demo.publish({
    channel: 'demo_tutorial',
    message: {"created_by":CURRENT_USER_,
					"is_checked":isChecked,
					"timestamp":currentTime,
					"task_description":taskDescription,
					"task_id":CURRENT_SELECTED_TASK_ID,
					"action_type":"modify",
					"owner":ownerIs}
});			
}
											

									
}




// showModification Dialog

function modifyTask(taskID){
	var taskData = document.getElementById("dump_"+taskID).innerHTML;
	var parsedData = JSON.parse(taskData);
	
	alert("will modify");
	document.getElementById('addNewTaskDiv').style.display='block';
	document.getElementById('fade').style.display='block';
	
	document.getElementById("actionTypeTitle").innerHTML="Modify";
	document.getElementById("addTaskButton").innerHTML="Modify";
	document.getElementById("taskDescription").value=parsedData['task_description'];
	if(parsedData['is_checked']==="checked"){
		document.getElementById("newTaskCheckStatus").checked = true;
	}else{
		document.getElementById("newTaskCheckStatus").checked = false;
	}
	document.getElementById('selectedOwner').value=parsedData['owner'];
	if(parsedData['created_by']===CURRENT_USER_){	
	document.getElementById('taskDescription').style.pointerEvents = 'auto';
	document.getElementById('selectedOwner').style.pointerEvents = 'auto';
	document.getElementById('addTaskButton').style.pointerEvents = 'auto'; 

	}else{
		document.getElementById('taskDescription').style.pointerEvents = 'none';
		document.getElementById('selectedOwner').style.pointerEvents = 'none';
		document.getElementById('addTaskButton').style.pointerEvents = 'none'; 
		document.getElementById("addNewTaskDiv").disabled = true;
	}
	
	CURRENT_SELECTED_TASK_ID=taskID;
				
}


// hide the add/modify task dialog

function hideDialog(){
	document.getElementById('addNewTaskDiv').style.display='none';
	document.getElementById('fade').style.display='none';
}

function toggleListVisibility(mode){
	if(mode==1){
	document.getElementById('taskListings').style.display='block';
	document.getElementById('btnToggleTaskList').style.display='none';
	}else{
		document.getElementById('taskListings').style.display='none';
	document.getElementById('btnToggleTaskList').style.display='block';
	}
}
