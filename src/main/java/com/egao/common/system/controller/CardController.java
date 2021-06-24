package com.egao.common.system.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.egao.common.core.annotation.OperLog;
import com.egao.common.core.utils.DateUtil;
import com.egao.common.core.utils.ExcelUtil;
import com.egao.common.core.web.BaseController;
import com.egao.common.core.web.JsonResult;
import com.egao.common.core.web.PageParam;
import com.egao.common.system.entity.DictionaryData;
import com.egao.common.system.entity.Role;
import com.egao.common.system.entity.User;
import com.egao.common.system.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/sys/card")
public class CardController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerInfoService customerInfoService;

    @Autowired
    private DictionaryDataService dictionaryDataService;

    @Autowired
    private ApplyRecordService applyRecordService;

    @Autowired
    private RechargeRecordService rechargeRecordService;


    //    @RequiresPermissions("sys:card:view")
    @RequestMapping()
    public String view(Model model) {

        System.out.println("system/card.html");

        List<User> customerList = userService.selectCustomer(new HashMap());


        model.addAttribute("customerList", JSON.toJSONString(customerList));

        Map userMap = new HashMap();
        userMap.put("userId", "0");
        userMap.put("customerName", "无分配");

        User user = new User();
        user.setUserId(0);
        user.setCustomerName("无分配");
        customerList.add(user);
        System.out.println("cardList："+JSONArray.toJSONString(customerList));
        model.addAttribute("userList", JSON.toJSON(customerList));

        User loginUser = getLoginUser();
        model.addAttribute("type", loginUser.getType());


        // 0未激活 1已激活 2已注销 3注销中
        List cardStatusList = new ArrayList();
        Map map1 = new HashMap();
        map1.put("statusId", "1");
        map1.put("statusName", "已激活");

        Map map2 = new HashMap();
        map2.put("statusId", "2");
        map2.put("statusName", "已注销");

        Map map3 = new HashMap();
        map3.put("statusId", "3");
        map3.put("statusName", "注销中");
        cardStatusList.add(map1);
        cardStatusList.add(map2);
        cardStatusList.add(map3);

        model.addAttribute("cardStatusList", JSON.toJSON(cardStatusList));


        List<DictionaryData> sex = dictionaryDataService.listByDictCode("sex");
        System.out.println("sex："+JSON.toJSON(sex));
        model.addAttribute("sexList", JSON.toJSON(sex));

        return "system/card.html";
    }



    @OperLog(value = "卡片管理", desc = "分页查询")
//    @RequiresPermissions("sys:card:list")
    @ResponseBody
    @RequestMapping("/page")
    public JsonResult list(HttpServletRequest request
            , @RequestParam(name = "page", required = false)Integer page, @RequestParam(name = "limit", required = false)Integer limit
            , @RequestParam(name = "purchase_request_id", required = false)String purchase_request_id, @RequestParam(name = "card_number", required = false)String card_number
            , @RequestParam(name = "cardStatus", required = false)String cardStatus, @RequestParam(name = "userId", required = false)String userIds
            , @RequestParam(name = "searchTime", required = false)String searchTime) {


        PageParam pageParam = new PageParam(request);
        pageParam.setDefaultOrder(new String[]{"id"}, null);

        System.out.println("卡片管理 分页查询数据...");

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
        map.put("purchase_request_id", purchase_request_id);
        map.put("card_number", card_number);
        map.put("status", cardStatus);
//        map.put("user_id", userIds);
        map.put("startTime", startTime);
        map.put("endTime", endTime);


//        map.put("customer_id", loginUser.getType() == 0 ? "" : loginUser.getCustomerId());
//        map.put("user_id", loginUser.getType() == 0 ? "" : loginUser.getUserId());
        map.put("user_id", loginUser.getType() == 0 ? userIds : loginUser.getUserId());

        System.out.println("map:"+map);

        List<Map<String, Object>> list = cardService.select(map);
        System.out.println("list："+JSON.toJSONString(list));


        int count = 0;
        if(list.size() > 0){
            count = cardService.selectCount(map);
            for(Map<String, Object> maps : list){

                Integer card_id = (Integer)maps.get("card_id");
                String card_name = (String)maps.get("card_name");
                Integer status = (Integer)maps.get("status");

                BigDecimal allot_recharge_amount = (BigDecimal)maps.get("allot_recharge_amount");
                BigDecimal actual_amount = (BigDecimal)maps.get("actual_amount"); // 实际额度
                BigDecimal actual_remaining_amount = (BigDecimal)maps.get("actual_remaining_amount"); // 实际剩于额度
                BigDecimal external_amount = (BigDecimal)maps.get("external_amount"); // 对外额度
                BigDecimal external_remaining_amount = (BigDecimal)maps.get("external_remaining_amount"); // 对外剩于额度
                Double billingAmount = (Double)maps.getOrDefault("billingAmount",Double.valueOf(0.00));

//                Date end_time = (Date)maps.getOrDefault("end_time", "");
                String end_time = (String)maps.get("end_time");
                Date logout_time = (Date)maps.get("logout_time");

                BigDecimal init_amount = (BigDecimal)maps.get("init_amount");
                BigDecimal total_recharge_amount = (BigDecimal)maps.get("total_recharge_amount");
                BigDecimal remaining_credit_amount = (BigDecimal)maps.get("remaining_credit_amount");

                String customer_name = (String)maps.getOrDefault("customer_name", "");






                maps.put("allot_recharge_amount", String.valueOf(allot_recharge_amount == null ? "0.00" : allot_recharge_amount));
                maps.put("actual_amount", String.valueOf(actual_amount));
                maps.put("actual_remaining_amount", String.valueOf(actual_remaining_amount));
                maps.put("external_amount", String.valueOf(external_amount));
                maps.put("external_remaining_amount", String.valueOf(external_remaining_amount));
                BigDecimal billingAmountBig  = new BigDecimal(billingAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
                maps.put("billingAmount", String.valueOf(billingAmountBig.setScale(2, BigDecimal.ROUND_HALF_UP))); // 已用额度
                maps.put("external_remaining_amount", String.valueOf(external_amount.subtract(billingAmountBig).setScale(2, BigDecimal.ROUND_HALF_UP))); // 对外剩于额度


                maps.put("init_amount", String.valueOf(init_amount));
                maps.put("total_recharge_amount", String.valueOf(total_recharge_amount));
                maps.put("remaining_credit_amount", String.valueOf(remaining_credit_amount));
                maps.put("customer_name", customer_name);
                maps.put("end_time", end_time);
                maps.put("logout_time", logout_time == null ? "" : logout_time);


                maps.put("cardId", card_id);
                maps.put("cardName", card_name);

/*
                String statusStr = "未激活";
                if(status == 1){
                    statusStr = "已激活";
                }else if(status == 2) {
                    statusStr = "已注销";


                    maps.put("actual_amount", billingAmountBig.setScale(2, BigDecimal.ROUND_HALF_UP));
                    maps.put("external_amount", billingAmountBig.setScale(2, BigDecimal.ROUND_HALF_UP));


                }else if(status == 3) {
                    statusStr = "注销处理中";
                }
*/
                String statusStr = getStatusStr(status, maps, billingAmountBig);
                maps.put("statusStr", statusStr);

            }

        }

        JsonResult data = JsonResult.ok(0, count,"成功").put("data", list).put("type", loginUser.getType());

        System.out.println("卡片管理 data:"+JSONObject.toJSON(data));
        return data;
    }

    public String getStatusStr(Integer status, Map<String, Object> maps, BigDecimal billingAmountBig){
        String statusStr = "未激活";
        if(status == 1){
            statusStr = "已激活";
        }else if(status == 2) {
            statusStr = "已注销";




        }else if(status == 3) {
            statusStr = "注销处理中";
        }
        return statusStr;
    }




    /**
     * 添加数据
     */
    @OperLog(value = "卡片管理", desc = "添加数据", result = true)
    @RequiresPermissions("sys:card:add")
    @ResponseBody
    @RequestMapping("/add")
    public JsonResult add(HttpServletRequest request
            , @RequestParam(name = "purchase_request_id", required = false)Long purchase_request_id, @RequestParam(name = "status", required = false)Integer status
            , @RequestParam(name = "init_amount", required = false)String init_amount, @RequestParam(name = "end_time", required = false)String end_time
            , @RequestParam(name = "create_time", required = false)String create_time, @RequestParam(name = "card_number", required = false)String card_number
            , @RequestParam(name = "number", required = false)String number
            , @RequestParam(name = "pin", required = false)String pin, @RequestParam(name = "remark", required = false)String remark
            , @RequestParam(name = "warning_amount", required = false)String warning_amount, @RequestParam(name = "auto_recharge_amount", required = false)String auto_recharge_amount
            , @RequestParam(name = "user_id", required = false)Integer user_id
            , @RequestParam(name = "actual_amount", required = false)String actual_amount, @RequestParam(name = "external_amount", required = false)String external_amountStr) {

        System.out.println("卡片管理 add:"+request);


        try {

            BigDecimal external_amount  = new BigDecimal(external_amountStr);
            Map map = new HashMap();
            map.put("purchase_request_id", purchase_request_id);
            map.put("status", status);
            map.put("init_amount", init_amount);
            map.put("actual_amount", actual_amount);
            map.put("external_amount", external_amount);
            map.put("create_time", create_time);
            map.put("end_time", end_time);
            map.put("pin", pin);
            map.put("number", number);
            map.put("card_number", card_number);
            map.put("remark", remark);

            map.put("warning_amount", warning_amount);
            map.put("auto_recharge_amount", auto_recharge_amount);



            map.put("user_id", user_id);
            map.put("external_create_time", DateUtil.timestampToTime(System.currentTimeMillis(), "yyyy-MM-dd"));
            System.out.println("map："+map);


            if(user_id!=null){
                BigDecimal zero  = new BigDecimal(0.00);
//                Map<String, Object> customerMap = customerService.selectByUserId(user_id);
                User user = userService.getById(user_id);

                BigDecimal min_open_card_limit = new BigDecimal(0.00);
                if(user != null){
//                    min_open_card_limit = (BigDecimal)customerMap.getOrDefault("min_open_card_limit", zero);
                    min_open_card_limit = user.getMinOpenCardLimit();
                }

//            Map<String, Object> cardMap = cardService.selectById(card_id);
//            BigDecimal external_amount = (BigDecimal)cardMap.getOrDefault("external_amount", zero);
                if(external_amount.intValue() < min_open_card_limit.intValue()){
                    return JsonResult.error("该用户最低开卡限额为："+min_open_card_limit);
                }



                Map amountMap = new HashMap();
                amountMap.put("user_id", user_id);
                List<Map<String, Object>> amountList = customerService.selectCustomerAmount(amountMap);
                System.out.println("amountList："+amountList);
                Map<String, Object> amountMap2 = amountList.get(0);

                BigDecimal transfer_amount = (BigDecimal)amountMap2.getOrDefault("transfer_amount", zero);
//            BigDecimal external_amount = (BigDecimal)amountMap2.getOrDefault("external_amount", zero);
                BigDecimal allot_recharge_amount = transfer_amount.subtract(external_amount);
//            BigDecimal allot_recharge_amount = (BigDecimal)amountMap2.getOrDefault("allot_recharge_amount", zero);

                System.out.println("allot_recharge_amount："+allot_recharge_amount);

                BigDecimal total_amount = external_amount;
                if(total_amount.intValue() > allot_recharge_amount.intValue()){
                    return JsonResult.error("可分配充值金额不足："+allot_recharge_amount+" 实际需要金额："+total_amount);
                }
            }




            Integer card_id = cardService.insert(map);

            if(user_id!=null){
                Map cardMap = new HashMap();
                cardMap.put("user_id", user_id);
                cardMap.put("card_id", card_id);
                cardService.insertUserCard(cardMap);
            }

            System.out.println("map："+map);

/*
            Map<String, Object> userCardMap = cardService.selectUserCardByCardId(card_id);
            if(userCardMap != null){
                return JsonResult.error("卡片已分配");
            }
*/



            return JsonResult.ok("添加成功");


        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("添加失败");
    }



    /**
     * 修改数据
     */
    @OperLog(value = "卡片管理", desc = "修改数据", result = true)
    @RequiresPermissions("sys:card:update")
    @ResponseBody
    @RequestMapping("/update")
    public JsonResult update(HttpServletRequest request, @RequestParam(name = "id", required = false)Integer id
            , @RequestParam(name = "purchase_request_id", required = false)Long purchase_request_id, @RequestParam(name = "status", required = false)Integer status
            , @RequestParam(name = "init_amount", required = false)String init_amount, @RequestParam(name = "end_time", required = false)String end_time
            , @RequestParam(name = "create_time", required = false)String create_time, @RequestParam(name = "external_create_time", required = false)String external_create_time
            , @RequestParam(name = "logout_time", required = false)String logout_time
            , @RequestParam(name = "number", required = false)String number, @RequestParam(name = "card_number", required = false)String card_number
            , @RequestParam(name = "pin", required = false)String pin, @RequestParam(name = "remark", required = false)String remark
            , @RequestParam(name = "warning_amount", required = false)String warning_amount, @RequestParam(name = "auto_recharge_amount", required = false)String auto_recharge_amount
            , @RequestParam(name = "user_id", required = false)Integer user_id
            , @RequestParam(name = "actual_amount", required = false)String actual_amountStr, @RequestParam(name = "external_amount", required = false)String external_amountStr) {

        System.out.println("卡片管理 update:"+request);

        logger.warning("card update start:"+System.currentTimeMillis());

        try {

            BigDecimal external_amount  = new BigDecimal(external_amountStr);
            BigDecimal actual_amount  = new BigDecimal(actual_amountStr);
            if(status != null && status == 2){
                Map<String, Object> cardMap = cardService.selectById(id);
                actual_amount = (BigDecimal)cardMap.get("actual_amount");
                external_amount = (BigDecimal)cardMap.get("external_amount");
            }

            Map map = new HashMap();
            map.put("id", id);
            map.put("purchase_request_id", purchase_request_id);
            map.put("status", status);
            map.put("init_amount", init_amount);
            map.put("actual_amount", actual_amount);
            map.put("external_amount", external_amount);
            map.put("create_time", create_time);
            map.put("external_create_time", external_create_time);
            map.put("logout_time", logout_time);
            map.put("end_time", end_time);
            map.put("pin", pin);
            map.put("number", number);
            map.put("card_number", card_number);
            map.put("remark", remark);
            map.put("warning_amount", warning_amount);
            map.put("auto_recharge_amount", auto_recharge_amount);
            map.put("user_id", user_id);
//            map.put("logout_time", "");
            System.out.println("map："+JSON.toJSONString(map));

            logger.warning("card update map:"+JSON.toJSONString(map));

            if(user_id!=null){
                BigDecimal zero  = new BigDecimal(0.00);
//                Map<String, Object> customerMap = customerService.selectByUserId(user_id);

                if(status != null && status != 2){ // 注销状态不判断开卡限额
                    User user = userService.getById(user_id);
                    BigDecimal min_open_card_limit = new BigDecimal(0.00);
                    if(user != null){
                        min_open_card_limit = user.getMinOpenCardLimit();
                    }
//            Map<String, Object> cardMap = cardService.selectById(card_id);
//            BigDecimal external_amount = (BigDecimal)cardMap.getOrDefault("external_amount", zero);
                    if(external_amount.intValue() < min_open_card_limit.intValue()){
                        return JsonResult.error("该用户最低开卡限额为："+min_open_card_limit);
                    }
                }




                Map amountMap = new HashMap();
                amountMap.put("user_id", user_id);
                List<Map<String, Object>> amountList = customerService.selectCustomerAmount(amountMap);
                System.out.println("amountList："+amountList);
                Map<String, Object> amountMap2 = amountList.get(0);

                BigDecimal transfer_amount = (BigDecimal)amountMap2.getOrDefault("transfer_amount", zero);
//            BigDecimal external_amount = (BigDecimal)amountMap2.getOrDefault("external_amount", zero);
                BigDecimal allot_recharge_amount = transfer_amount.subtract(external_amount);
//            BigDecimal allot_recharge_amount = (BigDecimal)amountMap2.getOrDefault("allot_recharge_amount", zero);

                System.out.println("allot_recharge_amount："+allot_recharge_amount);

                BigDecimal total_amount = external_amount;
                if(total_amount.intValue() > allot_recharge_amount.intValue()){
                    return JsonResult.error("可分配充值金额不足："+allot_recharge_amount+" 实际需要金额："+total_amount);
                }

                cardService.deleteUserCardByCardId(id);

                Map cardMap = new HashMap();
                cardMap.put("user_id", user_id);
                cardMap.put("card_id", id);
                cardService.insertUserCard(cardMap);
            }else{
                cardService.deleteUserCardByCardId(id);
            }


            boolean result = cardService.update(map);
            if(result){
                return JsonResult.ok("修改成功");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("修改失败");
    }



    /**
     * 删除数据
     */
    @OperLog(value = "卡片管理", desc = "删除数据", result = true)
    @RequiresPermissions("sys:card:delete")
    @ResponseBody
    @RequestMapping("/delete")
    public JsonResult remove(Integer id) {

        System.out.println("卡片管理 删除数据 card_id："+id);

        boolean result = cardService.deleteById(id);

        if (result) {
            return JsonResult.ok("删除成功");
        }
        return JsonResult.error("删除失败");
    }

    /**
     * 分配卡片
     */
    @OperLog(value = "卡片管理", desc = "分配卡片", result = true)
    @RequiresPermissions("sys:card:update")
    @ResponseBody
    @RequestMapping("/allot")
    public JsonResult allot(HttpServletRequest request, @RequestParam(name = "id", required = false)Integer id
            , @RequestParam(name = "userId", required = false)Integer user_id
            , @RequestParam(name = "statusStr", required = false)Integer status) {

        System.out.println("分配卡片 allot:"+request);

        try {

            BigDecimal zero  = new BigDecimal(0.00);
            Map<String, Object> customerMap = customerService.selectByUserId(user_id);

            BigDecimal min_open_card_limit = new BigDecimal(0.00);
            if(customerMap != null){
                min_open_card_limit = (BigDecimal)customerMap.getOrDefault("min_open_card_limit", zero);
            }



            Map<String, Object> cardMap = cardService.selectById(id);
            BigDecimal external_amount = (BigDecimal)cardMap.getOrDefault("external_amount", zero);
            if(external_amount.intValue() < min_open_card_limit.intValue()){
                return JsonResult.error("该用户最低开卡限额为："+min_open_card_limit);
            }


            Map map = new HashMap();
            map.put("user_id", user_id);
            map.put("card_id", id);
            map.put("status", status);
            System.out.println("map："+map);

            Map<String, Object> userCardMap = cardService.selectUserCardByCardId(id);
            if(userCardMap != null){
                return JsonResult.error("卡片已分配");
            }


            cardService.insertUserCard(map);

            return JsonResult.ok("修改成功");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("修改失败");
    }



    /**
     * 注销卡片
     */
    @OperLog(value = "卡片管理", desc = "注销卡片", result = true)
    @RequiresPermissions("sys:card:update")
    @ResponseBody
    @RequestMapping("/logout")
    public JsonResult logout(HttpServletRequest request, @RequestParam(name = "id", required = false)Integer id) {

        System.out.println("注销卡片 logout:"+id);

        try {

            User loginUser = getLoginUser();
            List<Role> roles = loginUser.getRoles();
            Integer type = loginUser.getType();
            Role role = roles.get(0);
            Integer roleId = role.getRoleId();

//            Map<String, Object> userCardMap = cardService.selectUserCardByCardId(id);
            Map<String, Object> cardMap = cardService.selectById(id);


            System.out.println("roleId："+roleId);
            System.out.println("cardMap："+cardMap);

            // 管理员点击，则走销卡流程，如果普通用户点击，则发送申请给管理员，管理员走销卡流程
            Integer initStatus = type == 0 ? 2 : 3;

            Long purchase_request_id = null;
            if(cardMap != null){
                Integer status = (Integer)cardMap.get("status");
                purchase_request_id = (Long)cardMap.get("purchase_request_id");
                System.out.println("status："+status);
                // 注销中而且还是普通用户
                if(status == 3 && type > 0){ // 普通用户
                    return JsonResult.error("请勿重复提交");
                }else if(status == 2){ // 管理员
                    return JsonResult.error("已注销");
                }
            }


            // 卡片状态 0未激活 1已激活 2已注销 3注销审批中
//            cardService.updateStatus(id, initStatus);


            Map map = new HashMap();
            map.put("id", id);
            map.put("purchase_request_id", "");
            map.put("status", initStatus);
            map.put("init_amount", "");
            map.put("create_time", "");
            map.put("end_time", "");
            map.put("pin", "");
            map.put("card_number", "");
            map.put("remark", "");
            map.put("warning_amount", "");
            map.put("auto_recharge_amount", "");


            map.put("customer_id", null);
            map.put("logout_time", initStatus == 2 ? DateUtil.timestampToTime(System.currentTimeMillis(), "yyyy-MM-dd") : "");
            cardService.update(map);

            // type 0管理员 1普通用户
            boolean isAdmin = type == 0 ? true : false;
            System.out.println("isAdmin："+isAdmin);

            Map applyMap = new HashMap();
            applyMap.put("purchase_request_id", purchase_request_id);
            applyMap.put("apply_type", 1); // '申请类型 0充值申请 1销卡申请'

//            applyMap.put("account_type", type == 0 ? 0 : 1); // '账号类型 0管理员 1普通用户'
//            applyMap.put("account_type", type == 0 ? 0 : 1); // '账号类型 0管理员 1普通用户'
            applyMap.put("s_user_id", getLoginUserId());
            applyMap.put("card_id", id);
            applyMap.put("o_user_id", isAdmin ? getLoginUserId() : null);
            applyMap.put("front_status", isAdmin ? 2 : 0); // '前端状态 0审批中 1已充值 2已销卡'
            applyMap.put("back_status", isAdmin ? 1 : 0); // '后端状态 0未处理 1已处理'

            System.out.println("map："+map);
            System.out.println("applyMap："+applyMap);

            boolean result = applyRecordService.insert(applyMap);

            return JsonResult.ok(type == 0 ? "注销成功" : "注销处理中");


        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("注销失败");
    }



    /**
     * 充值
     */
    @OperLog(value = "卡片管理", desc = "充值", result = true)
    @RequiresPermissions("sys:card:update")
    @ResponseBody
    @RequestMapping("/recharge")
    public JsonResult recharge(HttpServletRequest request, @RequestParam(name = "id", required = false)Integer id
            , @RequestParam(name = "recharge_amount", required = false)String recharge_amountStr) {

        System.out.println("充值 id:"+id);

        try {

            BigDecimal recharge_amount = new BigDecimal(recharge_amountStr);


            Map<String, Object> cardMap = cardService.selectById(id);
            System.out.println("cardMap:"+cardMap);
            Integer user_id = (Integer)cardMap.get("user_id");
            BigDecimal card_external_amount = (BigDecimal)cardMap.get("external_amount");
            BigDecimal init_amount = (BigDecimal)cardMap.get("init_amount");


            // 获取可分配额度=累计转账金额-对外卡额度

            Map map = new HashMap();
            map.put("user_id", user_id);
            List<Map<String, Object>> customerAmountList = customerService.selectCustomerAmount(map);
            System.out.println("customerAmountList8888888888："+JSON.toJSONString(customerAmountList));

            BigDecimal zero = new BigDecimal(0.00);
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
                System.out.println("recharge_amount000000："+recharge_amount);
                return JsonResult.error("充值金额不可大于可充值金额,请联系管理员");
            }

            if(true){
                System.out.println("recharge_amount11111111："+recharge_amount);
            }

//            Map map = new HashMap();
            map.put("id", id);
            map.put("init_amount", init_amount.add(recharge_amount)); // 卡初始额度 会变
            map.put("actual_amount", ""); // 实际卡额度 不会变
            map.put("external_amount", card_external_amount.add(recharge_amount)); // 对外卡额度 会变
            System.out.println("map："+map);

            cardService.updateCardAmount(map);

            Map recordMap = new HashMap();
            recordMap.put("card_id", id);
            recordMap.put("s_user_id", getLoginUserId());
            recordMap.put("recharge_amount", recharge_amount);

            rechargeRecordService.insert(recordMap);

            return JsonResult.ok("充值成功");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("充值失败");
    }


    @OperLog(value = "卡片管理", desc = "获取卡片详情")
    @RequiresPermissions("sys:card:list")
    @ResponseBody
    @RequestMapping("/getCardInfo")
    public JsonResult getCardInfo(HttpServletRequest request
            , @RequestParam(name = "id", required = false)Long id) {

        PageParam pageParam = new PageParam(request);
        pageParam.setDefaultOrder(new String[]{"id"}, null);

        System.out.println("获取卡片详情 id："+id);


        Map map = new HashMap();
        map.put("id", id);


        System.out.println("map:"+map);

        Map<String, Object> resultMap = cardService.selectExternalAmount(map);
        Integer user_id = (Integer)resultMap.get("user_id");
        map.put("user_id", user_id);

/*
        Map<String, Object> customerMap = customerInfoService.selectByCustomerId(customer_id);
        BigDecimal allot_recharge_amount = new BigDecimal(0);
        if(customerMap!=null){
            allot_recharge_amount = (BigDecimal)customerMap.get("allot_recharge_amount");
        }
*/



        // 获取可分配额度=累计转账金额-对外卡额度
        Map<String, Object> allotRechargeAmountMap = cardService.selectAllotRechargeAmount(user_id);
        System.out.println("allotRechargeAmountMap："+JSON.toJSONString(allotRechargeAmountMap));

        List<Map<String, Object>> customerAmountList = customerService.selectCustomerAmount(map);
        System.out.println("customerAmountList8888888888："+JSON.toJSONString(customerAmountList));

        BigDecimal allot_recharge_amount = new BigDecimal(0.00);
        if(customerAmountList.size() > 0){
//            BigDecimal transfer_amount = (BigDecimal)allotRechargeAmountMap.get("transfer_amount");
//            BigDecimal external_amount = (BigDecimal)allotRechargeAmountMap.get("external_amount");

            BigDecimal zero = new BigDecimal(0.00);
            Map<String, Object> customerAmountMap = customerAmountList.get(0);
            BigDecimal transfer_amount = (BigDecimal)customerAmountMap.getOrDefault("transfer_amount", zero);
            BigDecimal external_amount = (BigDecimal)customerAmountMap.getOrDefault("external_amount", zero);

            BigDecimal service_charge = (BigDecimal)customerAmountMap.getOrDefault("service_charge", zero);
            BigDecimal hundred = new BigDecimal(100.00);
            service_charge = service_charge.divide(hundred);
            transfer_amount = transfer_amount.subtract(transfer_amount.multiply(service_charge));

            System.out.println("transfer_amount:"+transfer_amount);
            System.out.println("external_amount:"+external_amount);

            if(allotRechargeAmountMap != null){
                System.out.println("allotRechargeAmountMap.getOrDefault:"+allotRechargeAmountMap.get("logout_time"));

                Date logout_time = (Date)allotRechargeAmountMap.getOrDefault("logout_time", null);

                allotRechargeAmountMap.put("logout_time", logout_time == null ? "" : logout_time);

//            allot_recharge_amount = transfer_amount.subtract(external_amount);
//            System.out.println("allot_recharge_amount："+allot_recharge_amount);

                allotRechargeAmountMap.put("external_amount", external_amount.setScale(2, BigDecimal.ROUND_HALF_UP));
            }





            BigDecimal logout_billing_amount = new BigDecimal((Double)customerAmountMap.getOrDefault("logout_billing_amount", Double.valueOf(0)));
            allot_recharge_amount = transfer_amount.subtract(external_amount.subtract(logout_billing_amount)).setScale(2, BigDecimal.ROUND_HALF_UP);
            customerAmountMap.put("allot_recharge_amount", allot_recharge_amount); // 可分配充值金额
        }

        resultMap.put("allot_recharge_amount", allot_recharge_amount);



        Date logout_time = (Date)resultMap.get("logout_time");
        resultMap.put("logout_time", logout_time == null ? "" : logout_time);

        User loginUser = getLoginUser();
        resultMap.put("type", loginUser.getType());


        BigDecimal warning_amount = (BigDecimal)resultMap.get("warning_amount");
        BigDecimal auto_recharge_amount = (BigDecimal)resultMap.get("auto_recharge_amount");
        resultMap.put("warning_amount", warning_amount == null ? "" : warning_amount);
        resultMap.put("auto_recharge_amount", auto_recharge_amount == null ? "" : auto_recharge_amount);


        System.out.println("resultMap："+resultMap);

        System.out.println("loginUser："+loginUser.getType());
        JsonResult data = JsonResult.ok(0, 1,"成功").put("data", resultMap);

        System.out.println("卡片管理 data:"+JSONObject.toJSON(data));
        return data;
    }


    @OperLog(value = "卡片管理", desc = "导出到excel")
    @RequiresPermissions("sys:card:list")
    @ResponseBody
    @RequestMapping("/exportExcel")
    public void exportExcel(HttpServletRequest request,HttpServletResponse response
            ,@RequestParam(name = "page", required = false)Integer page, @RequestParam(name = "limit", required = false)Integer limit
            ,@RequestParam(name = "purchase_request_id", required = false)String purchase_request_id, @RequestParam(name = "card_number", required = false)String card_number
            , @RequestParam(name = "cardStatus", required = false)String cardStatus, @RequestParam(name = "user_id", required = false)String user_id) {

        System.out.println("exportExcel 导出："+user_id);

        User loginUser = getLoginUser();
        Integer type = loginUser.getType();

        Map map = new HashMap();
        map.put("page", (page-1)*limit);
        map.put("rows", limit);
        map.put("purchase_request_id", purchase_request_id);
        map.put("card_number", card_number);
        map.put("status", cardStatus);
        map.put("user_id", loginUser.getType() == 0 ? user_id : loginUser.getUserId());

        System.out.println("map:"+map);

//        List<Map<String, Object>> list = cardService.select(map);
        List<Map<String, Object>> list = cardService.selectCardDetail(map);
        System.out.println("exportExcel list："+JSON.toJSONString(list));


//        String data = json.toString();

//        Map<String,Object> map = getParameterNames(request);


        //把要导出到excel的数据的LinkedHashMap装载到这个List里面,这是导出工具类要求封装格式.
        List<Map<String, Object>> exportData = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            Map<String, Object> data = list.get(i);

            //使用LinkedHashMap,因为这个是有序的map
            LinkedHashMap<String,Object> reportData = new LinkedHashMap<>();

            BigDecimal zero = new BigDecimal(0.00);
            Double billingAmount = (Double)data.getOrDefault("billingAmount",Double.valueOf(0.00));
            BigDecimal billingAmountBig  = new BigDecimal(billingAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
            String customer_id = (String)data.get("customer_id");
            Integer id = (Integer)data.get("id");
            String number = (String)data.get("number");
            BigDecimal actual_amount = (BigDecimal)data.getOrDefault("actual_amount", zero); // 实际卡额度
            BigDecimal external_amount = (BigDecimal)data.getOrDefault("external_amount", zero); // 对卡外额度

            //装载数据,就是要导出到excel的数据
            reportData.put("purchase_request_id",data.getOrDefault("purchase_request_id", ""));

            // 卡片状态
            Integer status = (Integer)data.get("status");
            String statusStr = getStatusStr(status, reportData, billingAmountBig);
            reportData.put("statusStr", statusStr);



            // 已用额度

            reportData.put("billingAmount", String.valueOf(billingAmountBig.setScale(2, BigDecimal.ROUND_HALF_UP)));
            // 卡号
            reportData.put("card_number",data.getOrDefault("card_number", ""));

            reportData.put("end_time",data.getOrDefault("end_time", ""));



            // 外部开卡日期
            reportData.put("external_create_time",data.getOrDefault("external_create_time", ""));
            if(type == 0){
                // 内部开卡日期
                reportData.put("create_time",data.getOrDefault("create_time", ""));
                // 分配用户
                reportData.put("customer_name",data.getOrDefault("customer_name", ""));
            }



            reportData.put("pin",data.getOrDefault("pin", ""));
            // 销卡日期
            reportData.put("logout_time",data.getOrDefault("logout_time", ""));
            String initKK = StringUtils.isEmpty(customer_id) ? "kk" : customer_id;
            // 编号
            reportData.put("number", StringUtils.isEmpty(number) ? initKK+"-"+id : number);



            if(type==0){
                // 实际卡额度
                reportData.put("actual_amount",status == 2 ? billingAmountBig.setScale(2, BigDecimal.ROUND_HALF_UP) : actual_amount);
                // 实际卡剩余额度=实际卡额度(录入)-对卡外额度
                reportData.put("actual_remaining_amount", status == 2 ? zero : actual_amount.subtract(billingAmountBig).setScale(2, BigDecimal.ROUND_HALF_UP));
            }

            // 对外卡额度
            reportData.put("external_amount",status == 2 ? billingAmountBig.setScale(2, BigDecimal.ROUND_HALF_UP) : data.getOrDefault("external_amount", ""));
            // 对外剩余额度=对外卡额度-已用额度
            reportData.put("external_remaining_amount", status == 2 ? zero :external_amount.subtract(billingAmountBig).setScale(2, BigDecimal.ROUND_HALF_UP));

            reportData.put("remark", String.valueOf(data.get("remark")));

            exportData.add(reportData);
        }




        //表格列名用ArrayList装载
        List<String> columns = new ArrayList<>();
        //设置excel表格中的列名
        columns.add("请求ID");
        columns.add("卡片状态");

        columns.add("已用额度");
        columns.add("卡号");
        columns.add("到期日");

        if(type == 0){
            columns.add("外部开卡日");
            columns.add("内部开卡日");
            columns.add("分配用户");
        }else{
            columns.add("开卡日");
        }

        columns.add("PIN");
        columns.add("销卡日期");
        columns.add("编号");

        if(type==0){
            columns.add("实际卡额度");
            columns.add("实际卡剩于额度");
            columns.add("对外卡额度");
            columns.add("对外剩于额度");
        }else{
            columns.add("卡额度");
            columns.add("剩于额度");
        }



        columns.add("备注");

        //点击导出按钮的时候,页面上显示的标题,同时也是sheet的名称
        String filename ="card";
        try {
            //处理一下中文乱码问题
            response.setHeader("Content-Disposition", "attachment;filename="+new String(filename.getBytes("gb2312"), "ISO8859-1")+".xls");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //以上均为数据准备,下面开始调用导出excel工具类
        ExcelUtil.exportToExcel(response, exportData, filename, columns);

        System.out.println("export success");
//            renderState(true);
        return;




    }

}
