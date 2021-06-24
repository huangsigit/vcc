package com.egao.common.system.service.impl;

import com.egao.common.system.mapper.CardMapper;
import com.egao.common.system.mapper.OpenCardMapper;
import com.egao.common.system.service.CardService;
import com.egao.common.system.service.OpenCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
public class OpenCardServiceImpl implements OpenCardService {

    @Autowired
    public OpenCardMapper openCardMapper;


    public List<Map<String, Object>> select(Map map){

        List<Map<String, Object>> dataList = openCardMapper.select(map);
        return dataList;
    }

    public int selectCount(Map map){
        int openCardCount = openCardMapper.selectCount(map);
        return openCardCount;
    }


    @Override
    public boolean insert(Map map){

        long count = openCardMapper.insert(map);
        if(count > 0){
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Map map){

        openCardMapper.update(map);
        return true;
    }


    @Override
    public boolean deleteById(Long id){
        long count = openCardMapper.deleteById(id);
        boolean result = count > 0 ? true : false;
        return result;
    }

    public Map<String, Object> selectById(Long openCardId){

        Map<String, Object> dataMap = openCardMapper.selectById(openCardId);
        return dataMap;
    }




}
