package com.egao.common.core.scheduler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.egao.common.core.Cache;
import com.egao.common.core.Constants;
import com.egao.common.core.UploadConstant;
import com.egao.common.core.utils.AnalyticsUtil;
import com.egao.common.core.utils.Base64;
import com.egao.common.core.utils.DateUtil;
import com.egao.common.core.utils.ExcelUtil;
import com.egao.common.system.service.AdService;
import com.egao.common.system.service.CertificateService;
import com.egao.common.system.service.ItemsService;
import com.egao.common.system.service.PaymentControlService;
import com.google.api.services.analytics.model.AccountSummary;
import com.google.api.services.analytics.model.ProfileSummary;
import com.google.api.services.analytics.model.WebPropertySummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;


@Component
@EnableScheduling
public class PaymentControlScheduler {

    Logger logger = Logger.getLogger(PaymentControlScheduler.class.getName());

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static String TOKEN_URL = "https://accounts.google.com/o/oauth2/iframerpc";
    public static String SITE_URL = "https://content.googleapis.com/analytics/v3/management/accountSummaries";

    @Autowired
    private PaymentControlService paymentControlService;


    private boolean environment = Constants.DEVELOPMENT_ENVIRONMENT;


    // 获取站点 每隔1小时执行一次
//    @Scheduled(cron = "1 0 * * * ?", zone = "Asia/Shanghai")
    //    @Scheduled(fixedRate = 30*60*1000)
    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void paymentControlTasks() {

        if (false) {
            return;
        }

        logger.info("paymentControlTasks startTime:" + DateUtil.timestampToTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss:SSS"));
        System.out.println("paymentControlTasks startTime:" + DateUtil.timestampToTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss:SSS"));


        try {
            com.egao.common.core.utils.ExcelUtil obj = new ExcelUtil();
            // 此处为我创建Excel路径
            File file = new File("C:\\vcc\\paymentControl2.xls");

            List excelList = obj.readExcel(file);
            System.out.println("---list中的数据打印出来："+ JSONArray.toJSONString(excelList));

            for (int i = 1; i < excelList.size(); i++) {
                List list = (List) excelList.get(i);

                if(list == null || list.size() <= 0){
                    continue;
                }

                System.out.println("list："+list);
                int zero = 1;
                String purchaseRequestID = (String)list.get(zero++);
                String vcnStatus = (String)list.get(zero++);
                String inControlTransactionDateStr = (String)list.get(zero++);
                String transactionDateStr = (String)list.get(zero++);
                String transactionType = (String)list.get(zero++);
                String transactionSobType = (String)list.get(zero++);
                System.out.println("zero："+zero);
                System.out.println("transactionSobType："+transactionSobType);
                String billingAmount = (String)list.get(zero++);
                System.out.println("zero："+zero);
                System.out.println("billingAmount："+billingAmount);

                String merchantAmount = (String)list.get(zero++);
                String merchantName = (String)list.get(zero++);
                String realCardAlias = (String)list.get(zero++);
                String virtualCardNumber = (String)list.get(zero++);

                Map map = new HashMap();
                map.put("purchaseRequestID", purchaseRequestID);
                map.put("vcnStatus", vcnStatus);
//                DateUtil.changeDateFormat(transactionDate, "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss")
                String inControlTransactionDate = DateUtil.changeDateFormat(inControlTransactionDateStr, "dd/MM/yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
                map.put("inControlTransactionDate", inControlTransactionDate); // 28/12/2020 04:46:48
//                String transactionDate = DateUtil.changeDateFormat(transactionDateStr, "dd/MM/yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
                map.put("transactionDate", inControlTransactionDate);
                map.put("transactionType", transactionType);
                map.put("transactionSobType", transactionSobType);

                String billingAmount2 = billingAmount.substring(0, billingAmount.indexOf(" "));
                String billingCurrency = billingAmount.substring(billingAmount2.length()+1);
                map.put("billingAmount", billingAmount2);
                map.put("billingCurrency", billingCurrency);

                String merchantAmount2 = merchantAmount.substring(0, merchantAmount.indexOf(" "));
                String merchantCurrency = merchantAmount.substring(merchantAmount2.length()+1);
                map.put("merchantAmount", merchantAmount2);
                map.put("merchantCurrency", merchantCurrency);

                map.put("merchantName", merchantName);
                map.put("realCardAlias", realCardAlias);
                map.put("virtualCardNumber", virtualCardNumber);

                System.out.println("map："+map);

                List<Map<String, Object>> paymentControlList = paymentControlService.selectByDateAndMerchant(Long.valueOf(purchaseRequestID), inControlTransactionDate, merchantName, billingAmount2);
                if(paymentControlList.size() <= 0){
                    paymentControlService.insert(map);
                    System.out.println("没有记录 insert map："+map);
                }else{
                    System.out.println("有记录了："+map);
                }

                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("paymentControlTasks error:" + e.getMessage());
        }


    }

}

