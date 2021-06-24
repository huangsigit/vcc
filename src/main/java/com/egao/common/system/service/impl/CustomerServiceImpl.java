package com.egao.common.system.service.impl;

import com.egao.common.core.utils.DateUtil;
import com.egao.common.system.mapper.CustomerMapper;
import com.egao.common.system.mapper.PaymentControlMapper;
import com.egao.common.system.service.CustomerService;
import com.egao.common.system.service.PaymentControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    public CustomerMapper customerMapper;


    public List<Map<String, Object>> select(Map map){

        List<Map<String, Object>> dataList = customerMapper.select(map);

        return dataList;
    }

    public int selectCount(Map map){
        int customerCount = customerMapper.selectCount(map);
        return customerCount;
    }

    public List<Map<String, Object>> selectAll(Map map){

        List<Map<String, Object>> dataList = customerMapper.selectAll(map);
        return dataList;
    }


    @Override
    public boolean insert(Map map){
        Integer user_id = (Integer)map.get("user_id");

        Map<String, Object> customerMap = selectByUserId(user_id);
        System.out.println("customerMap："+customerMap);

        if(customerMap != null){
            return false;
        }

        int count = customerMapper.insert(map);
        System.out.println("count："+count);

        if(count > 0){
            return true;
        }

        return false;
    }

    @Override
    public boolean update(Map map){

        Integer id = (Integer)map.get("id");
        Integer user_id = (Integer)map.get("user_id");

        Map<String, Object> customerMap = selectByUserId(user_id);
        System.out.println("customerMap："+customerMap);
        if(customerMap != null){
            Integer cId = (Integer)customerMap.get("id");
            System.out.println("cId:"+cId);
            System.out.println("id:"+id);
            if(!cId.equals(id)){
                System.out.println("不是");
                return false;
            }
        }
        customerMapper.update(map);
        return true;
    }


    @Override
    public boolean deleteById(Integer id){
        int count = customerMapper.deleteById(id);
        return count > 0 ? true : false;
    }

    public Map<String, Object> selectById(Integer customerId){

        Map<String, Object> dataMap = customerMapper.selectById(customerId);
        return dataMap;
    }

    public Map<String, Object> selectByUserId(Integer userId){

        Map<String, Object> dataMap = customerMapper.selectByUserId(userId);
        return dataMap;
    }

    public List<Map<String, Object>> selectCustomerAmount(Map map){
        List<Map<String, Object>> dataList = customerMapper.selectCustomerAmount(map);
        System.out.println("dataList:"+dataList);

        for(Map<String, Object> dataMap : dataList){

            BigDecimal zero = new BigDecimal(0.00);
            Integer amountUserId = (Integer)dataMap.get("user_id");
            BigDecimal transfer_amount = (BigDecimal)dataMap.getOrDefault("transfer_amount", zero);
            BigDecimal external_amount = (BigDecimal)dataMap.getOrDefault("external_amount", zero);
            BigDecimal service_charge = (BigDecimal)dataMap.getOrDefault("service_charge", zero);


            BigDecimal hundred = new BigDecimal(100.00);
            service_charge = service_charge.divide(hundred);
            transfer_amount = transfer_amount.subtract(transfer_amount.multiply(service_charge));


        }





        return dataList;
    }

    public List<Map<String, Object>> selectCustomerCardCount(Map map){
        List<Map<String, Object>> dataList = customerMapper.selectCustomerCardCount(map);
        return dataList;
    }

    public int selectCustomerCount(Map map){
        int customerCount = customerMapper.selectCustomerCount(map);
        return customerCount;
    }

    @Override
    public boolean updateCustomerRemark(Map map){
        customerMapper.updateCustomerRemark(map);
        return true;
    }


}
