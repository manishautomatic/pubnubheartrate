//
//  ViewController.h
//  SampleHeartRateApp
//
//  Created by Gurpreet Singh on 6/06/15.
//  Copyright (c) 2015 Pubnub. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController

@property (weak, nonatomic) IBOutlet UIView *doctorId_view;
@property (weak, nonatomic) IBOutlet UIButton *doctorId_btn;
@property (weak, nonatomic) IBOutlet UITextField *doctorId_txtfld;
@property (strong , nonatomic) IBOutlet UIImageView *heartImage;
- (IBAction)SaveDoctId_btn:(id)sender;
- (IBAction)BackAction:(id)sender;
@property (weak, nonatomic) IBOutlet UIButton *backButton;

@end

