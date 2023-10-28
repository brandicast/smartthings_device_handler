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
local log = require "log"

local PHILIO_PST02_SENSOR_FINGERPRINTS = {
  {mfr = 0x013C, prod = 0x0002, model = 0x000C}  -- Philio PST02-A
}

local CAPABILITIES = {
  capabilities.motionSensor,
  capabilities.contactSensor,
  capabilities.temperatureMeasurement,
  capabilities.illuminanceMeasurement,
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

local function call_parent_handler(handlers, self, device, event, args)
  if type(handlers) == "function" then
    handlers = { handlers }  -- wrap as table
  end
  for _, func in ipairs( handlers or {} ) do
      func(self, device, event, args)
  end
end

local function dump_table (table, prefix)
  prefix = prefix .. "-"
  for key, value in pairs (table) do
    print (prefix, " ", key, " : ", value)
    if type(value) == "table" then
      dump_table (value, prefix)
    end 
  end 
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
  if cmd.args.sensor_type == 3 then
    local max_illuminance = 10000
    device:emit_event_for_endpoint(cmd.src_channel, capabilities.illuminanceMeasurement.illuminance({value = max_illuminance * cmd.args.sensor_value / 100, unit = "lux"}))
  elseif cmd.args.sensor_type == 1 then
    device:emit_event_for_endpoint(cmd.src_channel, capabilities.temperatureMeasurement.temperature({value = cmd.args.sensor_value, unit = "F"} ))
  end 
  
end 

local function sensor_binary_report_handler(self, device, cmd)
  log.debug ("[Brandicast] sensor_binary_report_handler is called")
end 

-- zwave_handlers end --

-- lifecycle handlers start --
local function device_init(self, device)
  log.debug ("[Brandicast] device_init is called")
  log.debug (device)
  log.debug (device.id)  -- this returns the "device id" in smartthings  (a long hex number, NOT the id in profile)
  log.debug (device.label) -- this returns the "deviceLabel" defined in fingerprints.yml
  log.debug (device.network_type) -- this returns "DEVICE_ZWAVE"
  -- device.debug_pretty_print(device)  -- this returns nothing
  -- log.debug (device.pretty_print(device)) -- this returns the same as device itself  
  log.debug ("[Brandicast] device_init is finished")
end

local function info_changed(self, device, event, args)
  log.debug ("[Brandicast] info_changed is called")
end

-- lifecycle handlers end --

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
    -- added          = added_handler,
    init           = device_init,
    infoChanged    = info_changed,
    -- doConfigure    = do_configure,
    -- driverSwitched = driver_switched,
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