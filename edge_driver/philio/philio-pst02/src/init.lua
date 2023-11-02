local Configuration = (require "st.zwave.CommandClass.Configuration")({ version = 1 })
local WakeUp = (require "st.zwave.CommandClass.WakeUp")({ version = 2 })
local Battery = (require "st.zwave.CommandClass.Battery")({ version = 1 })
local Notification = (require "st.zwave.CommandClass.Notification")({ version = 4 })
local SensorMultilevel = (require "st.zwave.CommandClass.SensorMultilevel")({ version = 5 })
local SensorBinary = (require "st.zwave.CommandClass.SensorBinary")({ version = 2 })
local capabilities = require "st.capabilities"
local ZwaveDriver = require "st.zwave.driver"
local defaults = require "st.zwave.defaults"
local cc = require "st.zwave.CommandClass"
local TemperatureDefaults = require "st.zwave.defaults.temperatureMeasurement"
local log = require "log"
local configurations = require "configurations"

local PHILIO_PST02_SENSOR_FINGERPRINTS = {
  {mfr = 0x013C, prod = 0x0002, model = 0x000C}  -- Philio PST02-A
}

local CAPABILITIES = {
  capabilities.motionSensor,
  capabilities.contactSensor,
  capabilities.temperatureMeasurement,
  capabilities.illuminanceMeasurement,
  capabilities.battery,
}

local function can_handle_philio_sensor (opts, driver, device, ...) 
  for _, fingerprint in ipairs(PHILIO_PST02_SENSOR_FINGERPRINTS) do
    log.debug ("[Brandicast] can_handle_philio_sensor is called")
    if device:id_match(fingerprint.mfr, fingerprint.prod) then
      return true
    end
  end
  return false
end

local function update_parameters(self, device)
  --device:send(Version:Get({}))
  --[[
       22 : ask the device to send "motion off" report.  
            And changing notification type from Notification Report to with Sensor Binary Report 
                  (Somehow Notification Report in STH was interpreated as BURGLAR)
  --]]  
  device:send(Configuration:Set({parameter_number = 7, size = 1, configuration_value = 22}))
end

-- zwave_handlers start --
local function configuration_report(self, device, cmd)
  log.debug ("[Brandicast] configuration_report is called")
end 

local function wakeup_notification(self, device, cmd)
  log.debug ("[Brandicast] wakeup_notification is called")
end 

local function battery_report(self, device, cmd)
  log.debug ("[Brandicast] battery_report is called")
end 

local function notification_report_handler(self, device, cmd)
  log.debug ("[Brandicast] notification_report_handler is called")
end 

local function sensor_multilevel_report_handler(self, device, cmd)
  log.debug ("[Brandicast] sensor_multilevel_report_handler is called")
  print (type(self))
  if cmd.args.sensor_type == 3 then
    local max_illuminance = 10000
    local lux = math.tointeger(max_illuminance * cmd.args.sensor_value / 100)
    device:emit_event_for_endpoint(cmd.src_channel, capabilities.illuminanceMeasurement.illuminance({value = lux, unit = "lux"}))
  elseif cmd.args.sensor_type == 1 then
   -- device:emit_event_for_endpoint(cmd.src_channel, capabilities.temperatureMeasurement.temperature({value = cmd.args.sensor_value, unit = "F"} ))
   TemperatureDefaults.zwave_handlers[cc.SENSOR_MULTILEVEL][SensorMultilevel.REPORT](self, device, cmd)
  end 
end 

local function sensor_binary_report_handler(self, device, cmd)
  log.debug ("[Brandicast] sensor_binary_report_handler is called")
end 

-- zwave_handlers end --

-- lifecycle handlers start --
local function device_init(self, device)
  log.debug ("[Brandicast] device_init is called")
  log.debug(device)
--  update_parameters(self, device)
  log.debug ("[Brandicast] device_init is finished")
end

local function do_configure(self, device, event, args)
  log.debug ("[Brandicast] do_configure is called")
  configurations.initial_configuration(self, device)
end

--[[
local function added_handler(self, device, event, args)
  log.debug ("[Brandicast] added_handler is called")
end

local function info_changed(self, device, event, args)
  log.debug ("[Brandicast] info_changed is called")
end



local function driver_switched(self, device, event, args)
  log.debug ("[Brandicast] driver_switched is called")
end

--]]

-- lifecycle handlers end --
--[[
local function dump_table (table, prefix)
  prefix = prefix .. "-"
  for key, value in pairs (table) do
    print (prefix, " ", key, " : ", value)
    if type(value) == "table" then
      dump_table (value, prefix)
    end 
  end 
end
--]]



local driver_template = {
  zwave_handlers = {
  --[[
    [cc.CONFIGURATION] = {
      [Configuration.REPORT] = configuration_report
    },
    [cc.WAKE_UP] = {
      [WakeUp.NOTIFICATION] = wakeup_notification,
    },
    [cc.BATTERY] = {
      [Battery.REPORT] = battery_report,
    },
    [cc.NOTIFICATION] = {
      [Notification.REPORT] = notification_report_handler
    },
    [cc.SENSOR_BINARY] = {
      [SensorBinary.REPORT] = sensor_binary_report_handler
    },
  --]]
    [cc.SENSOR_MULTILEVEL] = {
      [SensorMultilevel.REPORT] = sensor_multilevel_report_handler
    },
  },
  supported_capabilities = CAPABILITIES,
  lifecycle_handlers = {
    --added          = added_handler,
    init           = device_init,
    --infoChanged    = info_changed,
    doConfigure    = do_configure,
    --driverSwitched = driver_switched,
  },
  NAME = "Philio ZWave MultiSensor PST02-A",
  can_handle = can_handle_philio_sensor,
}

--[[
  The default handlers take care of the Command Classes and the translation to capability events 
  for most devices, but you can still define custom handlers to override them.
]]--

defaults.register_for_default_handlers(driver_template, driver_template.supported_capabilities)

-- log.info ("Yo info")
-- log.error ('Yo err')
local multiSensor = ZwaveDriver("philio-pst02-a-multisensor", driver_template)
multiSensor:run()