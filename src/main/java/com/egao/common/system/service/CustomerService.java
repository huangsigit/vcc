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
public interface CustomerService {

    List<Map<String, Object>> select(Map map);

    int selectCount(Map map);

    List<Map<String, Object>> selectAll(Map map);

    boolean insert(Map map);

    boolean update(Map map);

    boolean deleteById(Integer id);

    Map<String, Object> selectById(Integer id);

    Map<String, Object> selectByUserId(Integer user_id);

    List<Map<String, Object>> selectCustomerAmount(Map map);

    List<Map<String, Object>> selectCustomerCardCount(Map map);

    int selectCustomerCount(Map map);

    boolean updateCustomerRemark(Map map);
}

