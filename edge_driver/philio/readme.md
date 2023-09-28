＃ Smartthings Edge Driver for Philio Devices

Currently available devices:  
- Philio ZWave Sensor PST02-A
- Philio ZWave Sensor PAT05

https://callaway.smartthings.com/channels/0c6e8195-fcbd-4a9e-9359-42f0faf19f8b

# Smartthings CLI

https://github.com/SmartThingsCommunity/smartthings-cli/releases

# My Smartthings Console
https://my.smartthings.com/advanced/hubs/23c4fe6e-54bd-4b50-b632-27f22c016c4b

# reference
- https://github.com/Mariano-Github/Edge-Drivers-Beta
- https://github.com/SmartThingsDevelopers/SampleDrivers
- https://developer.smartthings.com/docs/devices/hub-connected/get-started/
- https://developer.smartthings.com/docs/edge-device-drivers/zwave/driver.html


# personal note

- capabilities specified in xxx-profile.yml and fingerprints.yml refers to the "title" (bold texts, not "name") on  https://developer.smartthings.com/docs/devices/capabilities/capabilities-reference 
- <b>deviceProfileName</b> in fingerprints.yml refers to the name speciified in profile.yml
- logging command : 
<pre> 
    smartthings edge:drivers:logcat --hub-address [HubIP]
</pre>

- in fingerprint.yml

    manufacturerId: <map to fingerprint string's mfr>
    productType: <map to fingerprint string's prod> 
    productId: <map to fingerprint string's model> 

- refer to https://developer.smartthings.com/docs/devices/hub-connected/driver-components-and-structure for the driver file structure and installation

- once finish creating the package, channel, enrolled and so on, to update the driver only need 3 steps :
    1. package again
    2. assign
    3. install

- device profile's category refers to 
https://developer.smartthings.com/docs/devices/device-profiles

