#Build and Install (iOS)

Here are the generic steps for building and installing the app on an iOS device.

1.  Download the code from from URL :- https://github.com/manishautomatic/pubnubheartrate/tree/master/iOS_client/HeartRateApp
2.  Open the code in latest Xcode  above 5.0 (Development environment)
3.  Set provisioning and certificates (Authenticate the user to install the build to device)

    a.  Get apple account.
    
    b.  Now create certificate and provisioning -

    c.  Add device’s UDID to Provisioning profile. (Get UDID with this link :  http://whatsmyudid.com/ )      

    d.  Install the certificate on the development Mac system.

    e.  Install the provisioning profile onto Xcode project (Set identifier).

    f.  Now run the project, and application will be installed to specific device 

#Usage
Here is how you can use this application for measuring and publishing your heart rate.

1. Open the mobile app and also the portal application
2. Enter a doctor's ID on the mobile app and tap on "Save Doctor Id" button. A doctor id is kind of a link to conect the patient to a specific doctor.
3. Enter the same doctor's id in the portal application and hit the submit button.
4. Place one finger of your hand holding the phone on the back camera in such a way that the tip of the finger presses against the camera and completely covers it.
5. With the other hand, tap on the "Start" button on the app to begin heart rate sensing process.
6. You will notice an animating heart symbol on the app screen with a message "Detecting Pulse". 
7. Hold your finger on the camera for about 10 seconds till you see a popup box displaying the current reading sensed by the app.
8. At the end the app will send the current reading to portal
9. You can check the last reading received at the portal and can also fetch historical readings for the specific doctor's id
