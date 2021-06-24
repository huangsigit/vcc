package com.egao.common.system.service.impl;

import com.egao.common.system.mapper.OpenCardMapper;
import com.egao.common.system.mapper.TransferMapper;
import com.egao.common.system.service.OpenCardService;
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
public class TransferServiceImpl implements TransferService {

    @Autowired
    public TransferMapper transferMapper;


    public List<Map<String, Object>> select(Map map){

        List<Map<String, Object>> dataList = transferMapper.select(map);
        return dataList;
    }

    public int selectCount(Map map){
        int transferCount = transferMapper.selectCount(map);
        return transferCount;
    }


    @Override
    public boolean insert(Map map){

        long count = transferMapper.insert(map);
        if(count > 0){
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Map map){

        transferMapper.update(map);
        return true;
    }


    @Override
    public boolean deleteById(Integer id){
        long count = transferMapper.deleteById(id);
        boolean result = count > 0 ? true : false;
        return result;
    }

    public Map<String, Object> selectById(Integer transferId){

        Map<String, Object> dataMap = transferMapper.selectById(transferId);
        return dataMap;
    }




}
