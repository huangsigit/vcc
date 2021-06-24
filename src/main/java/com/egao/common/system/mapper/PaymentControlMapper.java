package com.egao.common.system.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author hs
 * @since 2020-11-11
 */
@Component
public interface PaymentControlMapper {

    List<Map<String, Object>> selectAll(Map map);

    int selectAllCount(Map map);

    List<Map<String, Object>> selectByUserId(Map map);

    int selectByUserIdCount(Map map);

    List<Map<String, Object>> selectByDateAndMerchant(@Param("purchaseRequestID") Long purchaseRequestID
            , @Param("inControlTransactionDate") String inControlTransactionDate, @Param("merchantName") String merchantName, @Param("billingAmount") String billingAmount);

    int insert(Map map);

    int deleteById(@Param("id") Long id);

}
