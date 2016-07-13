# Discovery
Discover nearby devices using BLE.

React native implementation of https://github.com/omergul123/Discovery

(Android uses https://github.com/joshblour/discovery-android)

####Changes from version 0.0.x
Now supports discovery with multiple UUIDs. You need to intialize each one separately and specify the UUID when you perform actions.
i.e. `Discovery.setShouldAdvertise(true);` now becomes `Discovery.setShouldAdvertise("3E1180E5-222E-43E9-98B4-E6C0DD18E728", true);`

##What
Discovery is a very simple but useful library for discovering nearby devices with BLE(Bluetooth Low Energy) and for exchanging a value (kind of ID or username determined by you on the running app on peer device) regardless of whether the app on peer device works at foreground or background state.

####Example
```js
const {DeviceEventEmitter} = require('react-native');
const Discovery = require('react-native-discovery');
const myUUID = "3E1180E5-222E-43E9-98B4-E6C0DD18E728";

Discovery.initialize(
  myUUID,
  "SpacemanSpiff"
);
Discovery.setShouldAdvertise(myUUID, true);
Discovery.setShouldDiscover(myUUID, true);

// Listen for discovery changes
DeviceEventEmitter.addListener(
  'discoveredUsers',
  (data) => {
    if (data.uuid == myUUID) {
      if (data.didChange || data.usersChanged) { //slight callback discrepancy between the iOS and Android libraries
        console.log(data.users)
      }
    }
  }
);

```


####API

`initialize(uuid, username)` - string, string. Initialize the Discovery object with a UUID specific to your app, and a username specific to your device. Returns a promise which resolves to the specified UUID

`setPaused(uuid, isPaused)` - string, bool. pauses advertising and detection for the specified uuid. Returns a promise which resolves to true.

`setShouldDiscover(uuid, shouldDiscover)` - string, bool. starts and stops discovery for the specified uuid. Returns a promise which resolves to true.

`setShouldAdvertise(uuid, shouldAdvertise)` - string, bool. starts and stops advertising for the specified uuid. Returns a promise which resolves to true.

`setUserTimeoutInterval(uuid, userTimeoutInterval)` - string, integer in seconds (default is 5). After not seeing a user for x seconds, we remove him from the users list in our callback (for the specified uuid). Returns a promise which resolves to true.
  
  
*The following two methods are specific to the Android version, since the Android docs advise against continuous scanning. Instead, we cycle scanning on and off. This also allows us to modify the scan behaviour when the app moves to the background.*

`setScanForSeconds(uuid, scanForSeconds)` - string, integer in seconds (default is 5). This parameter specifies the duration of the ON part of the scan cycle for the specified uuid. Returns a promise which resolves to true.
    
`setWaitForSeconds(uuid, waitForSeconds)` - string, integer in seconds (default is 5). This parameter specifies the duration of the OFF part of the scan cycle for the specified uuid. Returns a promise which resolves to true.


##Setup

````
npm install --save react-native-discovery
````

###iOS
* Run open node_modules/react-native-discovery
* Drag ReactNativeDiscovery.xcodeproj into your Libraries group

###Android
#####Step 1 - Update Gradle Settings

```
// file: android/settings.gradle
...

include ':react-native-discovery'
project(':react-native-discovery').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-discovery/android')
```
#####Step 2 - Update Gradle Build

```
// file: android/app/build.gradle
...

dependencies {
    ...
    compile project(':react-native-discovery')
}
```
#####Step 3 - Register React Package
```
...
import com.joshblour.reactnativediscovery.ReactNativeDiscoveryPackage; // <--- import

public class MainActivity extends ReactActivity {

    ...

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
            new MainReactPackage(),
            new ReactNativeDiscoveryPackage(this) // <------ add the package
        );
    }

    ...
}
```
