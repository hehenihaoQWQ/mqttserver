package com.lqwql.mqttserver.controller;

import com.lqwql.mqttserver.entity.Device;
import com.lqwql.mqttserver.entity.Message;
import com.lqwql.mqttserver.mqtt.MqttGateway;
import com.lqwql.mqttserver.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController                 // 返回json格式的数据
@CrossOrigin                    // 允许跨域
//@RequestMapping("api/device")   // 下级目录
public class DeviceController {
    @Autowired
    private MqttGateway mqttGateway;
    @Autowired
    private DeviceService ds;
    @RequestMapping("test")
    public void test(){
        mqttGateway.sendToMqtt("gps-topic", "sssbbbasd");
    }

    @GetMapping("/get-device-list")
    public Map<String, Object> getDeviceList(String id){
        Map<String, Object> res = new HashMap<>();
        Boolean success = false;
        try
        {
            res.put("data", ds.getDeviceList(id));
            success = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            res.put("msg", "ERROR");
        }
        res.put("success", success);
        return res;
    }
    @PostMapping("/get-device")
    public Map<String, Object> getDevice(@RequestBody Device device){
        Map<String, Object> res = new HashMap<>();
        Boolean success = false;
        try
        {
            res.put("name", device.getName());
            res.put("data", ds.getAirData(device.getId()));
            success = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            res.put("msg", "ERROR");
        }
        res.put("success", success);
        return res;
    }
    @GetMapping("/get-device-data")
    public void getDeviceData(){

    }
    @PostMapping("/device-control")
    @ResponseBody
    public Map<String, Object> deviceControl(@RequestBody Message message){
        Map<String, Object> res = new HashMap<>();
        Boolean success = false;
        try
        {
            mqttGateway.sendToMqtt(message.getTopic(), message.getValue());
            success = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            res.put("msg", "ERROR");
        }
        res.put("success", success);
        return res;
    }
    @PostMapping("/device-set-name")
    @ResponseBody
    public Map<String, Object> deviceSetName(@RequestBody Device device){
        Map<String, Object> res = new HashMap<>();
        Boolean success = false;
        try
        {
            ds.deviceSetName(device.getId(), device.getName());
            success = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            res.put("msg", "ERROR");
        }
        res.put("success", success);
        return res;
    }
}
