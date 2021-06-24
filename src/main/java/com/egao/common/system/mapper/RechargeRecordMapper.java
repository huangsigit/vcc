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
public interface RechargeRecordMapper {

    List<Map<String, Object>> select(Map map);

    int selectCount(Map map);

    int insert(Map map);

    void update(Map map);

    long deleteById(@Param("id") Integer id);

    Map<String, Object> selectById(@Param("id") Integer id);



}
