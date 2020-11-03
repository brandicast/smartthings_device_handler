/**
 *  Device Handler for Philio PAT05
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
 
 https://support.actiontiles.com/communities/12/topics/4889-air-quality-sensor-capability-caqi-or-aqi-or-gm3
 
 */
metadata {
	definition (name: "Device Handler for Philio PAT05", namespace: "Brandicast", author: "Brandon Wu", cstHandler: true) {
		capability "Air Quality Sensor"
        capability "Configuration"

		fingerprint mfr: "013C", prod: "0002", model: "0052", deviceJoinName: "Philio PAT05"
        // raw : zw:Ls2 type:2101 mfr:013C prod:0002 model:0052 ver:1.02 zwv:6.04 lib:03 cc:5E,6C,55,98,9F sec:86,85,59,31,72,5A,73,71,70,7A
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

def parse(String description) {
	log.debug "Parsing : $description\n\n"
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description)
	} else {
	    def cmd = zwave.parse(description)
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

private getCommandClassVersions() {
	[ 0x31:11, 0x70:1, 0x71:8, 0x72:2,  0x85:2, 0x98:1]

    
   // COMMAND_CLASS_MANUFACTURER_SPECIFIC_V2
   // COMMAND_CLASS_ASSOCIATION_V2
   // COMMAND_CLASS_CONFIGURATION
   // COMMAND_CLASS_NOTIFICATION_V8
   //COMMAND_CLASS_SENSOR_MULTILEVEL_V11

    // below are not specify version yet
    //COMMAND_CLASS_VERSION_V3
    //COMMAND_CLASS_FIRMWARE_UPDATE_MD_V4
    //COMMAND_CLASS_ASSOCIATION_GRP_INFO
    //COMMAND_CLASS_POWERLEVEL
    //COMMAND_CLASS_DEVICE_RESET_LOCALLY

}



def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
    log.debug "sensormultilevelv5 repot is called"
    def map = [:]
    log.debug "sensor type : ${cmd.sensorType}"
    map.name = "airQuality"
    map.unit = "ppm"
    
    switch (cmd.sensorType) {
        case 0x11:
            // CO2
            map.linkText = "eCO2"
            state.eco2 = cmd.scaledSensorValue.toInteger().toString()
            map.value =  state.eco2
            break;
        case 0x27:
            // TVOC
            map.linkText = "TVOC"
            state.tvoc = ((float)(cmd.scaledSensorValue.toInteger() /1000 )).toString()
            map.value = state.tvoc 
            break;
    }
    map.unit = map.unit + "(" + map.linkText + ")"
    map.descriptionText = "Air Quality (tVOC/eCO2): " + state.tvoc + "/" + state.eco2 + " ppm"
    
    //map
    createEvent(map)

}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	log.debug "notificationv3 repot is called"
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    log.debug "batteryv1 repot is called"
}


def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    log.debug "associationv2 repot is called"
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
    log.debug "securityv1 is called"
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
    log.debug "PAT05: configure() called"

    initialize ()
    
    sendEvent(name: "checkInterval", value: state.checkInterval , displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

}

private command(physicalgraph.zwave.Command cmd) {
    log.debug "command() is called"
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

     state.sensorName = "Air Quality"

     state.tvoc = "-"
     state.eco2 = "-"
      
     if (chkInterval)
        state.checkInterval = chkInterval
     else
        state.checkInterval = 5 * 60 * 1000
       
}

def refresh() {
	log.debug "refresh() is called"
       
        if ((chkInterval) && (state.checkInterval != chkInterval * 1000)) {
            sendEvent(name: "checkInterval", value: chkInterval , displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
            state.checkInterval = chkInterval
            log.info ("Check Interval is now : $chkInterval seconds" )  
        } 
       
}
