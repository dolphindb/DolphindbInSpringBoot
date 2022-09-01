package com.dolphindb.demo.service;

import com.dolphindb.demo.entity.DemoEntity;

import java.util.Date;
import java.util.List;

public interface DemoStreamService {
    void insert(DemoEntity demoEntity);
    List<DemoEntity> queryByTimespan(Date start, Date end);
}
