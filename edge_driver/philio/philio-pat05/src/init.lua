--local Configuration = (require "st.zwave.CommandClass.Configuration")({ version = 1 })
local capabilities = require "st.capabilities"
local ZwaveDriver = require "st.zwave.driver"
local defaults = require "st.zwave.defaults"
-- local cc = require "st.zwave.CommandClass"
local log = require "log"

local ZWAVE_AIRQ_SENSOR_FINGERPRINTS = {
  {mfr = 0x013C, prod = 0x0002, model = 0x0052}  -- Philio PAT-05
}


local driver_template = {
  supported_capabilities = {
    capabilities.tvocMeasurement,
    capabilities.carbonDioxideMeasurement,
  },
  NAME = "Philio ZWave Air Quality Sensor",
}

--[[
  The default handlers take care of the Command Classes and the translation to capability events 
  for most devices, but you can still define custom handlers to override them.
]]--

defaults.register_for_default_handlers(driver_template, driver_template.supported_capabilities)
local multiSensor = ZwaveDriver("philio-pat05-airquality-sensor", driver_template)
multiSensor:run()