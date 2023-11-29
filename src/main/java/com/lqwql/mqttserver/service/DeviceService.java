package com.lqwql.mqttserver.service;

import com.lqwql.mqttserver.entity.AirData;
import com.lqwql.mqttserver.entity.Device;
import com.lqwql.mqttserver.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class DeviceService {
    @Autowired
    private DeviceMapper dm;
    public List<Device> getDeviceList(String uid) {
        return dm.getDeviceList(uid);
    }
    public List<AirData> getAirData(String cid) {
        return dm.getAirData(cid);
    }
    public void deviceSetName(String id, String name){
        dm.deviceSetName(id, name);
    }
    public void addAirData(String id, Double airt, Double airh, Double airp, String cid, Timestamp time){
        dm.addAirData(id, airt, airh, airp, cid, time);
    }
    public void addDevice(String id, String uid, String name) {
        dm.addDevice(id, uid, name);
    }
}
