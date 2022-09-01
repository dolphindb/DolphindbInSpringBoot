package com.dolphindb.demo.service.Impl;

import com.dolphindb.demo.dao.DemoStreamDao;
import com.dolphindb.demo.entity.DemoEntity;
import com.dolphindb.demo.service.DemoStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DemoStreamServiceImpl implements DemoStreamService {
    @Autowired
    DemoStreamDao demoStreamDao;

    @Override
    public void insert(DemoEntity demoEntity) {
        demoStreamDao.insert(demoEntity);
    }

    @Override
    public List<DemoEntity> queryByTimespan(Date start, Date end) {
        return demoStreamDao.queryByTimespan(start,end);
    }
}
