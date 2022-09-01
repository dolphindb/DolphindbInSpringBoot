package com.dolphindb.demo.service.Impl;

import com.dolphindb.demo.dao.DemoDfsDao;
import com.dolphindb.demo.entity.DemoEntity;
import com.dolphindb.demo.service.DemoDfsService;
import com.dolphindb.demo.service.DemoStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class DemoDfsServiceImpl implements DemoDfsService {
    @Autowired
    DemoDfsDao demoDfsDao;

    @Override
    public List<DemoEntity> queryByTimespan(Date start, Date end) {
        return demoDfsDao.queryByTimespan(start,end);
    }
}
