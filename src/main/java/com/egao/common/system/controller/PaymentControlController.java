package com.egao.common.system.controller;

import cn.hutool.poi.excel.ExcelReader;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.egao.common.core.annotation.OperLog;
import com.egao.common.core.utils.CoreUtil;
import com.egao.common.core.utils.CostUtil;
import com.egao.common.core.utils.DateUtil;
import com.egao.common.core.utils.ExcelUtil;
import com.egao.common.core.web.BaseController;
import com.egao.common.core.web.JsonResult;
import com.egao.common.core.web.PageParam;
import com.egao.common.system.entity.Role;
import com.egao.common.system.entity.User;
import com.egao.common.system.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/sys/paymentControl")
public class PaymentControlController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChannelCostService channelCostService;

    @Autowired
    private UserItemService userItemService;

    @Autowired
    private PaymentControlService paymentControlService;


//    @RequiresPermissions("sys:paymentControl:view")
    @RequestMapping()
    public String view(Model model) {

        User loginUser = getLoginUser();

        System.out.println("system/paymentControl.html");

        // VCN状态
        List vcnStatusList = new ArrayList();
        if(vcnStatusList.size() == 0){
            Map map1 = new HashMap();
            map1.put("statusId", "批准");
            map1.put("statusName", "批准");

            Map map2 = new HashMap();
            map2.put("statusId", "被拒绝");
            map2.put("statusName", "被拒绝");

            vcnStatusList.add(map1);
            vcnStatusList.add(map2);
        }
        model.addAttribute("vcnStatusList", JSON.toJSON(vcnStatusList));

        // 交易类型
        List transactionTypeList = new ArrayList();
        if(transactionTypeList.size() == 0){
            Map map1 = new HashMap();
            map1.put("typeId", "授权");
            map1.put("typeName", "授权");

            Map map2 = new HashMap();
            map2.put("typeId", "冲账");
            map2.put("typeName", "冲账");

            // 只有管理员才展示
            if(loginUser.getType() == 0){
                Map map3 = new HashMap();
                map3.put("typeId", "进货退回");
                map3.put("typeName", "进货退回");
                transactionTypeList.add(map3);
            }


            transactionTypeList.add(map1);
            transactionTypeList.add(map2);
        }
        System.out.println("JSON.toJSON(transactionTypeList)："+JSON.toJSON(transactionTypeList));
        model.addAttribute("transactionTypeList", JSON.toJSON(transactionTypeList));

        List<User> userList = userService.selectCustomer(new HashMap());
        System.out.println("JSON.toJSONString(userList):"+JSON.toJSONString(userList));
        model.addAttribute("userList", JSON.toJSON(userList));

        model.addAttribute("type", loginUser.getType());

        return "system/paymentControl.html";
    }



    @OperLog(value = "付款控制", desc = "分页查询")
//    @RequiresPermissions("sys:paymentControl:list")
    @ResponseBody
    @RequestMapping("/page")
    public JsonResult list(HttpServletRequest request
            , @RequestParam(name = "page", required = false)Integer page, @RequestParam(name = "limit", required = false)Integer limit
            , @RequestParam(name = "purchase_request_id", required = false)String purchase_request_id, @RequestParam(name = "card_number", required = false)String card_number
            , @RequestParam(name = "billing_amount", required = false)String billing_amount, @RequestParam(name = "vcn_status", required = false)String vcn_status
            , @RequestParam(name = "transaction_type", required = false)String transaction_type, @RequestParam(name = "merchant_name", required = false)String merchant_name
            , @RequestParam(name = "searchTime", required = false)String searchTime, @RequestParam(name = "userId", required = false)Integer user_id) {

        PageParam pageParam = new PageParam(request);
        pageParam.setDefaultOrder(new String[]{"id"}, null);

        System.out.println("渠道成本管理 分页查询数据...");

        System.out.println("page："+page);
        System.out.println("limit："+limit);
        System.out.println("searchTime："+searchTime);

        searchTime = searchTime == null ? "" : searchTime;

        User loginUser = getLoginUser();
        Integer userId = loginUser.getUserId();

        Integer loginUserId = getLoginUserId();
        System.out.println("userId："+userId);
        System.out.println("loginUserId："+loginUserId);

        String startTime = StringUtils.substringBefore(searchTime, " - ");

        System.out.println("startTime1："+startTime);
        System.out.println("startTime11："+(System.currentTimeMillis() - 86400000*30l));
        // 获取7天前日期
//        startTime = StringUtils.isEmpty(startTime) ? DateUtil.timestampToTime(System.currentTimeMillis() - 86400000 * 7, "yyyy-MM") : startTime;
        // 获取一年前日期
        startTime = StringUtils.isEmpty(startTime) ? DateUtil.timestampToTime(System.currentTimeMillis() - 86400000*30l, "yyyy-MM-dd") : startTime;
        System.out.println("startTime2："+startTime);

        String endTime = StringUtils.substringAfter(searchTime, " - ");
        // 获取昨天日期
        endTime = StringUtils.isEmpty(endTime) ? DateUtil.timestampToTime(System.currentTimeMillis(), "yyyy-MM-dd") : endTime;

        if(StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)){
            try {

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd") ; //使用了默认的格式创建了一个日期格式化对象。

                Date startDate = dateFormat.parse(startTime); //注意:指定的字符串格式必须要与SimpleDateFormat的模式要一致。
                Date endDate = dateFormat.parse(endTime); //注意:指定的字符串格式必须要与SimpleDateFormat的模式要一致。

                if(startDate.getTime() > endDate.getTime()){
                    System.out.println("startDate.getTime()："+startDate.getTime());
                    System.out.println("endDate.getTime()："+endDate.getTime());

                    return JsonResult.error("开始日期不能大于结束日期...");
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        Map map = new HashMap();
        map.put("page", (page-1)*limit);
        map.put("rows", limit);
//        map.put("itemsId", itemsId);
        map.put("purchase_request_id", purchase_request_id);
        map.put("card_number", card_number);
        map.put("billing_amount", billing_amount);
        map.put("vcn_status", vcn_status);
        map.put("transaction_type", transaction_type);
        map.put("merchant_name", merchant_name);
        map.put("userId", userId);
        map.put("startTime", startTime+" 00:00:00");
        map.put("endTime", endTime+" 23:59:59");

        map.put("user_id", loginUser.getType() == 0 ? user_id : loginUser.getUserId());

        System.out.println("map:"+map);


        List<Map<String, Object>> costList = null;
        int costCount = 0;
        if(loginUser.getType() == 0){
            // 管理员
            costList = paymentControlService.selectAll(map);
            costCount = costList.size() > 0 ? paymentControlService.selectAllCount(map) : 0;

        }else{
            // 普通用户
            costList = paymentControlService.selectByUserId(map);
            costCount = costList.size() > 0 ? paymentControlService.selectByUserIdCount(map) : 0;
        }


        JsonResult data = JsonResult.ok(0, costCount,"成功").put("data", costList);

        System.out.println("渠道成本 data:"+JSONObject.toJSON(data));
        return data;
    }





    /**
     * 添加数据
     */
    @OperLog(value = "付款控制", desc = "添加数据", result = true)
    @RequiresPermissions("sys:paymentControl:add")
    @ResponseBody
    @RequestMapping("/add")
    public JsonResult add(HttpServletRequest request, @RequestParam(name = "month", required = false)String month, @RequestParam(name = "dates", required = false)String dates
            , @RequestParam(name = "itemsId", required = false)Long itemsId, @RequestParam(name = "channelId", required = false)Long channelId
            , @RequestParam(name = "cost", required = false)String costStr) {

        System.out.println("渠道成本管理 add:"+request);

        System.out.println("itemId:"+itemsId);
        System.out.println("channelId:"+channelId);
        System.out.println("month:"+month);
        System.out.println("adCost:"+costStr);




        try {

            BigDecimal cost = new BigDecimal(costStr);
            BigDecimal zero = new BigDecimal(0.00);
            if(cost.compareTo(zero) < 1){
                System.out.println("cost小于等于0："+cost);
                return JsonResult.error("成本不能小于等于0");
            }


            Map map = new HashMap();
            map.put("item_id", itemsId);
            map.put("channel_id", channelId);
//            map.put("month", month);
            map.put("dates", dates);
            map.put("cost", cost);

            System.out.println("map："+map);


            Map<String, Object> channelCostMap = channelCostService.selectChannelCostByItemIdAndChannelId(month, itemsId, channelId);
            if(channelCostMap != null){
                return JsonResult.error("该成本已存在");
            }



            Integer userId = getLoginUserId();
            List<Map<String, Object>> userItemList = userItemService.selectUserItemByUserId(userId);
            map.put("userItemList", userItemList);
            map.put("userId", userId);

            channelCostService.insertChannelCost(map);

            return JsonResult.ok("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("添加失败");
    }



    /**
     * 修改数据
     */
    @OperLog(value = "付款控制", desc = "修改数据", result = true)
    @RequiresPermissions("sys:paymentControl:update")
    @ResponseBody
    @RequestMapping("/update")
    public JsonResult update(HttpServletRequest request, @RequestParam(name = "cost_id", required = false)Long costId, @RequestParam(name = "channelId", required = false)Long channelId
            , @RequestParam(name = "itemsId", required = false)Long itemsId, @RequestParam(name = "dates", required = false)String dates
            , @RequestParam(name = "cost", required = false)String costStr) {

        System.out.println("成本管理 update:"+request);

        System.out.println("itemsId:"+itemsId);
        System.out.println("dates:"+dates);

        try {

            BigDecimal cost = new BigDecimal(costStr);
            BigDecimal zero = new BigDecimal(0.00);
            if(cost.compareTo(zero) < 1){
                System.out.println("adCost小于等于0："+cost);
                return JsonResult.error("成本不能小于等于0");
            }


            Map map = new HashMap();
            map.put("costId", costId);
            map.put("item_id", itemsId);
            map.put("channel_id", channelId);
            map.put("dates", dates);
            map.put("cost", cost);
            System.out.println("map："+map);


            Map<String, Object> channelCostMap = channelCostService.selectChannelCostByItemIdAndChannelId(dates, itemsId, channelId);

            if(channelCostMap != null){
                Long cost_id = (Long)channelCostMap.get("cost_id");
                System.out.println("database cost_id："+cost_id);
                System.out.println("costId："+costId);

                if(!cost_id.equals(costId)){
                    return JsonResult.error("该成本已存在");
                }
            }


            Integer userId = getLoginUserId();
            List<Map<String, Object>> userItemList = userItemService.selectUserItemByUserId(userId);
            map.put("userItemList", userItemList);
            map.put("userId", userId);
            channelCostService.updateChannelCost(map);


            return JsonResult.ok("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("添加失败");
    }


    /**
     * 删除数据
     */
    @OperLog(value = "付款控制", desc = "删除数据", result = true)
    @RequiresPermissions("sys:paymentControl:delete")
    @ResponseBody
    @RequestMapping("/delete")
    public JsonResult remove(Integer cost_id) {

        System.out.println("付款控制 删除数据cost_id："+cost_id);

//        boolean result = channelCostService.deleteByMonth(month);
//        boolean result = channelCostService.deleteChannelCostById(cost_id);


        try {
            com.egao.common.core.utils.ExcelUtil obj = new ExcelUtil();
            // 此处为我创建Excel路径：E:/zhanhj/studysrc/jxl下
            File file = new File("E:\\vcc\\paymentControl2.xls");

            List excelList = obj.readExcel(file);
//            System.out.println("---list中的数据打印出来："+ JSONArray.toJSONString(excelList));

            for (int i = 1; i < excelList.size(); i++) {
                List list = (List) excelList.get(i);

                if(list == null || list.size() <= 0){
                    continue;
                }

                int zero = 0;
                String purchaseRequestID = (String)list.get(zero++);
                String vcnStatus = (String)list.get(zero++);
                String inControlTransactionDateStr = (String)list.get(zero++);
                String transactionDateStr = (String)list.get(zero++);
                String transactionType = (String)list.get(zero++);
                String transactionSobType = (String)list.get(zero++);
                String billingAmount = (String)list.get(zero++);
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
                String transactionDate = DateUtil.changeDateFormat(transactionDateStr, "dd/MM/yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
                map.put("transactionDate", transactionDate);
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

                List<Map<String, Object>> paymentControlList = paymentControlService.selectByDateAndMerchant(Long.valueOf(purchaseRequestID)
                        , inControlTransactionDate, merchantName, billingAmount2);
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

        boolean result = true;
        System.out.println("付款控制 删除数据 result："+result);

        if (false) {
            return JsonResult.ok("删除成功");
        }
        return JsonResult.error("删除失败");
    }


}
