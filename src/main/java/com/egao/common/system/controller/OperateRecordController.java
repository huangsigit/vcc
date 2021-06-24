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
import com.egao.common.system.service.*;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sys/operateRecord")
public class OperateRecordController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private ApplyRecordService applyRecordService;

    @Autowired
    private RechargeRecordService rechargeRecordService;



    //    @RequiresPermissions("sys:operateRecord:view")
    @RequestMapping()
    public String view(Model model) {

        System.out.println("system/operateRecord.html");


        List<User> customerList = userService.selectCustomer(new HashMap());
        model.addAttribute("userList", JSON.toJSON(customerList));

        System.out.println("customerList："+JSONArray.toJSONString(customerList));

        // 查询某个客户下的转账记录
        HashMap<Object, Object> transferMap = new HashMap<>();
        transferMap.put("page", 0);
        transferMap.put("rows", 1000);
        List<Map<String, Object>> transferList = transferService.select(transferMap);
        System.out.println("transferList："+JSONArray.toJSONString(transferList));


        model.addAttribute("customerList", JSON.toJSONString(customerList));
        model.addAttribute("transferList", JSON.toJSONString(transferList));

        model.addAttribute("service_charge", 0);

        model.addAttribute("type", getLoginUser().getType());


        return "system/operateRecord.html";
    }



    @OperLog(value = "操作记录", desc = "分页查询")
//    @RequiresPermissions("sys:operateRecord:list")
    @ResponseBody
    @RequestMapping("/page")
    public JsonResult list(HttpServletRequest request
            , @RequestParam(name = "page", required = false)Integer page, @RequestParam(name = "limit", required = false)Integer limit
            , @RequestParam(name = "channelId", required = false)Integer channelId, @RequestParam(name = "itemsId", required = false)String itemsId
            , @RequestParam(name = "jobNumber", required = false)String jobNumber, @RequestParam(name = "adAccount", required = false)String adAccount
            , @RequestParam(name = "searchTime", required = false)String searchTime, @RequestParam(name = "adChannel", required = false)String adChannel
            , @RequestParam(name = "userId", required = false)String userIds, @RequestParam(name = "recordType", required = false, defaultValue = "0")Integer recordType) {

        PageParam pageParam = new PageParam(request);
        pageParam.setDefaultOrder(new String[]{"id"}, null);

        System.out.println("操作记录 分页查询数据...");

        System.out.println("page："+page);
        System.out.println("limit："+limit);
        System.out.println("searchTime："+searchTime);
        System.out.println("recordType："+recordType);

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
        map.put("channelId", channelId);
        map.put("jobNumber", jobNumber);
        map.put("adAccount", adAccount);
        map.put("adChannel", adChannel);
        map.put("userId", userId);
        map.put("startTime", startTime+" 00:00:00");
        map.put("endTime", endTime+ " 23:59:59");

        User user = getLoginUser();
        boolean isAdmin = user.getType() == 0 ? true : false;


        map.put("user_id", isAdmin ? userIds : user.getUserId());

        System.out.println("map:"+map);

        // 0销卡记录 1转账记录 2充值记录

        int count = 0;
        List<Map<String, Object>> list = null;
        if(recordType == 0){
            System.out.println("销卡记录:"+map);
            list = applyRecordService.select(map);
            count = list.size() > 0 ? applyRecordService.selectCount(map) : 0;

            for(Map<String, Object> maps : list){
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
//                maps.put("create_time", DateUtil.formatDate(create_time, "yyyy-MM-dd"));
                maps.put("applyTypeStr", applyType == 1 ? "销卡申请" : "充值申请");

            }

        }else
        if(recordType == 1){
            System.out.println("转账记录:"+map);
            list = transferService.select(map);
            count = list.size() > 0 ? transferService.selectCount(map) : 0;
        }else if(recordType == 2){
            System.out.println("充值记录:"+map);
            list = rechargeRecordService.select(map);
            count = list.size() > 0 ? rechargeRecordService.selectCount(map) : 0;
        }




        System.out.println("list："+list);



        if(list.size() > 0){

            for(Map<String, Object> maps : list){



/*
                Integer status = (Integer)maps.get("status");
                BigDecimal serviceCharge = (BigDecimal)maps.get("service_charge");

                BigDecimal zero = new BigDecimal(0.00);


                String statusStr = "已处理";
                if(status == 1){
                    statusStr = "已处理";
                }
                maps.put("statusStr", statusStr);
                maps.put("service_charge", serviceCharge == null ? zero : serviceCharge);
*/

            }

        }

        JsonResult data = JsonResult.ok(0, count,"成功").put("data", list);

        System.out.println("操作记录 data:"+JSONObject.toJSON(data));
        return data;
    }





    /**
     * 添加数据
     */
    @OperLog(value = "操作记录", desc = "添加数据", result = true)
    @RequiresPermissions("sys:operateRecord:add")
    @ResponseBody
    @RequestMapping("/add")
    public JsonResult add(HttpServletRequest request
            , @RequestParam(name = "userId", required = false)Integer user_id, @RequestParam(name = "transfer_amount", required = false)String transfer_amount
            , @RequestParam(name = "transfer_time", required = false)String transfer_time) {

        System.out.println("操作记录 add:"+user_id);


        try {

            if(user_id == null){
                return JsonResult.error("请选择客户");
            }

            BigDecimal zero = new BigDecimal(0.00);
//            Map<String, Object> customerMap = customerService.selectByUserId(user_id);
            User user = userService.getById(user_id);

            BigDecimal change = new BigDecimal(100.00);
//            BigDecimal serviceCharge = customerMap == null ? zero : (BigDecimal)customerMap.get("service_charge");
            BigDecimal serviceCharge = user == null ? zero : user.getServiceCharge();
            BigDecimal transferAmount = new BigDecimal(transfer_amount);


            Map map = new HashMap();
            map.put("user_id", user_id);
            map.put("transfer_amount", transferAmount);
            map.put("status", 0);
            map.put("service_charge", serviceCharge);
            map.put("actual_received_amount", transferAmount.subtract(transferAmount.multiply(serviceCharge.divide(change))));
            map.put("transfer_time", transfer_time);
            System.out.println("map："+map);

            boolean result = transferService.insert(map);
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
    @OperLog(value = "操作记录", desc = "修改数据", result = true)
    @RequiresPermissions("sys:operateRecord:update")
    @ResponseBody
    @RequestMapping("/update")
    public JsonResult update(HttpServletRequest request, @RequestParam(name = "id", required = false)Integer id
            , @RequestParam(name = "userId", required = false)Integer user_id, @RequestParam(name = "transfer_amount", required = false)String transfer_amount
            , @RequestParam(name = "transfer_time", required = false)String transfer_time) {

        System.out.println("操作记录 transfer_amount:"+transfer_amount);

        try {

            if(user_id == null){
                return JsonResult.error("请选择客户");
            }

            Map<String, Object> transferMap = transferService.selectById(id);
            System.out.println("transferMap："+transferMap);
            BigDecimal service_charge = (BigDecimal)transferMap.get("service_charge");


//            Map<String, Object> customerMap = customerService.selectByCustomerId(customer_id);
            BigDecimal change = new BigDecimal(100.00);
//            BigDecimal serviceCharge = customerMap == null ? new BigDecimal(0.00) : (BigDecimal)customerMap.get("service_charge");
            BigDecimal transferAmount = new BigDecimal(transfer_amount);




            Map map = new HashMap();
            map.put("id", id);
            map.put("user_id", user_id);
            map.put("transfer_amount", transfer_amount);
            map.put("service_charge", service_charge);
            map.put("actual_received_amount", transferAmount.subtract(transferAmount.multiply(service_charge.divide(change))));
            map.put("transfer_time", transfer_time);
            System.out.println("map："+map);



            boolean result = transferService.update(map);
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
    @OperLog(value = "操作记录", desc = "删除数据", result = true)
    @RequiresPermissions("sys:operateRecord:delete")
    @ResponseBody
    @RequestMapping("/delete")
    public JsonResult remove(Integer id) {

        System.out.println("操作记录 删除数据 card_id："+id);

        boolean result = transferService.deleteById(id);

        if (true) {
            return JsonResult.ok("删除成功");
        }
        return JsonResult.error("删除失败");
    }



}
