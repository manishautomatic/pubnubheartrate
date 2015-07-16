//
//  AppDelegate.h
//  SampleHeartRateApp
//
//  Created by Gurpreet Singh on 6/06/15.
//  Copyright (c) 2015 Pubnub. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <PubNub/PubNub.h>


@interface AppDelegate : UIResponder <UIApplicationDelegate,PNObjectEventListener>

@property (strong, nonatomic) UIWindow *window;

// Stores reference on PubNub client to make sure what it won't be released.
@property (nonatomic) PubNub *client;

-(void)PublishOnPubNub :(NSString*)PulseRate docId:(NSString*)Docid;

-(void)ShowLoader;
-(void)stopLoader;


@end

