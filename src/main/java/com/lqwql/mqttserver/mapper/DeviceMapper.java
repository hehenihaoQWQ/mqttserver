package com.lqwql.mqttserver.mapper;

import com.lqwql.mqttserver.entity.AirData;
import com.lqwql.mqttserver.entity.Device;
import org.apache.ibatis.annotations.Mapper;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface DeviceMapper {
    List<Device> getDeviceList(String uid);
    List<AirData> getAirData(String cid);

    void deviceSetName(String id, String name);

    void addAirData(String id, Double airt, Double airh, Double airp, String cid, Timestamp time);

    void addDevice(String id, String uid, String name);
}
