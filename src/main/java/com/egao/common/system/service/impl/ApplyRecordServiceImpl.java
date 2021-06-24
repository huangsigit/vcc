package com.egao.common.system.service.impl;

import com.egao.common.system.mapper.ApplyRecordMapper;
import com.egao.common.system.service.ApplyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author hs
 * @since 2020-10-10
 */
@Service
public class ApplyRecordServiceImpl implements ApplyRecordService {

    @Autowired
    public ApplyRecordMapper applyRecordMapper;


    public List<Map<String, Object>> select(Map map){

        List<Map<String, Object>> dataList = applyRecordMapper.select(map);
        return dataList;
    }

    public int selectCount(Map map){
        int applyRecordCount = applyRecordMapper.selectCount(map);
        return applyRecordCount;
    }


    @Override
    public boolean insert(Map map){

        long count = applyRecordMapper.insert(map);
        if(count > 0){
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Map map){

        applyRecordMapper.update(map);
        return true;
    }


    @Override
    public boolean deleteById(Integer id){
        long count = applyRecordMapper.deleteById(id);
        boolean result = count > 0 ? true : false;
        return result;
    }

    public Map<String, Object> selectById(Integer applyRecordId){

        Map<String, Object> dataMap = applyRecordMapper.selectById(applyRecordId);
        return dataMap;
    }




}
