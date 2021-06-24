package com.egao.common.system.service;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author hs
 * @since 2019-10-10
 */
public interface PaymentControlService {

    List<Map<String, Object>> selectAll(Map map);

    int selectAllCount(Map map);

    List<Map<String, Object>> selectByUserId(Map map);

    int selectByUserIdCount(Map map);


    List<Map<String, Object>> selectByDateAndMerchant(Long purchaseRequestID, String inControlTransactionDate, String merchantName, String billingAmount);

    boolean insert(Map map);

    boolean deleteById(Long id);





}
