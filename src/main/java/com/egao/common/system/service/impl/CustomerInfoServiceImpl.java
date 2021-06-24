package com.egao.common.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.egao.common.system.mapper.CustomerInfoMapper;
import com.egao.common.system.mapper.CustomerMapper;
import com.egao.common.system.service.CustomerInfoService;
import com.egao.common.system.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
public class CustomerInfoServiceImpl implements CustomerInfoService {

    @Autowired
    public CustomerMapper customerMapper;

    @Autowired
    public CustomerInfoMapper customerInfoMapper;


    public List<Map<String, Object>> select(Map map){

//        List<Map<String, Object>> dataList = customerInfoMapper.select(map);

        List<Map<String, Object>> customerAmountList = customerMapper.selectCustomerAmount(map);
        System.out.println("customerAmountList:"+ JSON.toJSONString(customerAmountList));
        List<Map<String, Object>> customerCardCountList = customerMapper.selectCustomerCardCount(map);
        System.out.println("customerCardCountList:"+customerCardCountList);
        for(Map<String, Object> customerAmountMap : customerAmountList){
            BigDecimal zero = new BigDecimal(0.00);
            Integer amountUserId = (Integer)customerAmountMap.get("user_id");
            BigDecimal transfer_amount = (BigDecimal)customerAmountMap.getOrDefault("transfer_amount", zero);
            BigDecimal external_amount = (BigDecimal)customerAmountMap.getOrDefault("external_amount", zero);
            BigDecimal service_charge = (BigDecimal)customerAmountMap.getOrDefault("service_charge", zero);
            BigDecimal logout_billing_amount = new BigDecimal((Double)customerAmountMap.getOrDefault("logout_billing_amount", Double.valueOf(0)));
            Double billing_amount_d = (Double)customerAmountMap.getOrDefault("billing_amount", Double.valueOf(0));


            BigDecimal hundred = new BigDecimal(100.00);
            service_charge = service_charge.divide(hundred);
            transfer_amount = transfer_amount.subtract(transfer_amount.multiply(service_charge));


            BigDecimal billing_amount = new BigDecimal(billing_amount_d).setScale(2, BigDecimal.ROUND_HALF_UP);


            System.out.println("transfer_amount："+transfer_amount);
            System.out.println("billing_amount："+billing_amount);
            System.out.println("external_amount："+external_amount);
            System.out.println("logout_billing_amount："+logout_billing_amount);
            customerAmountMap.put("remaining_credit_amount", transfer_amount.subtract(billing_amount).setScale(2, BigDecimal.ROUND_HALF_UP)); // 剩余信用额度
            customerAmountMap.put("allot_recharge_amount", transfer_amount.subtract(external_amount.subtract(logout_billing_amount)).setScale(2, BigDecimal.ROUND_HALF_UP)); // 可分配充值金额

            customerAmountMap.put("billing_amount", billing_amount); // 实际消耗总额度


            for(Map<String, Object> customerCardCountMap : customerCardCountList){
                Integer cardCountUserId = (Integer)customerCardCountMap.get("user_id");
                if(amountUserId == cardCountUserId){
                    // total_open_card_count、active_card_count
                    Long total_open_card_count = (Long)customerCardCountMap.get("total_open_card_count"); // 总开卡数量
                    Long active_card_count = (Long)customerCardCountMap.get("active_card_count"); // 总激活卡数量

                    customerAmountMap.put("total_open_card_count", total_open_card_count);
                    customerAmountMap.put("active_card_count", active_card_count);
                }


            }


        }


        return customerAmountList;
    }


    public List<Map<String, Object>> selectWarningCustomer(Map map){

        List<Map<String, Object>> customerAmountList = customerMapper.selectCustomerAmount(map);


        List<Map<String, Object>> list = new ArrayList<>();
        for(Map<String, Object> customerAmountMap : customerAmountList){
            BigDecimal zero = new BigDecimal(0.00);
            Integer amountUserId = (Integer)customerAmountMap.get("user_id");
            String customerName = (String)customerAmountMap.get("customer_name");
            BigDecimal transfer_amount = (BigDecimal)customerAmountMap.getOrDefault("transfer_amount", zero);
            BigDecimal external_amount = (BigDecimal)customerAmountMap.getOrDefault("external_amount", zero);
            BigDecimal service_charge = (BigDecimal)customerAmountMap.getOrDefault("service_charge", zero);
            BigDecimal warning_amount = (BigDecimal)customerAmountMap.get("warning_amount");
            BigDecimal logout_billing_amount = new BigDecimal((Double)customerAmountMap.getOrDefault("logout_billing_amount", Double.valueOf(0)));
//            Double billing_amount_d = (Double)customerAmountMap.getOrDefault("billing_amount", Double.valueOf(0));
            BigDecimal hundred = new BigDecimal(100.00);
            service_charge = service_charge.divide(hundred);
            transfer_amount = transfer_amount.subtract(transfer_amount.multiply(service_charge));
            BigDecimal allot_recharge_amount = transfer_amount.subtract(external_amount.subtract(logout_billing_amount)).setScale(2, BigDecimal.ROUND_HALF_UP);

//            BigDecimal billing_amount = new BigDecimal(billing_amount_d).setScale(2, BigDecimal.ROUND_HALF_UP);

            customerAmountMap.put("allot_recharge_amount", allot_recharge_amount); // 可分配充值金额

            // 可分配充值金额小于等于预警额度
            if(warning_amount!=null && allot_recharge_amount.compareTo(warning_amount) < 1){
                System.out.println("预警客户："+customerName);

                list.add(customerAmountMap);
            }
        }

        System.out.println("selectWarningCustomer list："+list);
        return list;
    }



    public int selectCount(Map map){
        int customerInfoCount = customerInfoMapper.selectCount(map);
        return customerInfoCount;
    }


    @Override
    public boolean insert(Map map){
        Long customerInfo_id = (Long)map.get("customerInfo_id");

        Map<String, Object> customerInfoMap = selectById(customerInfo_id);
        System.out.println("customerInfoMap："+customerInfoMap);

        if(customerInfoMap != null){
            return false;
        }

        long count = customerInfoMapper.insert(map);
        System.out.println("count："+count);

        if(count > 0){
            return true;
        }

        return false;
    }

    @Override
    public boolean update(Map map){

/*
        Long id = (Long)map.get("id");
        Long customerInfo_id = (Long)map.get("customerInfo_id");
        Map<String, Object> customerInfoMap = selectByCustomerId(customerInfo_id);
        System.out.println("customerInfoMap："+customerInfoMap);
        if(customerInfoMap != null){
            Long cId = (Long)customerInfoMap.get("id");
            System.out.println("cId:"+cId);
            System.out.println("id:"+id);
            if(!cId.equals(id)){
                System.out.println("不是");
                return false;
            }
        }
*/

        customerInfoMapper.update(map);
        return true;
    }


    @Override
    public boolean deleteById(Long id){
        long count = customerInfoMapper.deleteById(id);
        return count > 0 ? true : false;
    }

    public Map<String, Object> selectById(Long customerInfoId){

        Map<String, Object> dataMap = customerInfoMapper.selectById(customerInfoId);
        return dataMap;
    }

    public Map<String, Object> selectByCustomerId(Long customerInfoId){

        Map<String, Object> dataMap = customerInfoMapper.selectByCustomerId(customerInfoId);
        return dataMap;
    }


}
