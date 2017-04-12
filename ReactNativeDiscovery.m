//
//  DiscoveryReact.m
//  DiscoveryReact

#import "ReactNativeDiscovery.h"

#import <React/RCTBridge.h>
#import <React/RCTConvert.h>
#import <React/RCTEventDispatcher.h>

#import "Discovery.h"


@interface ReactNativeDiscovery()

@property (strong, nonatomic) id bleStateObserver;
@property (strong, nonatomic) NSMutableDictionary *discoveryDict;

@end

@implementation ReactNativeDiscovery

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;

#pragma mark Initialization




/**
 * Initialize the Discovery object with a UUID specific to your app, and a username specific to your device.
 */
RCT_REMAP_METHOD(initialize, initialize:(NSString *)uuidString username:(NSString *)username resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (self.bleStateObserver == nil) {
        self.bleStateObserver = [[NSNotificationCenter defaultCenter] addObserverForName:kBluetoothStateNotificationKey object:nil queue:nil usingBlock:^(NSNotification * _Nonnull note) {

            NSInteger centralState = [(NSNumber *)note.userInfo[kBluetoothCentralStateKey] integerValue];
            BOOL isOn = centralState == CBCentralManagerStatePoweredOn;
            NSDictionary *event = @{ @"isOn" : @(isOn)};
            [self.bridge.eventDispatcher sendDeviceEventWithName:@"bleStateChanged" body:event];
        }];
    }

    if (self.discoveryDict == nil) {
        self.discoveryDict = [NSMutableDictionary dictionary];
    }

    Discovery *discovery = [self.discoveryDict objectForKey:uuidString];
    if (discovery != nil) {
        [discovery setShouldDiscover: NO];
        [discovery setShouldAdvertise: NO];
        [self.discoveryDict removeObjectForKey:uuidString];
    }


    discovery = [[Discovery alloc] initWithUUID: [CBUUID UUIDWithString:uuidString]
                                       username: username
                                    startOption:DIStartNone
                                     usersBlock:^(NSArray *users, BOOL usersChanged) {
                                         [self discovery:uuidString discoveredUsers:users didChange:usersChanged];
                                     }];

    [self.discoveryDict setObject:discovery forKey:uuidString];
    resolve(uuidString);
}

/**
 * run on the main queue otherwise discovery timers dont work.
 */
- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}


-(void)discovery:(NSString *)uuidString discoveredUsers:(NSArray *)users didChange:(BOOL) usersChanged {
    NSMutableArray *array = [NSMutableArray array];
    for (BLEUser *user in users) {
        [array addObject:[self convertBLEUserToDict:user]];
    }

    NSDictionary *event = @{
                            @"uuid": uuidString,
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


/**
 * Returns the user user from our user dictionary according to its peripheralId.
 */
RCT_REMAP_METHOD(userWithPeripheralId, userWithPeripheralId:(NSString *)uuidString peripheralId:(NSString *)peripheralId resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    Discovery *discovery = [self.discoveryDict objectForKey:uuidString];
    if (discovery) {
        BLEUser *user = [discovery userWithPeripheralId:peripheralId];
        resolve(user ? [self convertBLEUserToDict:user] : @{});
    } else {
        reject(@"not_initialized", [NSString stringWithFormat:@"UUID %@ not initialized", uuidString], [NSError errorWithDomain:@"ReactNativeDiscovery" code:0 userInfo:nil]);
    }
}


/**
 * Changing these properties will start/stop advertising/discovery
 */
RCT_REMAP_METHOD(setShouldAdvertise, setShouldAdvertise:(NSString *)uuidString shouldAdvertise:(BOOL)shouldAdvertise resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    Discovery *discovery = [self.discoveryDict objectForKey:uuidString];
    if (discovery) {
        [discovery setShouldAdvertise:shouldAdvertise];
        resolve(@YES);
    } else {
        reject(@"not_initialized", [NSString stringWithFormat:@"UUID %@ not initialized", uuidString], [NSError errorWithDomain:@"ReactNativeDiscovery" code:0 userInfo:nil]);
    }

}

RCT_REMAP_METHOD(setShouldDiscover, setShouldDiscover:(NSString *)uuidString shouldDiscover:(BOOL)shouldDiscover resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    Discovery *discovery = [self.discoveryDict objectForKey:uuidString];
    if (discovery) {
        [discovery setShouldDiscover:shouldDiscover];
        resolve(@YES);
    } else {
        reject(@"not_initialized", [NSString stringWithFormat:@"UUID %@ not initialized", uuidString], [NSError errorWithDomain:@"ReactNativeDiscovery" code:0 userInfo:nil]);
    }
}


/*
 * Discovery removes the users if can not re-see them after some amount of time, assuming the device-user is gone.
 * The default value is 3 seconds. You can set your own values.
 */
RCT_REMAP_METHOD(setUserTimeoutInterval, setUserTimeoutInterval:(NSString *)uuidString userTimeoutInterval:(int)userTimeoutInterval resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    Discovery *discovery = [self.discoveryDict objectForKey:uuidString];
    if (discovery) {
        [discovery setUserTimeoutInterval:userTimeoutInterval];
        resolve(@YES);
    } else {
        reject(@"not_initialized", [NSString stringWithFormat:@"UUID %@ not initialized", uuidString], [NSError errorWithDomain:@"ReactNativeDiscovery" code:0 userInfo:nil]);
    }
}

/*
 * Update interval is the interval that your usersBlock gets triggered.
 */
RCT_REMAP_METHOD(setUpdateInterval, setUpdateInterval:(NSString *)uuidString updateInterval:(int)updateInterval resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    Discovery *discovery = [self.discoveryDict objectForKey:uuidString];
    if (discovery) {
        [discovery setUpdateInterval:updateInterval];
        resolve(@YES);
    } else {
        reject(@"not_initialized", [NSString stringWithFormat:@"UUID %@ not initialized", uuidString], [NSError errorWithDomain:@"ReactNativeDiscovery" code:0 userInfo:nil]);
    }
}

/**
 * Set this to YES, if your app will disappear, or set to NO when it will appear.
 * You don't have to set YES when your app goes to background state, Discovery handles that.
 */
RCT_REMAP_METHOD(setPaused, setPaused:(NSString *)uuidString paused:(BOOL)paused resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    Discovery *discovery = [self.discoveryDict objectForKey:uuidString];
    if (discovery) {
        [discovery setPaused:paused];
        resolve(@YES);
    } else {
        reject(@"not_initialized", [NSString stringWithFormat:@"UUID %@ not initialized", uuidString], [NSError errorWithDomain:@"ReactNativeDiscovery" code:0 userInfo:nil]);
    }
}


@end
