/**
 *  Device Handler for Philio PST02-A
 *
 *  Copyright 2020 Brandon Wu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
    definition (name: "Device Handler for Philio PST02-A", namespace: "Brandicast", author: "Brandon Wu") {
    	capability "Configuration"
	capability "Battery"
	capability "Contact Sensor"
	capability "Motion Sensor"
	capability "Tamper Alert"
	capability "Temperature Measurement"
	capability "Illuminance Measurement"
 	capability "Polling"
        capability "Sensor"
        capability "Refresh"

        command "clearTamper"       

        fingerprint mfr: "013C", prod: "0002", model: "000C" //, cc:"5E,72,86,59,73,5A,8F,98,7A", ccOut:"20", sec:"85,80,71,85,70,30,31,84"
        //fingerprint mfr: "013C", prod: "0002"    			// 少了 model 000c 會沒有illumunation and motion      
        //zw:Ss type:2101 mfr:013C prod:0002 model:0064 ver:1.04 zwv:4.05 lib:03 cc:5E,86,72,98,84 ccOut:5A sec:59,85,73,71,80,30,31,70,7A role:06 ff:8C07 ui:8C07
        
        fingerprint deviceId: "0x0701", inClusters: "0x5E,0x72,0x86,0x59,0x73,0x5A,0x8F,0x98,0x7A", outClusters: "0x20"
    }

        // the name of the input may cause the preference parameter unable to read.  Guess groovy parse is too smart to check the input name with existing function.
        preferences {
            input name: "chkInterval", type: "number", title: "Device Checking Interval", description: "Number in second for checking interval", required: true
           // input name: "tScale", type: "enum", title: "Temperature Scale", options: [["X":"Fahrenheit"], ["Y":"Celsius"]], description: "Choose Temperature Scale", required: true
        }

}   


private getCommandClassVersions() {
	[ 0x20:1, 0x30:2, 0x31:5, 0x70:1, 0x71:3, 0x72:2, 0x84:2, 0x85:2, 0x98:1]
	
	// 0x30 : COMMAND_CLASS_SENSOR_BINARY_V2         ->  Philio PST02-A : v2
	// 0x31 : COMMAND_CLASS_SENSOR_MULTILEVEL_V5     ->  Philio PST02-A : v5
	// 0x70 : COMMAND_CLASS_CONFIGURATION            ->  Philio PST02-A : v1
	// 0x71 : COMMAND_CLASS_NOTIFICATION_V4          ->  Philio PST02-A : v4
	// 0x72 : COMMAND_CLASS_MANUFACTURER_SPECIFIC_V2 ->  Philio PST02-A : v2
	// 0x84 : COMMAND_CLASS_WAKE_UP_V2               ->  Philio PST02-A : V2
	// 0x85 : COMMAND_CLASS_ASSOCIATION_V2	         ->  Philio PST02-A : V2
	// 0x98 : COMMAND_CLASS_SECURITY                 ->  Philio PST02-A : V1
}


def parse(String description) {
	log.debug "Parsing : $description\n\n"
        state.sensorName = "N/A"
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description)
	} else {
	    def cmd = zwave.parse(description, commandClassVersions)
	    if (cmd) {
		result = zwaveEvent(cmd)
                log.debug "**************************************************************************"
                log.debug "* result : $result"
                log.debug "* description : ${description}"
                log.debug "* cmd    : ${cmd}"
                log.debug "* sensor :  ${state.sensorName}"
                log.debug "**************************************************************************"
                //log.debug "${device.currentValue('tamper')}"   
	    } else {
		result = createEvent(value: description, descriptionText: description, isStateChange: false)
	    }
	}

	return result
}


def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
        def result
        switch (cmd.sensorType) {
                case 2:
                        result = createEvent(name:"smoke",
                                value: cmd.sensorValue ? "detected" : "closed")
                        break
                case 3:
                        result = createEvent(name:"carbonMonoxide",
                                value: cmd.sensorValue ? "detected" : "clear")
                        break
                case 4:
                        result = createEvent(name:"carbonDioxide",
                                value: cmd.sensorValue ? "detected" : "clear")
                        break
                case 5:
                        result = createEvent(name:"temperature",
                                value: cmd.sensorValue ? "overheated" : "normal")
                        break
                case 6:
                        result = createEvent(name:"water",
                                value: cmd.sensorValue ? "wet" : "dry")
                        break
                case 7:
                        result = createEvent(name:"temperature",
                                value: cmd.sensorValue ? "freezing" : "normal")
                        break
                case 8:
                        result = createEvent(name:"tamper",
                                value: cmd.sensorValue ? "detected" : "okay")
                        break
                case 9:
                        result = createEvent(name:"aux",
                                value: cmd.sensorValue ? "active" : "inactive")
                        break
                case 0x0A:
                        result = createEvent(name:"contact",
                        value: cmd.sensorValue ? "open" : "closed")
                        state.sensorName = "Contact Sensor"
                        break
                case 0x0B:
                        result = createEvent(name:"tilt", value: cmd.sensorValue ? "detected" : "okay")
                        break
                case 0x0C:
                        result = createEvent(name:"motion",
                        value: cmd.sensorValue ? "active" : "inactive")
                        state.sensorName = "Motion Sensor"
                        break
                case 0x0D:
                        result = createEvent(name:"glassBreak",
                        value: cmd.sensorValue ? "detected" : "okay")
                        break
                default:
                        result = createEvent(name:"sensor",
                        value: cmd.sensorValue ? "active" : "inactive")
                        break
        }
        result
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
        def map = [ displayed: true, value: cmd.scaledSensorValue.toString() ]
        switch (cmd.sensorType) {
                case 1:
                        map.name = "temperature"
                        map.unit = cmd.scale == 1 ? "F" : "C"
                        //log.debug "temp value = ${cmd.scaledSensorValue}  ${cmd.scale}  ${cmd.precision}"
                        map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, map.unit, cmd.precision)
           		map.unit = getTemperatureScale()
                        state.sensorName = "Temperature Sensor"
                        break;
                case 3:
                        map.name = "illuminance"
                        map.value = cmd.scaledSensorValue.toInteger().toString()
                        map.unit = "lux"
                        state.sensorName = "Illuminance Sensor"
                        break;
        }
        createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	if (cmd.notificationType == 0x06) {
    	switch (cmd.event) {
        	case 0x16:
            	result << createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
                break;
            case 0x17:
            	result << createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
                break;
            default:
           		result << createEvent(descriptionText: "$device.displayName detected for unknown event", isStateChange: true)
                break;
        }
     }
     else if (cmd.notificationType == 0x07) {
     		switch (cmd.event) {
            	case 0x08:
                	result << createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
                    break;
                case 0x03:
                	result << createEvent(name: "tamper", value: "detected")
                    break;
                default:
                	result << createEvent(name: "motion", value: "inactive")
                    result << createEvent(name: "tamper", value: "clear")
                    break;
                	
             }
     }
	 else {
		result << createEvent(descriptionText: "$device.displayName detected for unknown notification type", isStateChange: true)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
			map.value = 1
			map.descriptionText = "${device.displayName} has a low battery"
			map.isStateChange = true
	} else {
			map.value = cmd.batteryLevel
	}
	// Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
	state.lastbatt = new Date().time
	createEvent(map)
}

// Battery powered devices can be configured to periodically wake up and
// check in. They send this command and stay awake long enough to receive
// commands, or until they get a WakeUpNoMoreInformation command that
// instructs them that there are no more commands to receive and they can
// stop listening.
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd){
        def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

        // Only ask for battery if we haven't had a BatteryReport in a while
        if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
                result << response(zwave.batteryV1.batteryGet())
                result << response("delay 1200")  // leave time for device to respond to batteryGet
        }
        result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
        result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	def result = []
	if (cmd.nodeId.any { it == zwaveHubNodeId }) {
			result << createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
	} else if (cmd.groupingIdentifier == 1) {
			// We're not associated properly to group 1, set association
			result << createEvent(descriptionText: "Associating $device.displayName in group ${cmd.groupingIdentifier}")
			result << response(zwave.associationV1.associationSet(groupingIdentifier:cmd.groupingIdentifier, nodeId:zwaveHubNodeId))
	}
	result
}

// Devices that support the Security command class can send messages in an
// encrypted form; they arrive wrapped in a SecurityMessageEncapsulation
// command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 1, 0x20: 1])

	// can specify command class versions here like in zwave.parse
	if (encapsulatedCommand) {
			return zwaveEvent(encapsulatedCommand)
	}
}


// Many sensors send BasicSet commands to associated devices.
// This is so you can associate them with a switch-type device
// and they can directly turn it on/off when the sensor is triggered.
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd){
    log.debug "[NOT HANDLED] basicv1.BasicSet is called : ${cmd} "
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd){
    log.debug "[NOT HANDLED] basicv1.BasicReport is called : ${cmd}"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
        createEvent(descriptionText: "[NOT HANDLED] ${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
     log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"    
}


// If you add the Configuration capability to your device type, this
// command will be called right after the device joins to set
// device-specific configuration commands.
//  
// 2020/10/27 - This method works only during inclusion.  It doesn't work even directly called from emulator.
//

def configure() {
    log.debug "PST02: configure() called"

    initialize ()
    
    sendEvent(name: "checkInterval", value: state.checkInterval , displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

    def request = []
	
    request << zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: 8) // Operation Mode : Change temperature unit to Celsius
   
    request << zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: 22) // Operation Mode : Change from notification report to sensor binary.  Enable motion sensor sending Motion Off.

    /*
    request << zwave.wakeUpV2.wakeUpIntervalSet(seconds: 24 * 3600, nodeid:zwaveHubNodeId) // Wake up period

    //7. query sensor data
    request << zwave.batteryV1.batteryGet()
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1) //temperature
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3) //illuminance
    
    // 8. query notification data
    request << zwave.notificationV3.notificationGet(notificationType: 7)
   
      */

   return  commands(request) + ["delay 5000", zwave.configurationV1.configurationGet().format()]
}

private command(physicalgraph.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=200) {
    log.info "sending commands: ${commands}"
    delayBetween(commands.collect{ command(it) }, delay)
}

def initialize (){
     log.debug "init() is called"
     state.clear_tamper = false ;
      
     if (chkInterval)
        state.checkInterval = chkInterval
     else
        state.checkInterval = 5 * 60 * 1000
       
}

def refresh() {
	log.debug "refresh() is called"
        clearTamper()
        if ((chkInterval) && (state.checkInterval != chkInterval * 1000)) {
            sendEvent(name: "checkInterval", value: chkInterval , displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
            state.checkInterval = chkInterval
            log.info ("Check Interval is now : $chkInterval seconds" )  
        } 
       
}


def clearTamper() {
    log.debug "PAT02: clearing tamper"
    if (device.currentValue('tamper') == "detected") {
        def map = [:]
        map.name = "tamper"
        map.value = "clear"
        map.descriptionText = "$device.displayName is cleared"
        createEvent(map)
        sendEvent(map)
    }
}

/*

def on() {
	log.debug "on() is called"
}


def off() {
	log.debug "off() is called"
        delayBetween([
                zwave.basicV1.basicSet(value: 0x00).format(),
                zwave.basicV1.basicGet().format()
        ], 5000)  // 5 second delay for dimmers that change gradually, can be left out for immediate switches
}


// If you add the Polling capability to your device type, this command
// will be called approximately every 5 minutes to check the device's state
def poll() {
		log.debug "poll() is called"
        zwave.basicV1.basicGet().format()
}



*/