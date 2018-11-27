# Androidplugin_Unity
## Unity
### BleConnect

### Plugins folder
#### build.gradle

Export jar

<pre><code>
task makeJar(type: Copy) {
    delete 'build/libs/AndroidPlugin.jar'
    from('build/intermediates/packaged-classes/release/')
    into('build/libs/')
    include('classes.jar')
    rename ('classes.jar', 'AndroidPlugin.jar')
}
makeJar.dependsOn(build)
</pre></code>

#### AndroidManifest.xml

Setting Android APK environment<br/>

* Main activity：com.wistron.gerry.bleconnect.androidbleconnect
* Package Name：com.wistron.gerry.bleconnect

#### bin -> AndroidPlugin.jar

Android export jar file let unity loading

## Android
### androidbleconnect.java<br/>

Bluetooth for android connect to oCare100_MBT

###AndroidManifest.xml<br/>

Permission setting<br/>

* android.permission.BLUETOOTH
* android.permission.BLUETOOTH_ADMIN
* android.hardware.bluetooth_le
