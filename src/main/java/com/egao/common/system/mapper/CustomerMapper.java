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
public interface CustomerMapper {

    List<Map<String, Object>> select(Map map);

    int selectCount(Map map);

    List<Map<String, Object>> selectAll(Map map);

    int insert(Map map);

    void update(Map map);

    int deleteById(@Param("id") Integer id);

    Map<String, Object> selectById(@Param("id") Integer id);

    Map<String, Object> selectByUserId(@Param("user_id") Integer user_id);

    List<Map<String, Object>> selectCustomerAmount(Map map);

    List<Map<String, Object>> selectCustomerCardCount(Map map);

    int selectCustomerCount(Map map);

    void updateCustomerRemark(Map map);


}
