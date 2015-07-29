#Realtime Heartrate monitoring on Android using PubNub

##Introduction
This is a prototype healthcare application consisting of a mobile app and a healthcare portal. The mobile app allows users to measure their heart rate using the phone's inbuilt camera and send the readings to the portal application which can be accessed by a doctor in realtime. The communication between the mobile app and portal is achieved via PubNub's realtime data stream network.

##Installation
Install the app by downloading and apk file from the release page.
Run the portal appliciation under <github.io> link

##Usage
Here is how you can use this application for measuring and publishing your heart rate.

1. Open the mobile app and also the portal application
2. Enter a doctor's ID on the mobile app and tap on submit button. A doctor id is kind of a link to conect the patient to a specific doctor.
3. Enter the same doctor's id in the portal application and hit the submit button.
4. Place one finger of your hand holding the phone on the back camera in such a way that the tip of the finger presses against the camera and completely covers it.
5. With the other hand, tap on the red PubNub icon on the app to begin heart rate sensing process.
6. You will notice that the camera flash will be turned on and a countdown will appear at the bottom of the screen starting from 10 secs.
7. Hold your finger on the camera till the countdown ends and the phone vibrates. The app will also alert you for taking another measurement which can either be accepted or rejected. 
8. At the end the app will send the current reading to portal
9. You can check the last reading received at the portal and can also fetch historical readings for the specific doctpr's id

##ToDo
1. The application does not differenciate between users sending the heart rate reading. All users sending readings to a specific doctor's id are treated as the same user.
2. The portal application does not support login functionality.
3. The accuracy of heartrate reading taken by the app may tend to be grossly wayward if fingers are not placed properly.
4. The app does not have a way to detect if the finger is properly placed at the camera and it is not intelligent enough to detect the absense of finger either. It assumes that the image taken by the camera is that of the finger pressing against it. 
