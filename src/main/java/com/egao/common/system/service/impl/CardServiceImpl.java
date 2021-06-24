package com.egao.common.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.egao.common.core.web.JsonResult;
import com.egao.common.system.mapper.CardMapper;
import com.egao.common.system.mapper.CustomerInfoMapper;
import com.egao.common.system.mapper.CustomerMapper;
import com.egao.common.system.service.CardService;
import com.egao.common.system.service.CustomerInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
public class CardServiceImpl implements CardService {

    @Autowired
    public CardMapper cardMapper;

    @Autowired
    public CustomerMapper customerMapper;

    public List<Map<String, Object>> select(Map map){

        List<Map<String, Object>> dataList = cardMapper.select(map);

        return dataList;
    }

    public int selectCount(Map map){
        int cardCount = cardMapper.selectCount(map);
        return cardCount;
    }


    @Override
    public Integer insert(Map map){

        Integer user_id = (Integer)map.get("user_id");

        long count = cardMapper.insert(map);
        System.out.println("count："+count);

        Integer card_id = (Integer)map.get("id");

/*
        if(user_id != null){

            Map userCardMap = new HashMap();
            userCardMap.put("user_id", user_id);
            userCardMap.put("card_id", card_id);
            System.out.println("userCardMap："+userCardMap);
            insertUserCard(userCardMap);
        }
*/

        /*if(count > 0){
            return true;
        }
        */
        return card_id;
    }

    @Override
    public boolean update(Map map){


/*        Integer user_id = (Integer)map.get("user_id");
        Integer card_id = (Integer)map.get("id");
        // 卡片与用户进行绑定
        if(user_id != null){
            Map userCardMap = new HashMap();
            userCardMap.put("user_id", user_id);
            userCardMap.put("card_id", card_id);
            System.out.println("userCardMap："+userCardMap);
            insertUserCard(userCardMap);
        }*/

        cardMapper.update(map);
        return true;
    }

    @Override
    public boolean updateCardAmount(Map map){
        cardMapper.updateCardAmount(map);
        return true;
    }


    @Override
    public void updateStatus(Integer id, Integer status){
        cardMapper.updateStatus(id, status);
    }


    @Override
    public boolean deleteById(Integer id){
        long count = cardMapper.deleteById(id);

        boolean result = count > 0 ? true : false;

        // 解除卡片与用户的绑定
        if(result){
            deleteUserCardByCardId(id);
        }

        return result;
    }

    public Map<String, Object> selectById(Integer cardId){

        Map<String, Object> dataMap = cardMapper.selectById(cardId);
        return dataMap;
    }

    public Map<String, Object> selectAllotRechargeAmount(Integer userId){

        Map<String, Object> dataMap = cardMapper.selectAllotRechargeAmount(userId);
        return dataMap;
    }


    @Override
    public void insertUserCard(Map map){
        cardMapper.insertUserCard(map);

    }

    @Override
    public boolean deleteUserCardByCardId(Integer cardId){
        long count = cardMapper.deleteUserCardByCardId(cardId);
        return count > 0 ? true : false;
    }

    public Map<String, Object> selectUserCardByCardId(Integer cardId){
        Map<String, Object> dataMap = cardMapper.selectUserCardByCardId(cardId);
        return dataMap;
    }

    public List<Map<String, Object>> selectCanAllotCard(Map map){
        List<Map<String, Object>> dataList = cardMapper.selectCanAllotCard(map);
        return dataList;
    }

    public Map<String, Object> selectExternalAmount(Map map){
        Map<String, Object> dataMap = cardMapper.selectExternalAmount(map);
        System.out.println("selectExternalAmount dataMap:"+ JSON.toJSONString(dataMap));

        BigDecimal zero = new BigDecimal(0.00);

//        for(Map<String, Object> dataMap : dataList){
            Integer id = (Integer)dataMap.get("id");
            String customer_id = (String)dataMap.get("customer_id");
            String number = (String)dataMap.get("number");
            BigDecimal actual_amount = (BigDecimal)dataMap.getOrDefault("actual_amount", zero); // 实际卡额度
            BigDecimal external_amount = (BigDecimal)dataMap.getOrDefault("external_amount", zero); // 对卡外额度
            BigDecimal billing_amount = new BigDecimal((Double)dataMap.getOrDefault("billing_amount", Double.valueOf(0))); // 已用额度
            BigDecimal actual_remaining_amount = (BigDecimal)dataMap.getOrDefault("actual_remaining_amount", zero); // 实际剩于额度
            BigDecimal external_remaining_amount = (BigDecimal)dataMap.getOrDefault("external_remaining_amount", zero); // 对外剩于额度




        dataMap.put("customer_id", StringUtils.isEmpty(customer_id) ? "kk" : customer_id);

        String initKK = StringUtils.isEmpty(customer_id) ? "kk" : customer_id;
            dataMap.put("number", StringUtils.isEmpty(number) ? initKK+"-"+id : number);
            // 实际卡剩余额度=实际卡额度(录入)-对卡外额度
//            dataMap.put("actual_remaining_amount", actual_amount.subtract(external_amount).setScale(2, BigDecimal.ROUND_HALF_UP));
            dataMap.put("actual_remaining_amount", actual_amount.subtract(billing_amount).setScale(2, BigDecimal.ROUND_HALF_UP));
            // 对外剩余额度=对外卡额度-已用额度
            dataMap.put("external_remaining_amount", external_amount.subtract(billing_amount).setScale(2, BigDecimal.ROUND_HALF_UP));
            dataMap.put("use_amount", billing_amount.setScale(2, BigDecimal.ROUND_HALF_UP)); // 已用额度

            Integer status = (Integer)dataMap.get("status");
            String customer_name = (String)dataMap.get("customer_name");

            String statusStr = "未激活";
            if(status == 1){
                statusStr = "已激活";
            }else if(status == 2) {
                statusStr = "已注销";

                dataMap.put("actual_amount", billing_amount.setScale(2, BigDecimal.ROUND_HALF_UP));
                dataMap.put("external_amount", billing_amount.setScale(2, BigDecimal.ROUND_HALF_UP));
                dataMap.put("actual_remaining_amount", zero);
                dataMap.put("external_remaining_amount", zero);

            }else if(status == 3) {
                statusStr = "注销处理中";
            }
            dataMap.put("statusStr", statusStr);
            dataMap.put("customer_name", StringUtils.isEmpty(customer_name) ? "" : customer_name);



//        }

        return dataMap;
    }

    public String getStatusStr(Integer status, Map<String, Object> maps, BigDecimal billingAmountBig){
        String statusStr = "未激活";
        if(status == 1){
            statusStr = "已激活";
        }else if(status == 2) {
            statusStr = "已注销";


            maps.put("actual_amount", billingAmountBig.setScale(2, BigDecimal.ROUND_HALF_UP));
            maps.put("external_amount", billingAmountBig.setScale(2, BigDecimal.ROUND_HALF_UP));


        }else if(status == 3) {
            statusStr = "注销处理中";
        }
        return statusStr;
    }


    @Override
    public boolean updateOpenCard(Map map){
        cardMapper.updateOpenCard(map);
        return true;
    }

    public List<Map<String, Object>> selectCardDetail(Map map){

        List<Map<String, Object>> dataList = cardMapper.selectCardDetail(map);
        for(int i = 0; i < dataList.size(); i++){
            Map<String, Object> data = dataList.get(i);

            Double billingAmount = (Double)data.getOrDefault("billingAmount",Double.valueOf(0.00));
            BigDecimal billingAmountBig  = new BigDecimal(billingAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
            // 卡片状态
            Integer status = (Integer)data.get("status");
            String statusStr = getStatusStr(status, data, billingAmountBig);
            data.put("statusStr", statusStr);


        }

        return dataList;
    }

    public List<Map<String, Object>> selectAutoRechargeCard(Map map){

        List<Map<String, Object>> dataList = cardMapper.selectAutoRechargeCard(map);

        return dataList;
    }

}
