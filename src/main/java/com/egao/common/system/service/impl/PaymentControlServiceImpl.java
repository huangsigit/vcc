package com.egao.common.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.egao.common.core.utils.DateUtil;
import com.egao.common.system.mapper.ChannelCostMapper;
import com.egao.common.system.mapper.ChannelMapper;
import com.egao.common.system.mapper.ItemsMapper;
import com.egao.common.system.mapper.PaymentControlMapper;
import com.egao.common.system.service.ChannelCostService;
import com.egao.common.system.service.PaymentControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class PaymentControlServiceImpl implements PaymentControlService {

    private Logger logger = LoggerFactory.getLogger("PaymentControlServiceImpl");

    @Autowired
    public PaymentControlMapper paymentControlMapper;


    public List<Map<String, Object>> selectAll(Map map){

        List<Map<String, Object>> dataList = paymentControlMapper.selectAll(map);
        System.out.println("dataList:"+dataList);
        for(Map<String, Object> dataMap : dataList){
            String inControlTransactionDate = (String)dataMap.get("inControlTransactionDate");
            String transactionDate = (String)dataMap.get("transactionDate");

            dataMap.put("inControlTransactionDate", DateUtil.changeDateFormat(inControlTransactionDate, "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss"));
            dataMap.put("transactionDate", DateUtil.changeDateFormat(transactionDate, "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss"));
        }


        return dataList;
    }

    public int selectAllCount(Map map){
        int costCount = paymentControlMapper.selectAllCount(map);
        return costCount;
    }

    public List<Map<String, Object>> selectByUserId(Map map){

        List<Map<String, Object>> dataList = paymentControlMapper.selectByUserId(map);
        System.out.println("dataList:"+dataList);
        for(Map<String, Object> dataMap : dataList){
            String inControlTransactionDate = (String)dataMap.get("inControlTransactionDate");
            String transactionDate = (String)dataMap.get("transactionDate");

            dataMap.put("inControlTransactionDate", DateUtil.changeDateFormat(inControlTransactionDate, "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss"));
            dataMap.put("transactionDate", DateUtil.changeDateFormat(transactionDate, "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss"));
        }


        return dataList;
    }

    public int selectByUserIdCount(Map map){
        int costCount = paymentControlMapper.selectByUserIdCount(map);
        return costCount;
    }



    public List<Map<String, Object>> selectByDateAndMerchant(Long purchaseRequestID, String inControlTransactionDate, String merchantName, String billingAmount){
        List<Map<String, Object>> dataList = paymentControlMapper.selectByDateAndMerchant(purchaseRequestID, inControlTransactionDate, merchantName, billingAmount);
        return dataList;
    }

    @Override
    public boolean insert(Map map){
//        dates, item_id, channel_id, cost
        String dates = (String)map.get("dates");
        Long itemsId = (Long)map.get("item_id");
        Long channelId = (Long)map.get("channel_id");

/*
        Map<String, Object> channelMap = channelMapper.selectChannelById(Integer.valueOf(String.valueOf(channelId)));
        if(channelMap == null){
            System.out.println("渠道不存在："+map);
            return false;
        }
*/

        int count = paymentControlMapper.insert(map);
        if(count > 0){
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteById(Long id){
        int count = paymentControlMapper.deleteById(id);
        return count > 0 ? true : false;
    }


}
