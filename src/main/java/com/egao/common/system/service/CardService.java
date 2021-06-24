package com.egao.common.system.service;

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
public interface CardService {

    List<Map<String, Object>> select(Map map);

    int selectCount(Map map);

    Integer insert(Map map);

    boolean update(Map map);

    boolean updateCardAmount(Map map);

    void updateStatus(Integer id, Integer status);

    boolean deleteById(Integer id);

    Map<String, Object> selectById(Integer id);

    void insertUserCard(Map map);

    boolean deleteUserCardByCardId(Integer cardId);

    Map<String, Object> selectUserCardByCardId(Integer card_id);

    List<Map<String, Object>> selectCanAllotCard(Map map);

    Map<String, Object> selectExternalAmount(Map map);

    Map<String, Object> selectAllotRechargeAmount(Integer user_id);

    boolean updateOpenCard(Map map);

    List<Map<String, Object>> selectCardDetail(Map map);

    List<Map<String, Object>> selectAutoRechargeCard(Map map);
}

