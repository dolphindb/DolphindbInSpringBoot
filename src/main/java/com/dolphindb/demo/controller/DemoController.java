package com.dolphindb.demo.controller;

import com.dolphindb.demo.dto.QueryArg;
import com.dolphindb.demo.entity.DemoEntity;
import com.dolphindb.demo.service.Impl.DemoDfsServiceImpl;
import com.dolphindb.demo.service.Impl.DemoStreamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
public class DemoController {

    @Autowired
    DemoDfsServiceImpl demoDfsService;

    @Autowired
    DemoStreamServiceImpl demoStreamService;

    @PostMapping("/queryStream")
    public List<DemoEntity> queryStream(@RequestBody() QueryArg arg) {
        return demoStreamService.queryByTimespan(arg.getStartTime(), arg.getEndTime());
    }

    @PostMapping("/queryDfs")
    public List<DemoEntity> queryDfs(@RequestBody() QueryArg arg) {
        return demoDfsService.queryByTimespan(arg.getStartTime(), arg.getEndTime());
    }
}
