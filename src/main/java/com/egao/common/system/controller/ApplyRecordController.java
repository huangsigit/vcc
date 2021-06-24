package com.egao.common.system.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.egao.common.core.annotation.OperLog;
import com.egao.common.core.utils.DateUtil;
import com.egao.common.core.web.BaseController;
import com.egao.common.core.web.JsonResult;
import com.egao.common.core.web.PageParam;
import com.egao.common.system.entity.User;
import com.egao.common.system.service.CardService;
import com.egao.common.system.service.CustomerService;
import com.egao.common.system.service.ApplyRecordService;
import com.egao.common.system.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/sys/applyRecord")
public class ApplyRecordController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    @Autowired
    private CustomerService customerService;


    @Autowired
    private ApplyRecordService applyRecordService;


    //    @RequiresPermissions("sys:applyRecord:view")
    @RequestMapping()
    public String view(Model model) {

        System.out.println("system/applyRecord.html");


        List<User> customerList = userService.selectCustomer(new HashMap());
        model.addAttribute("userList", JSON.toJSON(customerList));

        System.out.println("customerList："+JSONArray.toJSONString(customerList));

/*
        // 查询某个客户下的转账记录
        HashMap<Object, Object> applyRecordMap = new HashMap<>();
        applyRecordMap.put("page", 0);
        applyRecordMap.put("rows", 1000);
        List<Map<String, Object>> applyRecordList = applyRecordService.select(applyRecordMap);
        System.out.println("applyRecordList："+JSONArray.toJSONString(applyRecordList));
        model.addAttribute("customerList", JSON.toJSONString(customerList));
        model.addAttribute("applyRecordList", JSON.toJSONString(applyRecordList));
        model.addAttribute("service_charge", 0);
*/



        // 申请类型 0充值申请 1销卡申请
        List applyList = new ArrayList();
        Map map1 = new HashMap();
        map1.put("applyId", "0");
        map1.put("applyName", "充值申请");

        Map map2 = new HashMap();
        map2.put("applyId", "1");
        map2.put("applyName", "销卡申请");

        applyList.add(map1);
        applyList.add(map2);

        model.addAttribute("applyList", JSON.toJSON(applyList));


        return "system/applyRecord.html";
    }



    @OperLog(value = "申请记录", desc = "分页查询")
//    @RequiresPermissions("sys:applyRecord:list")
    @ResponseBody
    @RequestMapping("/page")
    public JsonResult list(HttpServletRequest request
            , @RequestParam(name = "page", required = false)Integer page, @RequestParam(name = "limit", required = false)Integer limit
            , @RequestParam(name = "searchTime", required = false)String searchTime
            , @RequestParam(name = "purchase_request_id", required = false)String purchase_request_id, @RequestParam(name = "apply_type", required = false)Integer apply_type) {

        PageParam pageParam = new PageParam(request);
        pageParam.setDefaultOrder(new String[]{"id"}, null);

        System.out.println("申请记录 分页查询数据...");

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
        map.put("userId", userId);
        map.put("purchase_request_id", purchase_request_id);
        map.put("apply_type", apply_type);


        map.put("startTime", startTime+" 00:00:00");
        map.put("endTime", endTime+ " 23:59:59");


        System.out.println("map:"+map);

        List<Map<String, Object>> list = applyRecordService.select(map);
        System.out.println("list："+JSON.toJSONString(list));


        int count = 0;
        if(list.size() > 0){
            count = applyRecordService.selectCount(map);
            for(Map<String, Object> maps : list){
//                Integer accountType = (Integer)maps.get("account_type");
                Integer applyType = (Integer)maps.get("apply_type");
                Integer back_status = (Integer)maps.get("back_status");
                Integer front_status = (Integer)maps.get("front_status");
                Date create_time = (Date)maps.get("create_time");

                Integer status = (Integer)maps.get("status");


                String statusStr = "未激活";
                if(status == null){
                    statusStr = "已删除";
                }else if(status == 1){
                    statusStr = "已激活";
                }else if(status == 2) {
                    statusStr = "已注销";
                }else if(status == 3) {
                    statusStr = "注销处理中";
                }
                maps.put("statusStr", statusStr);


                maps.put("backStatusStr", back_status == 1 ? "已处理" : "未处理");

                String frontStatusStr = "审批中";
                if(front_status == 1){
                    frontStatusStr = "已充值";
                }else if(front_status == 2){
                    frontStatusStr = "已销卡";
                }
                maps.put("frontStatusStr", frontStatusStr);



                maps.put("create_time", DateUtil.formatDate(create_time, "yyyy-MM-dd"));

                maps.put("applyTypeStr", applyType == 1 ? "销卡申请" : "充值申请");

            }
        }

        JsonResult data = JsonResult.ok(0, count,"成功").put("data", list);

        System.out.println("申请记录 data:"+JSONObject.toJSON(data));
        return data;
    }





    /**
     * 添加数据
     */
    @OperLog(value = "申请记录", desc = "添加数据", result = true)
    @RequiresPermissions("sys:applyRecord:add")
    @ResponseBody
    @RequestMapping("/add")
    public JsonResult add(HttpServletRequest request
            , @RequestParam(name = "userId", required = false)Integer user_id, @RequestParam(name = "applyRecord_amount", required = false)String applyRecord_amount
            , @RequestParam(name = "applyRecord_time", required = false)String applyRecord_time) {

        System.out.println("申请记录 add:"+user_id);


        try {

            if(user_id == null){
                return JsonResult.error("请选择客户");
            }

            BigDecimal zero = new BigDecimal(0.00);
            Map<String, Object> customerMap = customerService.selectByUserId(user_id);


            BigDecimal change = new BigDecimal(100.00);
            BigDecimal serviceCharge = customerMap == null ? zero : (BigDecimal)customerMap.get("service_charge");
            BigDecimal applyRecordAmount = new BigDecimal(applyRecord_amount);





            Map map = new HashMap();
            map.put("user_id", user_id);
            map.put("applyRecord_amount", applyRecordAmount);
            map.put("status", 0);
            map.put("service_charge", serviceCharge);
            map.put("actual_received_amount", applyRecordAmount.subtract(applyRecordAmount.multiply(serviceCharge.divide(change))));
            map.put("applyRecord_time", applyRecord_time);
            System.out.println("map："+map);

            boolean result = applyRecordService.insert(map);
            if(result){
                return JsonResult.ok("添加成功");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("添加失败");
    }



    /**
     * 修改数据
     */
    @OperLog(value = "申请记录", desc = "修改数据", result = true)
    @RequiresPermissions("sys:applyRecord:update")
    @ResponseBody
    @RequestMapping("/update")
    public JsonResult update(HttpServletRequest request, @RequestParam(name = "id", required = false)Integer id, @RequestParam(name = "backStatus", required = false)Integer backStatus
            , @RequestParam(name = "userId", required = false)Integer user_id, @RequestParam(name = "applyRecord_amount", required = false)String applyRecord_amount
            , @RequestParam(name = "applyRecord_time", required = false)String applyRecord_time) {

        System.out.println("申请记录 applyRecord_amount:"+applyRecord_amount);

        try {

            if(id == null){
                return JsonResult.error("请选择");
            }


            Map map = new HashMap();
            map.put("id", id);
            map.put("purchase_request_id", null);
            map.put("apply_type", null);
            map.put("sponsor", null);
            map.put("operator", null);
            map.put("front_status", null);
            map.put("back_status", backStatus);

            System.out.println("map："+map);

            boolean result = applyRecordService.update(map);
            if(result){
                return JsonResult.ok("操作成功");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("操作失败");
    }


    /**
     * 修改数据
     */
    @OperLog(value = "申请记录", desc = "修改后端状态", result = true)
    @RequiresPermissions("sys:applyRecord:update")
    @ResponseBody
    @RequestMapping("/updateBackStatus")
    public JsonResult updateBackStatus(HttpServletRequest request, @RequestParam(name = "id", required = false)Integer id, @RequestParam(name = "backStatus", required = false)Integer backStatus
            , @RequestParam(name = "userId", required = false)Integer user_id, @RequestParam(name = "applyRecord_amount", required = false)String applyRecord_amount
            , @RequestParam(name = "applyRecord_time", required = false)String applyRecord_time) {

        System.out.println("申请记录 applyRecord_amount:"+applyRecord_amount);

        try {

            if(id == null){
                return JsonResult.error("请选择");
            }

            User loginUser = getLoginUser();

            Map map = new HashMap();
            map.put("id", id);
            map.put("purchase_request_id", null);
            map.put("apply_type", null);
            map.put("s_user_id", null);
            map.put("o_user_id", loginUser.getUserId());
            map.put("front_status", null);
            map.put("back_status", backStatus == 0 ? 1 : 0); // 后端状态 0未处理 1已处理

            System.out.println("map："+map);

            boolean result = applyRecordService.update(map);
            if(result){

                    Map<String, Object> applyRecordMap = applyRecordService.selectById(id);
                    if(applyRecordMap != null){
                        Integer card_id = (Integer)applyRecordMap.get("card_id");
                        System.out.println("card_idcard_id:"+card_id);

                        if(backStatus == 1){
                            System.out.println("注销中");
                            cardService.updateStatus(card_id, 3); // 卡片状态 0未激活 1已激活 2已注销 3注销中
                        }else{

                            System.out.println("已注销");
                            cardService.updateStatus(card_id, 2);
                        }


                    }

                return JsonResult.ok("操作成功");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonResult.error("操作失败");
    }



    /**
     * 删除数据
     */
    @OperLog(value = "申请记录", desc = "删除数据", result = true)
    @RequiresPermissions("sys:applyRecord:delete")
    @ResponseBody
    @RequestMapping("/delete")
    public JsonResult remove(Integer id) {

        System.out.println("申请记录 删除数据 card_id："+id);

        boolean result = applyRecordService.deleteById(id);

        if (true) {
            return JsonResult.ok("删除成功");
        }
        return JsonResult.error("删除失败");
    }



}
