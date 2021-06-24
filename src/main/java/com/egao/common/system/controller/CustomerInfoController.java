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
@RequestMapping("/sys/customerInfo")
public class CustomerInfoController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerInfoService customerInfoService;


/*
    @RequiresPermissions("sys:customerInfo:view")
    @RequestMapping()
    public String view(Model model) {

        System.out.println("system/customerInfo.html");

        List<User> customerInfoList = userService.selectCustomer(new HashMap());

        System.out.println("customerInfoList："+JSONArray.toJSONString(customerInfoList));

        model.addAttribute("customerList", JSON.toJSONString(customerInfoList));

        User loginUser = getLoginUser();
        model.addAttribute("type", loginUser.getType());

        return "system/customerInfo.html";
    }
*/


    @RequiresPermissions("sys:customerInfo:view")
    @RequestMapping()
    public String view(Model model) {

        System.out.println("system/userInfo.html");

        List<User> customerInfoList = userService.selectCustomer(new HashMap());

        System.out.println("customerInfoList："+JSONArray.toJSONString(customerInfoList));

        model.addAttribute("customerList", JSON.toJSONString(customerInfoList));

        User loginUser = getLoginUser();
        model.addAttribute("type", loginUser.getType());

        return "system/userInfo.html";
    }







    @OperLog(value = "用户信息", desc = "分页查询")
//    @RequiresPermissions("sys:customerInfo:list")
    @ResponseBody
    @RequestMapping("/page")
    public JsonResult list(HttpServletRequest request
            , @RequestParam(name = "page", required = false, defaultValue = "0")Integer page, @RequestParam(name = "limit", required = false, defaultValue = "10")Integer limit
            , @RequestParam(name = "channelId", required = false)Integer channelId, @RequestParam(name = "itemsId", required = false)String itemsId
            , @RequestParam(name = "jobNumber", required = false)String jobNumber, @RequestParam(name = "adAccount", required = false)String adAccount
            , @RequestParam(name = "searchTime", required = false)String searchTime, @RequestParam(name = "adChannel", required = false)String adChannel
            , @RequestParam(name = "user_id", required = false)Integer user_id) {

        PageParam pageParam = new PageParam(request);
        pageParam.setDefaultOrder(new String[]{"id"}, null);

        System.out.println("用户信息 分页查询数据...");

        System.out.println("page："+page);
        System.out.println("limit："+limit);
        System.out.println("searchTime："+searchTime);
        System.out.println("user_id："+user_id);


/*
        if(true){
            return JsonResult.error("开始日期不能大于结束日期...");
        }
*/


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
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("customer_id", loginUser.getType() == 0 ? "" : loginUser.getCustomerId());
        map.put("user_id", loginUser.getType() == 0 ? user_id : loginUser.getUserId());

        System.out.println("map:"+map);

        List<Map<String, Object>> list = customerInfoService.select(map);

        int count = 0;
        if(list.size() > 0){
//            count = customerInfoService.selectCount(map);
            count = customerService.selectCustomerCount(map);


            for(Map<String, Object> maps : list){
                BigDecimal zero = new BigDecimal(0.00);

                Long customerInfo_id = (Long)maps.get("customerInfo_id");
                String customerInfo_name = (String)maps.get("customerInfo_name");


                maps.put("customerInfoId", customerInfo_id);
                maps.put("customerInfoName", customerInfo_name);
                maps.put("service_charge", maps.getOrDefault("service_charge", zero));
                maps.put("total_open_card_count", maps.getOrDefault("total_open_card_count", 0));
                maps.put("active_card_count", maps.getOrDefault("active_card_count", Integer.valueOf(0)));
                maps.put("transfer_amount", maps.getOrDefault("transfer_amount", zero));
                maps.put("billing_amount", maps.getOrDefault("billing_amount", zero));
                maps.put("external_amount", maps.getOrDefault("external_amount", zero));
                maps.put("allot_recharge_amount", maps.getOrDefault("allot_recharge_amount", zero));
                maps.put("remaining_credit_amount", maps.getOrDefault("remaining_credit_amount", zero));


            }

        }



        JsonResult data = JsonResult.ok(0, count,"成功").put("data", list);

        System.out.println("渠道成本 data:"+JSONObject.toJSON(data));
        return data;
    }



    @OperLog(value = "用户信息", desc = "查询预警客户")
//    @RequiresPermissions("sys:customerInfo:list")
    @ResponseBody
    @RequestMapping("/selectWarningCustomer")
    public JsonResult selectWarningCustomer(HttpServletRequest request
            , @RequestParam(name = "page", required = false, defaultValue = "0")Integer page, @RequestParam(name = "limit", required = false, defaultValue = "10")Integer limit
            ) {

        User loginUser = getLoginUser();
        Map map = new HashMap();
        map.put("page", (page-1)*limit);
        map.put("rows", limit);
        map.put("user_id", loginUser.getUserId());

        System.out.println("map:"+map);

        List<Map<String, Object>> list = customerInfoService.selectWarningCustomer(map);



        JsonResult data = JsonResult.ok(0, list.size(),"成功").put("data", list);

        System.out.println("查询预警客户 data:"+JSONObject.toJSON(data));
        return data;
    }



    /**
     * 添加数据
     */
    @OperLog(value = "用户信息", desc = "添加数据", result = true)
    @RequiresPermissions("sys:customerInfo:add")
    @ResponseBody
    @RequestMapping("/add")
    public JsonResult add(HttpServletRequest request
            , @RequestParam(name = "customerInfoId", required = false)Long customerInfo_id, @RequestParam(name = "service_charge", required = false)String service_charge
            , @RequestParam(name = "min_open_card_limit", required = false)String min_open_card_limit, @RequestParam(name = "open_card_charge", required = false)String open_card_charge) {

        System.out.println("用户信息 add:"+request);


        try {

            Map map = new HashMap();
            map.put("customerInfo_id", customerInfo_id);
            map.put("service_charge", new BigDecimal(service_charge));
            map.put("min_open_card_limit", new BigDecimal(min_open_card_limit));
            map.put("open_card_charge", new BigDecimal(open_card_charge));
            System.out.println("map："+map);


            boolean result = customerInfoService.insert(map);
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
    @OperLog(value = "用户信息", desc = "修改数据", result = true)
    @RequiresPermissions("sys:customerInfo:update")
    @ResponseBody
    @RequestMapping("/update")
    public JsonResult update(HttpServletRequest request, @RequestParam(name = "id", required = false)Long id, @RequestParam(name = "user_id", required = false)Long user_id, @RequestParam(name = "remark", required = false)String remark) {

        System.out.println("用户信息 update:"+request);

        try {


            Map map = new HashMap();
            map.put("id", id);
            map.put("user_id", user_id);
            map.put("remark", remark);
            System.out.println("map："+map);

            boolean result = customerService.updateCustomerRemark(map);
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
    @OperLog(value = "用户信息", desc = "删除数据", result = true)
    @RequiresPermissions("sys:customerInfo:delete")
    @ResponseBody
    @RequestMapping("/delete")
    public JsonResult remove(long id) {

        System.out.println("用户信息 删除数据 customerInfo_id："+id);

        boolean result = customerInfoService.deleteById(id);

        if (true) {
            return JsonResult.ok("删除成功");
        }
        return JsonResult.error("删除失败");
    }





}
