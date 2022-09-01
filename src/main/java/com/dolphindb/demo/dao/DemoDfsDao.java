package com.dolphindb.demo.dao;


import com.dolphindb.demo.entity.DemoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.Date;
import java.util.List;

@Mapper
public interface DemoDfsDao {
    List<DemoEntity> queryByTimespan(@Param("start") Date start, @Param("end") Date end);
}
