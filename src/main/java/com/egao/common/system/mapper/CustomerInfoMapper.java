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
public interface CustomerInfoMapper {

    List<Map<String, Object>> select(Map map);

    int selectCount(Map map);

    long insert(Map map);

    void update(Map map);

    long deleteById(@Param("id") Long id);

    Map<String, Object> selectById(@Param("id") Long id);

    Map<String, Object> selectByCustomerId(@Param("customer_id") Long customer_id);


}
