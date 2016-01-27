//
//  DiscoveryReact.m
//  DiscoveryReact

#import "ReactNativeDiscovery.h"

#import "RCTBridge.h"
#import "RCTConvert.h"
#import "RCTEventDispatcher.h"

#import "Discovery.h"


@interface ReactNativeDiscovery()

@property (strong, nonatomic) Discovery *discovery;

@end

@implementation ReactNativeDiscovery

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;

#pragma mark Initialization




/**
 * Initialize the Discovery object with a UUID specific to your app, and a username specific to your device.
 */
RCT_EXPORT_METHOD(initialize:(NSString *)uuidString username:(NSString *)username) {
    CBUUID *uuid = [CBUUID UUIDWithString:uuidString];
    self.discovery = [[Discovery alloc] initWithUUID: uuid
                                            username: username
                                         startOption:DIStartNone
                                          usersBlock:^(NSArray *users, BOOL usersChanged) {
                                              [self discoveredUsers:users didChange:usersChanged];
                                          }];

}

/**
 * run on the main queue otherwise discovery timers dont work.
 */
- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}




-(void)discoveredUsers:(NSArray *)users didChange:(BOOL) usersChanged {
    NSMutableArray *array = [NSMutableArray array];
    for (BLEUser *user in users) {
        [array addObject:[self convertBLEUserToDict:user]];
    }

    NSDictionary *event = @{
                            @"uuid": [self.discovery.uuid UUIDString],
                            @"users": array,
                            @"didChange": @(usersChanged)
                        };

    [self.bridge.eventDispatcher sendDeviceEventWithName:@"discoveredUsers" body:event];
}

-(NSDictionary *)convertBLEUserToDict:(BLEUser *)bleUser{

    NSDictionary *dict = @{
                           @"peripheralId":bleUser.peripheralId,
                           @"username":bleUser.username,
                           @"identified":@(bleUser.identified),
                           @"rssi":@(bleUser.rssi),
                           @"proximity":@(bleUser.proximity),
                           @"updateTime":@(bleUser.updateTime)
                        };

    return dict;
}


//commented because i dont know how to return values for exported methods
///**
// * Returns the user user from our user dictionary according to its peripheralId.
// */
//RCT_EXPORT_METHOD(userWithPeripheralId:(NSString *)peripheralId) {
//    BLEUser *user = [self.discovery userWithPeripheralId:peripheralId];
//    return [self convertBLEUserToDict:user];
//}


/**
 * Changing these properties will start/stop advertising/discovery
 */
RCT_EXPORT_METHOD(setShouldAdvertise:(BOOL)shouldAdvertise)
{
    [self.discovery setShouldAdvertise:shouldAdvertise];
}

RCT_EXPORT_METHOD(setShouldDiscover:(BOOL)shouldDiscover)
{
    [self.discovery setShouldDiscover:shouldDiscover];
}


/*
 * Discovery removes the users if can not re-see them after some amount of time, assuming the device-user is gone.
 * The default value is 3 seconds. You can set your own values.
 */
RCT_EXPORT_METHOD(setUserTimeoutInterval:(int)userTimeoutInterval)
{
    [self.discovery setUserTimeoutInterval:userTimeoutInterval];
}

/*
 * Update interval is the interval that your usersBlock gets triggered.
 */
RCT_EXPORT_METHOD(setUpdateInterval:(int)updateInterval)
{
    [self.discovery setUpdateInterval:updateInterval];
}

/**
 * Set this to YES, if your app will disappear, or set to NO when it will appear.
 * You don't have to set YES when your app goes to background state, Discovery handles that.
 */

RCT_EXPORT_METHOD(setPaused:(BOOL)paused)
{
    [self.discovery setPaused:paused];
}


@end
