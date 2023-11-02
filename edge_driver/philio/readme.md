# Smartthings Edge Driver for Philio Devices

Currently available devices:  
- Philio ZWave Sensor PST02-A
- Philio ZWave Sensor PAT05

## Driver Channel link below :

https://callaway.smartthings.com/channels/0c6e8195-fcbd-4a9e-9359-42f0faf19f8b

# Smartthings CLI

- https://github.com/SmartThingsCommunity/smartthings-cli/releases
- To be able to start using CLI for packaging and communicate with your smartthings, Persona Access Token (PAT) is required.
  - As mentioned in document, could add a config.yaml under {user}\AppData\Local\@smartthings\cli and specify PAT inside.
- CLI command list : 

  https://github.com/SmartThingsCommunity/smartthings-cli#commands



# My Smartthings Console
 https://my.smartthings.com/advanced/hubs/23c4fe6e-54bd-4b50-b632-27f22c016c4b


# Reference
- https://github.com/Mariano-Github/Edge-Drivers-Beta
- https://github.com/SmartThingsDevelopers/SampleDrivers
- https://developer.smartthings.com/docs/devices/hub-connected/get-started/
- https://developer.smartthings.com/docs/edge-device-drivers/zwave/driver.html
- https://github.com/philh30/ST-Edge-Drivers
- Smartthings CLI command :
    https://github.com/SmartThingsCommunity/smartthings-cli#commands
- Smartthings supported ZWave command class list :

    https://graph.api.smartthings.com/ide/doc/zwave-utils.html
- Edge Driver Reference (This one is hard to find from the official web site unless you googled it)

  https://developer.smartthings.com/docs/edge-device-drivers/reference/index.html#


# Personal note

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
    <pre>
        smartthings edge:drivers:package [path]   </pre>
    this command returns ```[driver id]```

    2. assign
    <pre>
        smartthings edge:channels:assign [driver id]   </pre>
    this command will ask to choose channel id

    3. install
    <pre>
        smartthings edge:drivers:install</pre>

- device profile's category refers to 
https://developer.smartthings.com/docs/devices/device-profiles

- config set in lua
  <pre>
      device:send(Configuration:Set({parameter_number = value.parameter_number, size = value.size, configuration_value = value.configuration_value}))
    </pre>
- multiple line comments in LUA

  syntax as following :
  <pre>
  --[[
      Whatever comments here
  --]]
  </pre>

- client side API document for zwave isn't complete, download the API release from GitHub, and unzip the lua driver source code for complete API

- Customt capability, presentation are json format and submit to edge server thru command.  Then shall be able to reference in LUA code with namespace.cap_id or presentation id.  

# PST02-A
- As Smartthings support older command class, to display the motion event properly, changing the device report from Notification Report to Sensor Binary report (by changing the device configuration No 7's bit4 to 1) 
  - could also consider to set this to default while during installation in lua driver [To-Do]


# Smartthings Communities

  https://community.smartthings.com/

  Originally didn't expect much from the community since Smartthings device integration is relatively niche.  Turns out there are still enthusiastic people helping out.  

  Particularly thanks to Mariano_Colmenarejo for many tips to fill the gap between website documents and actual development. He also provides a handlful tool for device configuration. (https://github.com/Mariano-Github)