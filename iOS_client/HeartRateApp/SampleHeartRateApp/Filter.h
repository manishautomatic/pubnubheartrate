//
//  Filter.h
//  HeartRate
//
//  Created by Gurpreet Singh on 31/10/2013.
//  Copyright (c) 2015 Pubnub. All rights reserved.
//


#import <Foundation/Foundation.h>

#define NZEROS 10
#define NPOLES 10

@interface Filter : NSObject {
	float xv[NZEROS+1], yv[NPOLES+1];
}

-(float) processValue:(float) value;

@end
