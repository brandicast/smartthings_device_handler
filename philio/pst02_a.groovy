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
        

         fingerprint mfr: "013C", prod: "0002", model: "000C" //, cc:"5E,72,86,59,73,5A,8F,98,7A", ccOut:"20", sec:"85,80,71,85,70,30,31,84"
        //fingerprint mfr: "013C", prod: "0002"    			// 少了 model 000c 會沒有illumunation and motion      
        //zw:Ss type:2101 mfr:013C prod:0002 model:0064 ver:1.04 zwv:4.05 lib:03 cc:5E,86,72,98,84 ccOut:5A sec:59,85,73,71,80,30,31,70,7A role:06 ff:8C07 ui:8C07
    }

    tiles (scale:2) {
            
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
                state "battery", label:'${currentValue}% battery', unit:""
        }

        valueTile("temperature", "device.temperature") {
                state("temperature", label:'${currentValue}°',backgroundColors:[
                                [value: 31, color: "#153591"],
                                [value: 44, color: "#1e9cbb"],
                                [value: 59, color: "#90d2a7"],
                                [value: 74, color: "#44b621"],
                                [value: 84, color: "#f1d801"],
                                [value: 95, color: "#d04e00"],
                                [value: 96, color: "#bc2323"]
                        ]
                )
        }

        valueTile("illuminance", "device.illuminance", width: 2, height: 2) {
                state "val", label:'${currentValue}', defaultState: true, backgroundColors: [
                        [value: 10, color: "#ff0000"],
                        [value: 90, color: "#0000ff"]
                ]
        }


        valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
                state "clear", label: 'tamper clear', backgroundColor: "#ffffff"
                state "detected", label: 'tampered', backgroundColor: "#ff0000"
        }

        multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4) {
                tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
                        attributeState("open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
                        attributeState("closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC")
                }
        }

        multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
                tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
                        attributeState("active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC")
                        attributeState("inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#CCCCCC")
                }
        }

        main (["contact", "temperature"])
        details (["contact", "motion", "temperature", "battery", "tamper"])
}

}   

private getCommandClassVersions() {
	[  0x30: 2, 0x31: 5, 0x70: 1, 0x71: 3, 0x72: 2, 0x84: 2, 0x85: 2]
	
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
	log.debug "Parsing : $description"
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description)
	} else {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
            log.debug "**************************************************************************\n\n"
            log.debug "* description : ${description}"
            log.debug "* cmd    : ${cmd}"
            log.debug "* result : $result"
            log.debug "**************************************************************************"
            
		} else {
			result = createEvent(value: description, descriptionText: description, isStateChange: false)
		}
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
        def map = [ displayed: true, value: cmd.scaledSensorValue.toString() ]
        switch (cmd.sensorType) {
                case 1:
                        map.name = "temperature"
                        map.unit = cmd.scale == 1 ? "F" : "C"
                        break;
                case 3:
                        map.name = "illuminance"
                        map.value = cmd.scaledSensorValue.toInteger().toString()
                        map.unit = "lux"
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
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
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
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	log.debug "[NOT HANDLED] basicv1.BasicSet is called : ${cmd} "
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
    log.debug "[NOT HANDLED] basicv1.BasicReport is called : ${cmd}"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
        createEvent(descriptionText: "[NOT HANDLED] ${device.displayName}: ${cmd}")
}

def on() {
	log.debug "on() is called"
        delayBetween([
                zwave.basicV1.basicSet(value: 0xFF).format(),
                zwave.basicV1.basicGet().format()
        ], 5000)  // 5 second delay for dimmers that change gradually, can be left out for immediate switches
}

def off() {
	log.debug "off() is called"
        delayBetween([
                zwave.basicV1.basicSet(value: 0x00).format(),
                zwave.basicV1.basicGet().format()
        ], 5000)  // 5 second delay for dimmers that change gradually, can be left out for immediate switches
}

def refresh() {
		log.debug "refresh() is called"
        // Some examples of Get commands
        delayBetween([
                zwave.switchBinaryV1.switchBinaryGet().format(),
                zwave.switchMultilevelV1.switchMultilevelGet().format(),
                zwave.meterV2.meterGet(scale: 0).format(),      // get kWh
                zwave.meterV2.meterGet(scale: 2).format(),      // get Watts
                zwave.sensorMultilevelV1.sensorMultilevelGet().format(),
                zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1).format(),  // get temp in Fahrenheit
                zwave.batteryV1.batteryGet().format(),
                zwave.basicV1.basicGet().format(),
        ], 1200)
}

// If you add the Polling capability to your device type, this command
// will be called approximately every 5 minutes to check the device's state
def poll() {
		log.debug "poll() is called"
        zwave.basicV1.basicGet().format()
}

// If you add the Configuration capability to your device type, this
// command will be called right after the device joins to set
// device-specific configuration commands.
def configure() {
	log.debug "configure() is called"
        delayBetween([
                // Note that configurationSet.size is 1, 2, or 4 and generally
                // must match the size the device uses in its configurationReport
                zwave.configurationV1.configurationSet(parameterNumber:7, size:1, scaledConfigurationValue:20).format(),

                // Can use the zwaveHubNodeId variable to add the hub to the
                // device's associations:
                //zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format(),

                // Make sure sleepy battery-powered sensors send their
                // WakeUpNotifications to the hub every 4 hours:
                //zwave.wakeUpV1.wakeUpIntervalSet(seconds:4 * 3600, nodeid:zwaveHubNodeId).format(),
        ])
}

/*
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
                        break
                case 0x0B:
                        result = createEvent(name:"tilt", value: cmd.sensorValue ? "detected" : "okay")
                        break
                case 0x0C:
                        result = createEvent(name:"motion",
                                value: cmd.sensorValue ? "active" : "inactive")
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
*/