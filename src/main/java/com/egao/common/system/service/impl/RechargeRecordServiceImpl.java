package com.egao.common.system.service.impl;

import com.egao.common.system.mapper.RechargeRecordMapper;
import com.egao.common.system.mapper.TransferMapper;
import com.egao.common.system.service.RechargeRecordService;
import com.egao.common.system.service.TransferService;
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
public class RechargeRecordServiceImpl implements RechargeRecordService {

    @Autowired
    public RechargeRecordMapper rechargeRecordMapper;


    public List<Map<String, Object>> select(Map map){

        List<Map<String, Object>> dataList = rechargeRecordMapper.select(map);
        return dataList;
    }

    public int selectCount(Map map){
        int rechargeRecordCount = rechargeRecordMapper.selectCount(map);
        return rechargeRecordCount;
    }


    @Override
    public boolean insert(Map map){

        long count = rechargeRecordMapper.insert(map);
        if(count > 0){
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Map map){

        rechargeRecordMapper.update(map);
        return true;
    }


    @Override
    public boolean deleteById(Integer id){
        long count = rechargeRecordMapper.deleteById(id);
        boolean result = count > 0 ? true : false;
        return result;
    }

    public Map<String, Object> selectById(Integer rechargeRecordId){

        Map<String, Object> dataMap = rechargeRecordMapper.selectById(rechargeRecordId);
        return dataMap;
    }




}
