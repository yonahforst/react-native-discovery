# Discovery
Discover nearby devices using BLE.

React native implementation of https://github.com/omergul123/Discovery

(Android uses https://github.com/joshblour/discovery-android)

##What
Discovery is a very simple but useful library for discovering nearby devices with BLE(Bluetooth Low Energy) and for exchanging a value (kind of ID or username determined by you on the running app on peer device) regardless of whether the app on peer device works at foreground or background state.


###Example
```java
const Discovery = require('discovery-react');
Discovery.initialize(
  "3E1180E5-222E-43E9-98B4-E6C0DD18E728",
  "SpacemanSpiff"
);
Discovery.setShouldAdvertise(true);
Discovery.setShouldDiscover(true);

// Listen for discovery changes
DeviceEventEmitter.addListener(
  'discoveredUsers',
  (data) => {
    if (data.didChange || data.usersChanged) //slight callback discrepancy between the iOS and Android libraries
      console.log(data.users)
  }
);

```


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
import com.joshblour.reactnativediscovery.ReactNativeDiscovery; // <--- import

public class MainActivity extends ReactActivity {

    ...

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
            new MainReactPackage(),
            new ReactNativeDiscovery(this) // <------ add the package
        );
    }

    ...
}
```
