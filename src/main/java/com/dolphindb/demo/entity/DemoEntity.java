package com.dolphindb.demo.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class DemoEntity {
    @JSONField(name="time",format = "yyyy-MM-ddTHH:mm:ss")
    private Date time;
    @JSONField(name="id")
    private long id;
    @JSONField(name="f0")
    private float f0;
    @JSONField(name="f1")
    private float f1;
    @JSONField(name="f2")
    private float f2;
    @JSONField(name="f3")
    private float f3;
    @JSONField(name="f4")
    private float f4;
    @JSONField(name="f5")
    private float f5;
    @JSONField(name="f6")
    private float f6;
    @JSONField(name="f7")
    private float f7;
    @JSONField(name="f8")
    private float f8;
    @JSONField(name="f9")
    private float f9;

}