package com.lqwql.mqttserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@Accessors(chain = true)
@NoArgsConstructor
public class AirData {
    private String id;
    private Float airt;
    private Float airh;
    private Float airp;
    private String cid;
    private Timestamp time;
}
