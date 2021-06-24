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
import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/sys/customer")
public class CustomerController extends BaseController {

    @Autowired
    private ChannelCostService channelCostService;

    @Autowired
    private UserItemService userItemService;

    @Autowired
    private UserService userService;


    @Autowired
    private CustomerService customerService;


//    @RequiresPermissions("sys:customer:view")
    @RequestMapping()
    public String view(Model model) {

        System.out.println("system/customer.html");

        List<User> customerList = userService.selectCustomer(new HashMap());

        System.out.println("customerList："+JSONArray.toJSONString(customerList));

        model.addAttribute("customerList", JSON.toJSONString(customerList));






        return "system/customer.html";
    }



    @OperLog(value = "用户信息", desc = "分页查询")
//    @RequiresPermissions("sys:customer:list")
    @ResponseBody
    @RequestMapping("/page")
    public JsonResult list(HttpServletRequest request
            , @RequestParam(name = "page", required = false)Integer page, @RequestParam(name = "limit", required = false)Integer limit
            , @RequestParam(name = "purchase_request_id", required = false)String purchase_request_id, @RequestParam(name = "card_number", required = false)String card_number
            , @RequestParam(name = "billing_currency", required = false)String billing_currency, @RequestParam(name = "vcn_status", required = false)String vcn_status
            , @RequestParam(name = "transaction_type", required = false)String transaction_type, @RequestParam(name = "merchant_name", required = false)String merchant_name
            , @RequestParam(name = "searchTime", required = false)String searchTime, @RequestParam(name = "adChannel", required = false)String adChannel) {

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
        map.put("adChannel", adChannel);
        map.put("userId", userId);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
//        map.put("customer_id", loginUser.getType() == 0 ? "" : loginUser.getCustomerId());
        map.put("user_id", loginUser.getType() == 0 ? "" : loginUser.getUserId());

        System.out.println("map:"+map);

        List<Map<String, Object>> list = customerService.select(map);

        int count = 0;
        if(list.size() > 0){
            count = customerService.selectCount(map);
            for(Map<String, Object> maps : list){
                Integer user_id = (Integer)maps.get("user_id");
                String customer_name = (String)maps.get("customer_name");
                maps.put("userId", user_id);
                maps.put("customerName", customer_name);

            }
        }

        JsonResult data = JsonResult.ok(0, count,"成功").put("data", list);

        System.out.println("用户信息 data:"+JSONObject.toJSON(data));
        return data;
    }





    /**
     * 添加数据
     */
    @OperLog(value = "用户信息", desc = "添加数据", result = true)
    @RequiresPermissions("sys:customer:add")
    @ResponseBody
    @RequestMapping("/add")
    public JsonResult add(HttpServletRequest request
            , @RequestParam(name = "userId", required = false)Integer user_id, @RequestParam(name = "service_charge", required = false)String service_charge
            , @RequestParam(name = "min_open_card_limit", required = false)String min_open_card_limit, @RequestParam(name = "open_card_charge", required = false)String open_card_charge) {

        System.out.println("用户信息 add:"+request);

        try {

            Map map = new HashMap();
            map.put("user_id", user_id);
            map.put("service_charge", new BigDecimal(service_charge));
            map.put("min_open_card_limit", new BigDecimal(min_open_card_limit));
            map.put("open_card_charge", new BigDecimal(open_card_charge));
            System.out.println("map："+map);

            boolean result = customerService.insert(map);
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
    @RequiresPermissions("sys:customer:update")
    @ResponseBody
    @RequestMapping("/update")
    public JsonResult update(HttpServletRequest request, @RequestParam(name = "id", required = false)Integer id
            , @RequestParam(name = "userId", required = false)Integer user_id, @RequestParam(name = "service_charge", required = false)String service_charge
            , @RequestParam(name = "min_open_card_limit", required = false)String min_open_card_limit, @RequestParam(name = "open_card_charge", required = false)String open_card_charge) {


        System.out.println("用户信息 update:"+request);

        try {

            System.out.println("service_charge："+service_charge);

/*
            BigDecimal service_charge_limit = new BigDecimal(0.1);
            boolean sclBoo = service_charge.compareTo(service_charge_limit) > -1;
            System.out.println("sclBoo："+sclBoo);
            if(!sclBoo){
                System.out.println("a小于b");
                return JsonResult.error("服务费不能小于0.1");
            }
*/

            Map map = new HashMap();
            map.put("id", id);
            map.put("user_id", user_id);
            map.put("service_charge", new BigDecimal(service_charge));
            map.put("min_open_card_limit", new BigDecimal(min_open_card_limit));
            map.put("open_card_charge", new BigDecimal(open_card_charge));
            System.out.println("map："+map);


            boolean result = customerService.update(map);
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
    @RequiresPermissions("sys:customer:delete")
    @ResponseBody
    @RequestMapping("/delete")
    public JsonResult remove(Integer id) {

        System.out.println("用户信息 删除数据 customer_id："+id);

        boolean result = customerService.deleteById(id);

        if (true) {
            return JsonResult.ok("删除成功");
        }
        return JsonResult.error("删除失败");
    }



    /**
     * 获取客户信息
     */
    @OperLog(value = "用户信息", desc = "获取客户信息", param = false, result = true)
//    @RequiresPermissions("sys:customer:list")
    @ResponseBody
    @RequestMapping("/getCustomer")
    public JsonResult getRevenue(HttpServletRequest request) {


        System.out.println("获取客户信息...");

        User loginUser = getLoginUser();
        HashMap map = new HashMap();
        map.put("user_id", loginUser.getType() == 0 ? "" : loginUser.getUserId());
        List<Map<String, Object>> customerList = customerService.selectAll(map);


        JsonResult data = JsonResult.ok().put("data", customerList);
        System.out.println("getCustomer data："+JSONObject.toJSON(data));
        return data;

    }





}
