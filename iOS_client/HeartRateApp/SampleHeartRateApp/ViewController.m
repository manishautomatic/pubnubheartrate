//
//  ViewController.m
//  SampleHeartRateApp
//
//  Created by Gurpreet Singh on 6/06/15.
//  Copyright (c) 2015 Pubnub. All rights reserved.
//

#import "ViewController.h"
#import <AVFoundation/AVFoundation.h>
#import "PulseDetector.h"
#import "Filter.h"
#import "AppDelegate.h"
#import <QuartzCore/QuartzCore.h>


typedef NS_ENUM(NSUInteger, CURRENT_STATE) {
    STATE_PAUSED,
    STATE_SAMPLING
};

#define MIN_FRAMES_FOR_FILTER_TO_SETTLE 10

@interface ViewController ()<AVCaptureVideoDataOutputSampleBufferDelegate>

@property(nonatomic, strong) AVCaptureSession *session;
@property(nonatomic, strong) AVCaptureDevice *camera;
@property(nonatomic, strong) PulseDetector *pulseDetector;
@property(nonatomic, strong) Filter *filter;
@property(nonatomic, assign) CURRENT_STATE currentState;
@property(nonatomic, assign) int validFrameCounter;
@property(nonatomic, strong) IBOutlet UILabel *pulseRate;
@property(nonatomic, strong) IBOutlet UILabel *validFrames;
@property (weak, nonatomic) IBOutlet UIButton *StartButton;


- (IBAction)StartBtnAction:(id)sender;

@end

@implementation ViewController
{
    BOOL    TimerBool;
    NSTimer   *timer;
    BOOL PubNubBool;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    _backButton.hidden=YES;
    
    self.filter=[[Filter alloc] init];
    self.pulseDetector=[[PulseDetector alloc] init];
    _StartButton.hidden=NO;
    
    UIView *paddingView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 20)];
    _doctorId_txtfld.leftView = paddingView;
    _doctorId_txtfld.leftViewMode = UITextFieldViewModeAlways;

    _doctorId_btn.layer.cornerRadius = 5; // this value vary as per your desire
    _doctorId_btn.clipsToBounds = YES;
    
    _doctorId_txtfld.layer.cornerRadius=5;
    _doctorId_txtfld.clipsToBounds =YES;

    _StartButton.layer.cornerRadius=5;
    _StartButton.clipsToBounds =YES;

}

-(void) viewDidAppear:(BOOL)animated {
    
    [super viewDidAppear:animated];
    [self resume];
}

-(void) viewWillDisappear:(BOOL)animated {
    
    [super viewWillDisappear:animated];
    [self pause];
}

- (IBAction)StartBtnAction:(id)sender {
    
    
   // _StartButton.hidden=YES;
    
//            self.pulseRate.alpha = 0;
//            [UIView animateWithDuration:1.5 delay:0.5 options:UIViewAnimationOptionRepeat | UIViewAnimationOptionAutoreverse animations:^{
//                self.pulseRate.alpha = 1;
//            } completion:nil];

    
    
    if ([_StartButton.currentTitle isEqualToString:@"Start"]) {
       
        [_StartButton setTitle:@"Stop" forState:UIControlStateNormal];
        [self startCameraCapture];PubNubBool=NO;

    }else{
        
        [_StartButton setTitle:@"Start" forState:UIControlStateNormal];
        [timer invalidate];
        [self stopCameraCapture];
        self.pulseRate.text=@"Please start reading";

    
    }
    

    
}


// start capturing frames
-(void) startCameraCapture {
    
//    [NSTimer scheduledTimerWithTimeInterval:0.5 target:self selector:@selector(BlinkingMethod) userInfo:nil repeats:YES];

    timer = [NSTimer scheduledTimerWithTimeInterval: 0.5
                                             target: self
                                           selector:@selector(BlinkingMethod)
                                           userInfo: nil repeats:YES];

    
    // Create the AVCapture Session
    self.session = [[AVCaptureSession alloc] init];
    
    // Get the default camera device
    self.camera = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];

    // switch on torch mode - can't detect the pulse without it
    if([self.camera isTorchModeSupported:AVCaptureTorchModeOn]) {
        [self.camera lockForConfiguration:nil];
        self.camera.torchMode=AVCaptureTorchModeOn;
        [self.camera unlockForConfiguration];
    }
    
    // Create a AVCaptureInput with the camera device
    NSError *error=nil;
    
    AVCaptureInput* cameraInput = [[AVCaptureDeviceInput alloc] initWithDevice:self.camera error:&error];
    if (cameraInput == nil) {
        NSLog(@"Error to create camera capture:%@",error);
    }
    
    // Set the output
    AVCaptureVideoDataOutput* videoOutput = [
                                             [AVCaptureVideoDataOutput alloc] init];
    
    // create a queue to run the capture on
    dispatch_queue_t captureQueue= dispatch_queue_create("captureQueue", NULL);
    
    // setup ourself up as the capture delegate
    [videoOutput setSampleBufferDelegate:self queue:captureQueue];
    
    // configure the pixel format
    videoOutput.videoSettings = [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithUnsignedInt:kCVPixelFormatType_32BGRA], (id)kCVPixelBufferPixelFormatTypeKey, nil];
    
    // set the minimum acceptable frame rate to 10 fps
    videoOutput.minFrameDuration=CMTimeMake(1, 10);
    
    // and the size of the frames we want - we'll use the smallest frame size available
    [self.session setSessionPreset:AVCaptureSessionPresetLow];
    
    // Add the input and output
    [self.session addInput:cameraInput];
    [self.session addOutput:videoOutput];
    
    // Start the session
    [self.session startRunning];
    
    // we're now sampling from the camera
    self.currentState=STATE_SAMPLING;
    
    // stop the app from sleeping
    [UIApplication sharedApplication].idleTimerDisabled = YES;

    // update our UI on a timer every 0.1 seconds
    [NSTimer scheduledTimerWithTimeInterval:0.1 target:self selector:@selector(update) userInfo:nil repeats:YES];
    

}

-(void) stopCameraCapture {
    
    [self.session stopRunning];
    self.session=nil;
}

#pragma mark Pause and Resume of pulse detection
-(void) pause {
    
    if(self.currentState==STATE_PAUSED) return;
    
    // switch off the torch
    if([self.camera isTorchModeSupported:AVCaptureTorchModeOn]) {
        [self.camera lockForConfiguration:nil];
        self.camera.torchMode=AVCaptureTorchModeOff;
        [self.camera unlockForConfiguration];
    }
    self.currentState=STATE_PAUSED;
    // let the application go to sleep if the phone is idle
    [UIApplication sharedApplication].idleTimerDisabled = NO;
}

-(void) resume {
    
    if(self.currentState!=STATE_PAUSED) return;
    
    // switch on the torch
    if([self.camera isTorchModeSupported:AVCaptureTorchModeOn]) {
        [self.camera lockForConfiguration:nil];
         [self.camera unlockForConfiguration];
    }
    self.currentState=STATE_SAMPLING;
    // stop the app from sleeping
    [UIApplication sharedApplication].idleTimerDisabled = YES;
    
}

// r,g,b values are from 0 to 1 // h = [0,360], s = [0,1], v = [0,1]
//	if s == 0, then h = -1 (undefined)
void RGBtoHSV( float r, float g, float b, float *h, float *s, float *v ) {
    
    float min, max, delta;
    min = MIN( r, MIN(g, b ));
    max = MAX( r, MAX(g, b ));
    *v = max;
    delta = max - min;
    if( max != 0 )
        *s = delta / max;
    else {
        // r = g = b = 0
        *s = 0;
        *h = -1;
        return;
    }
    if( r == max )
        *h = ( g - b ) / delta;
    else if( g == max )
        *h=2+(b-r)/delta;
    else
        *h=4+(r-g)/delta;
    *h *= 60;
    if( *h < 0 )
        *h += 360;

}


// process the frame of video
- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer fromConnection:(AVCaptureConnection *)connection {
    

    
    // if we're paused don't do anything
    if(self.currentState==STATE_PAUSED) {
        // reset our frame counter
        self.validFrameCounter=0;
        return;
    }
    
    // this is the image buffer
    CVImageBufferRef cvimgRef = CMSampleBufferGetImageBuffer(sampleBuffer);
    // Lock the image buffer
    CVPixelBufferLockBaseAddress(cvimgRef,0);
    // access the data
    size_t width=CVPixelBufferGetWidth(cvimgRef);
    size_t height=CVPixelBufferGetHeight(cvimgRef);
    // get the raw image bytes
    uint8_t *buf=(uint8_t *) CVPixelBufferGetBaseAddress(cvimgRef);
    size_t bprow=CVPixelBufferGetBytesPerRow(cvimgRef);
    // and pull out the average rgb value of the frame
    float r=0,g=0,b=0;
   
    for(int y=0; y<height; y++) {
        for(int x=0; x<width*4; x+=4) {
            b+=buf[x];
            g+=buf[x+1];
            r+=buf[x+2];
        }
        buf+=bprow;
    }
    r/=255*(float) (width*height);
    g/=255*(float) (width*height);
    b/=255*(float) (width*height);
    
    // convert from rgb to hsv colourspace
    float h,s,v;
    RGBtoHSV(r, g, b, &h, &s, &v);
    // do a sanity check to see if a finger is placed over the camera
    if(s>0.5 && v>0.5) {
        
        
        NSLog(@"RatePulse: %@",self.pulseRate.text);
        
        // increment the valid frame count
        self.validFrameCounter++;
        // filter the hue value - the filter is a simple band pass filter that removes any DC component and any high frequency noise
        float filtered=[self.filter processValue:h];
        // have we collected enough frames for the filter to settle?
        if(self.validFrameCounter > MIN_FRAMES_FOR_FILTER_TO_SETTLE) {
            // add the new value to the pulse detector
            [self.pulseDetector addNewValue:filtered atTime:CACurrentMediaTime()];
        }
        
        TimerBool=YES;

        
    } else {
        
        
         TimerBool=NO;


        
        self.validFrameCounter = 0;
        // clear the pulse detector - we only really need to do this once, just before we start adding valid samples
        [self.pulseDetector reset];
    }

}

-(void)BlinkingMethod{  
    
    if (!TimerBool) { [_heartImage setImage:[UIImage imageNamed:@"Black1_heart.png"]];
        self.pulseRate.text=@"PLACE FINGER ON CAMERA LENS";
        return; }
    
    if ([self.pulseRate.text isEqualToString:@"DETECTING PULSE ...         "]||[self.pulseRate.text isEqualToString:@"PLACE FINGER ON CAMERA LENS"]) {
        self.pulseRate.text=@"DETECTING PULSE ...         ";
    }
    


    UIImage *picture = [UIImage imageNamed:@"Black1_heart.png"];

    if ([_heartImage.image isEqual:picture] ) {
        
        [_heartImage setImage:[UIImage imageNamed:@"Red_heart.png"]];
    }else{
    
      [_heartImage setImage:[UIImage imageNamed:@"Black1_heart.png"]];
    }
}

-(void) update {
    
    self.validFrames.text = [NSString stringWithFormat:@"Captured Frames: %d%%", MIN(100, (100 * self.validFrameCounter)/MIN_FRAMES_FOR_FILTER_TO_SETTLE)];
 

    
    // if we're paused then there's nothing to do
    if(self.currentState==STATE_PAUSED) return;
    
    // get the average period of the pulse rate from the pulse detector
    float avePeriod=[self.pulseDetector getAverage];
    if(avePeriod==INVALID_PULSE_PERIOD) {
        // no value available
      //  self.pulseRate.text=@"Reading...";


    } else {
        // got a value so show it

        float pulse=60.0/avePeriod;
        self.pulseRate.text=[NSString stringWithFormat:@"%0.0f", pulse];
        
        if (!PubNubBool) {
            [self PubnubMethod:self.pulseRate.text];
        }
    }
    
    
}


-(void)PubnubMethod:(NSString*)PulseRate {
    
    PubNubBool=YES;
    [self stopCameraCapture];

    [_StartButton setTitle:@"Start" forState:UIControlStateNormal];
    [timer invalidate];

    UIAlertView *alert=[[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"%@ bpm",PulseRate] message:@"Send Reading to Doctor" delegate:self cancelButtonTitle:@"Ok" otherButtonTitles:@"Cancel", nil];
    alert.tag=123;
    [alert show];

    
}



- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{

    
    if (alertView.tag==123) {

        if (buttonIndex==0) {
            
            
            AppDelegate *myDelegate=( AppDelegate* )[UIApplication sharedApplication].delegate;
            [myDelegate PublishOnPubNub:self.pulseRate.text docId:_doctorId_txtfld.text];
            [myDelegate ShowLoader];
            
            self.pulseRate.text=@"PLACE FINGER ON CAMERA LENS";

            [self DisplayDocIdView];
            _backButton.hidden=YES;

        }
        else{
            
            self.pulseRate.text=@"PLACE FINGER ON CAMERA LENS";
            [self DisplayDocIdView];
            _backButton.hidden=YES;

            
        }
    }
    
}

-(void)DisplayDocIdView{
    
    _doctorId_txtfld.text=@"";
    
    [UIView animateWithDuration:0.0
                     animations:^{
                         _doctorId_view.alpha = 0;
                     }
                     completion:^(BOOL finished){
                         [UIView animateWithDuration:0.0
                                          animations:^{
                                              _doctorId_view.alpha = 1.0;
                                          }
                                          completion:^(BOOL finished){
                                              [_doctorId_view setHidden:NO];
                                              
                                          }];
                     }];

}


- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    
    UITouch * touch = [touches anyObject];
    if(touch.phase == UITouchPhaseBegan) {
        [self.view endEditing:YES];
    }
}

- (IBAction)SaveDoctId_btn:(id)sender {
    
    if ([_doctorId_txtfld.text length] == 0  ) {
        
        UIAlertView *alert=[[UIAlertView alloc] initWithTitle:@"PubNub" message:@"Please enter DoctorId" delegate:nil cancelButtonTitle:@"Ok" otherButtonTitles:nil, nil];
        
        [alert show];
    }
    else{
    
        [self.view endEditing:YES];
        _backButton.hidden=NO;

        
        self.pulseRate.text=@"PLACE FINGER ON CAMERA LENS";
        [_heartImage setImage:[UIImage imageNamed:@"Black1_heart.png"]];


        [UIView animateWithDuration:1.0
                     animations:^{
                         _doctorId_view.alpha = 0;
                     }
                     completion:^(BOOL finished){
                         [UIView animateWithDuration:1.0
                                          animations:^{
                                              _doctorId_view.alpha = 0;
                                          }
                                          completion:^(BOOL finished){
                                              [_doctorId_view setHidden:YES];

                                          }];
                     }];
    }

}

- (IBAction)BackAction:(id)sender {
    
    [_StartButton setTitle:@"Start" forState:UIControlStateNormal];
    [timer invalidate];
    [self stopCameraCapture];
    
    self.pulseRate.text=@"PLACE FINGER ON CAMERA LENS";
    
    [self DisplayDocIdView];
    _backButton.hidden=YES;

    
}


@end
