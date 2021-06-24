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
public interface CardMapper {

    List<Map<String, Object>> select(Map map);

    int selectCount(Map map);

    long insert(Map map);

    void update(Map map);

    void updateCardAmount(Map map);

    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    Integer deleteById(@Param("id") Integer id);

    Map<String, Object> selectById(@Param("id") Integer id);

    void insertUserCard(Map map);

    long deleteUserCardByCardId(@Param("card_id") Integer card_id);

    Map<String, Object> selectUserCardByCardId(@Param("card_id") Integer card_id);

    List<Map<String, Object>> selectCanAllotCard(Map map);

    Map<String, Object> selectExternalAmount(Map map);

    Map<String, Object> selectAllotRechargeAmount(@Param("user_id") Integer user_id);

    void updateOpenCard(Map map);

    List<Map<String, Object>> selectCardDetail(Map map);

    List<Map<String, Object>> selectAutoRechargeCard(Map map);

}
