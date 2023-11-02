--- @type st.zwave.CommandClass.Configuration
local Configuration = (require "st.zwave.CommandClass.Configuration")({ version=1 })
--- @type st.zwave.CommandClass.Association
local Association = (require "st.zwave.CommandClass.Association")({ version=2 })
--- @type st.zwave.CommandClass.Notification
local Notification = (require "st.zwave.CommandClass.Notification")({ version=8 })
--- @type st.zwave.CommandClass.WakeUp
local WakeUp = (require "st.zwave.CommandClass.WakeUp")({ version = 2 })

local log = require "log"
 --[[
       22 : ask the device to send "motion off" report.  
            And changing notification type from Notification Report to with Sensor Binary Report 
                  (Somehow Notification Report in STH was interpreated as BURGLAR)
  --]]       
local devices = {
    Philio_ZWave_Sensor_PST02_A = {
    MATCHING_MATRIX = {
      mfrs = 0x013C,
      product_types = 0x0002,
      product_ids = 0x000C
    },      
    CONFIGURATION = {
      {parameter_number = 7, configuration_value = 22, size = 1}
    }
  }
}
local configurations = {}

configurations.initial_configuration = function(driver, device)
  log.debug ("[Brandicast] configurations.initial_configuration is called")    
  local configuration = configurations.get_device_configuration(device)
  if configuration ~= nil then
    for _, value in ipairs(configuration) do
      --device:send(Configuration:Set(value))
      log.debug ("[Brandicast] configurations.initial_configuration sending Configuration:Set")    
      device:send(Configuration:Set({parameter_number = value.parameter_number, size = value.size, configuration_value = value.configuration_value}))
    end
  end

  local association = configurations.get_device_association(device)
  if association ~= nil then
    for _, value in ipairs(association) do
      local _node_ids = value.node_ids or {driver.environment_info.hub_zwave_id}
      device:send(Association:Set({grouping_identifier = value.grouping_identifier, node_ids = _node_ids}))
    end
  end
  local notification = configurations.get_device_notification(device)
  if notification ~= nil then
    for _, value in ipairs(notification) do
      device:send(Notification:Set(value))
    end
  end
  local wake_up = configurations.get_device_wake_up(device)
  if wake_up ~= nil then
    for _, value in ipairs(wake_up) do
      local _node_id = value.node_id or driver.environment_info.hub_zwave_id
      device:send(WakeUp:IntervalSet({seconds = value.seconds, node_id = _node_id}))
    end
  end
end

configurations.get_device_configuration = function(zw_device)
  for _, device in pairs(devices) do
    if zw_device:id_match(
      device.MATCHING_MATRIX.mfrs,
      device.MATCHING_MATRIX.product_types,
      device.MATCHING_MATRIX.product_ids) then
      return device.CONFIGURATION
    end
  end
  return nil
end

configurations.get_device_association = function(zw_device)
  for _, device in pairs(devices) do
    if zw_device:id_match(
      device.MATCHING_MATRIX.mfrs,
      device.MATCHING_MATRIX.product_types,
      device.MATCHING_MATRIX.product_ids) then
      return device.ASSOCIATION
    end
  end
  return nil
end

configurations.get_device_notification = function(zw_device)
  for _, device in pairs(devices) do
    if zw_device:id_match(
      device.MATCHING_MATRIX.mfrs,
      device.MATCHING_MATRIX.product_types,
      device.MATCHING_MATRIX.product_ids) then
      return device.NOTIFICATION
    end
  end
  return nil
end

configurations.get_device_wake_up = function(zw_device)
  for _, device in pairs(devices) do
    if zw_device:id_match(
      device.MATCHING_MATRIX.mfrs,
      device.MATCHING_MATRIX.product_types,
      device.MATCHING_MATRIX.product_ids) then
      return device.WAKE_UP
    end
  end
  return nil
end

return configurations