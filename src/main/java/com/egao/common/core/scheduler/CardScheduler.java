package com.egao.common.core.scheduler;

import com.alibaba.fastjson.JSON;
import com.egao.common.core.Constants;
import com.egao.common.core.utils.DateUtil;
import com.egao.common.core.utils.ExcelUtil;
import com.egao.common.core.web.JsonResult;
import com.egao.common.system.service.CardService;
import com.egao.common.system.service.CustomerService;
import com.egao.common.system.service.PaymentControlService;
import com.egao.common.system.service.RechargeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@Component
@EnableScheduling
public class CardScheduler {

    Logger logger = Logger.getLogger(CardScheduler.class.getName());

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static String TOKEN_URL = "https://accounts.google.com/o/oauth2/iframerpc";
    public static String SITE_URL = "https://content.googleapis.com/analytics/v3/management/accountSummaries";

    @Autowired
    private PaymentControlService paymentControlService;

    @Autowired
    private CardService cardService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RechargeRecordService rechargeRecordService;


    private boolean environment = Constants.DEVELOPMENT_ENVIRONMENT;


    // 获取站点 每隔1小时执行一次
//    @Scheduled(cron = "1 0 * * * ?", zone = "Asia/Shanghai")
        @Scheduled(fixedRate = 10*60*1000)
//    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void autoRechargeTasks() {

        if (true) {
            return;
        }

        logger.info("autoRechargeTasks startTime:" + DateUtil.timestampToTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss:SSS"));
        System.out.println("autoRechargeTasks startTime:" + DateUtil.timestampToTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss:SSS"));

        try {

            BigDecimal zero = new BigDecimal(0.00);
            Map rechargeCardMap = new HashMap();
            List<Map<String, Object>> cardList = cardService.selectAutoRechargeCard(rechargeCardMap);
            for(Map<String, Object> cardMap : cardList){

                Integer card_id = (Integer)cardMap.get("id");
                BigDecimal recharge_amount = (BigDecimal)cardMap.getOrDefault("auto_recharge_amount", zero);

                System.out.println("cardMap:"+cardMap);
                Integer user_id = (Integer)cardMap.get("user_id");
                BigDecimal card_external_amount = (BigDecimal)cardMap.get("external_amount");
                BigDecimal init_amount = (BigDecimal)cardMap.get("init_amount");


                // 获取可分配额度=累计转账金额-对外卡额度

                Map map = new HashMap();
                map.put("user_id", user_id);
                List<Map<String, Object>> customerAmountList = customerService.selectCustomerAmount(map);

                Map<String, Object> customerAmountMap = customerAmountList.get(0);
                BigDecimal transfer_amount = (BigDecimal)customerAmountMap.getOrDefault("transfer_amount", zero);
                BigDecimal external_amount = (BigDecimal)customerAmountMap.getOrDefault("external_amount", zero);

                BigDecimal service_charge = (BigDecimal)customerAmountMap.getOrDefault("service_charge", zero);
                BigDecimal hundred = new BigDecimal(100.00);
                service_charge = service_charge.divide(hundred);
                transfer_amount = transfer_amount.subtract(transfer_amount.multiply(service_charge));
                BigDecimal logout_billing_amount = new BigDecimal((Double)customerAmountMap.getOrDefault("logout_billing_amount", Double.valueOf(0)));
                BigDecimal allot_recharge_amount = transfer_amount.subtract(external_amount.subtract(logout_billing_amount)).setScale(2, BigDecimal.ROUND_HALF_UP);


                if(recharge_amount.intValue() > allot_recharge_amount.intValue()){
//                    return JsonResult.error("充值金额不可大于可充值金额,请联系管理员");
                    System.out.println("充值金额不可大于可充值金额,请联系管理员");
                    continue;
                }


                // 修改卡额度
                map.put("id", card_id);
                map.put("init_amount", init_amount.add(recharge_amount)); // 卡初始额度 会变
                map.put("actual_amount", ""); // 实际卡额度 不会变
                map.put("external_amount", card_external_amount.add(recharge_amount)); // 对外卡额度 会变

                cardService.updateCardAmount(map);

                // 记录充值操作
                Map recordMap = new HashMap();
                recordMap.put("card_id", card_id);
                recordMap.put("s_user_id", user_id);
                recordMap.put("recharge_amount", recharge_amount);
                rechargeRecordService.insert(recordMap);

                System.out.println("充值成功 card_id："+card_id+ " user_id："+user_id);

            }




        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("autoRechargeTasks error:" + e.getMessage());
        }


    }

}

