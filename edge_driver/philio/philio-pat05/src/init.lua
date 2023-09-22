local Configuration = (require "st.zwave.CommandClass.Configuration")({ version = 1 })
local capabilities = require "st.capabilities"
local ZwaveDriver = require "st.zwave.driver"
local defaults = require "st.zwave.defaults"
local cc = require "st.zwave.CommandClass"
local log = require "log"

local ZWAVE_AIRQ_SENSOR_FINGERPRINTS = {
  {mfr = 0x013C, prod = 0x0002, model = 0x0052}  -- Philio PAT-05
}

local function can_handle_philio_air_quality_sensor (opts, driver, device, ...) 
  for _, fingerprint in ipairs(ZWAVE_AIRQ_SENSOR_FINGERPRINTS) do
    if device:id_match(fingerprint.mfr, fingerprint.prod) then
      return true
    end
  end
  return false
end



local driver_template = {
  zwave_handlers = {
    [cc.CONFIGURATION] = {
      [Configuration.REPORT] = configuration_report
    }
  },
  supported_capabilities = {
    capabilities.tvocMeasurement,
    capabilities.carbonDioxideMeasurement,
  },
  lifecycle_handlers = {
    init = init_dev
  },
  NAME = "Philio ZWave Air Quality Sensor",
  can_handle = can_handle_philio_air_quality_sensor,
}

--[[
  The default handlers take care of the Command Classes and the translation to capability events 
  for most devices, but you can still define custom handlers to override them.
]]--

defaults.register_for_default_handlers(driver_template, driver_template.supported_capabilities)
local multiSensor = ZwaveDriver("philio-pat05-airquality-sensor", driver_template)
multiSensor:run()