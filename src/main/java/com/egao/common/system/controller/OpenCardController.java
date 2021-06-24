package com.egao.common.system.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.egao.common.core.annotation.OperLog;
import com.egao.common.core.utils.DateUtil;
import com.egao.common.core.web.BaseController;
import com.egao.common.core.web.JsonResult;
import com.egao.common.core.web.PageParam;
import com.egao.common.system.entity.Role;
import com.egao.common.system.entity.User;
import com.egao.common.system.service.CardService;
import com.egao.common.system.service.CustomerService;
import com.egao.common.system.service.OpenCardService;
import com.egao.common.system.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.tika.utils.DateUtils;
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
@RequestMapping("/sys/openCard")
public class OpenCardController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OpenCardService openCardService;


    //    @RequiresPermissions("sys:openCard:view")
    @RequestMapping()
    public String view(Model model) {

        System.out.println("system/openCard.html");

        List<User> customerList = userService.selectCustomer(new HashMap());

        System.out.println("cardList："+JSONArray.toJSONString(customerList));

        model.addAttribute("userList", JSON.toJSON(customerList));


        List<User> cardList = userService.selectCustomer(new HashMap());

        System.out.println("cardList："+JSONArray.toJSONString(cardList));

        model.addAttribute("customerList", JSON.toJSONString(cardList));

        User loginUser = getLoginUser();
        model.addAttribute("type", loginUser.getType());

        return "system/openCard.html";
    }



    @OperLog(value = "开卡申请", desc = "分页查询")
//    @RequiresPermissions("sys:openCard:list")
    @ResponseBody
    @RequestMapping("/page")
    public JsonResult list(HttpServletRequest request
            , @RequestParam(name = "page", required = false)Integer page, @RequestParam(name = "limit", required = false)Integer limit
            , @RequestParam(name = "searchTime", required = false)String searchTime, @RequestParam(name = "userId", required = false)String userIds) {

        PageParam pageParam = new PageParam(request);
        pageParam.setDefaultOrder(new String[]{"id"}, null);

        System.out.println("开卡申请 分页查询数据...");

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
        map.put("startTime", startTime+" 00:00:00");
        map.put("endTime", endTime+ " 23:59:59");

        map.put("user_id", loginUser.getType() == 0 ? userIds : loginUser.getUserId());

        System.out.println("map:"+map);

        List<Map<String, Object>> list = openCardService.select(map);
        System.out.println("list："+list);


        int count = 0;
        if(list.size() > 0){
            count = openCardService.selectCount(map);
            for(Map<String, Object> maps : list){
                Integer status = (Integer)maps.get("status");


                String statusStr = "审批中";
                if(status == 1){
                    statusStr = "已处理";
                }
                maps.put("statusStr", statusStr);

            }

        }

        JsonResult data = JsonResult.ok(0, count,"成功").put("data", list);

        System.out.println("开卡申请 data:"+JSONObject.toJSON(data));
        return data;
    }





    /**
     * 添加数据
     */
    @OperLog(value = "开卡申请", desc = "添加数据", result = true)
    @RequiresPermissions("sys:openCard:add")
    @ResponseBody
    @RequestMapping("/add")
    public JsonResult add(HttpServletRequest request
            , @RequestParam(name = "userId", required = false)Integer user_id, @RequestParam(name = "open_card_count", required = false)Integer open_card_count
            , @RequestParam(name = "init_amount", required = false)String init_amountStr) {

        System.out.println("开卡申请 add:"+user_id);

        try {


            System.out.println("openCard open_card_count:"+open_card_count);
            System.out.println("openCard init_amountStr:"+init_amountStr);


            User loginUser = getLoginUser();
            // 如果不是管理员就使用自己的id
            user_id = loginUser.getType() == 0 ? user_id : loginUser.getUserId();

/*
            if(true){
                return JsonResult.error("错误");
            }
*/


            BigDecimal zero = new BigDecimal(0.00);
            BigDecimal init_amount = new BigDecimal(init_amountStr);
            BigDecimal open_card_count_big = new BigDecimal(open_card_count);


            User user = userService.getById(user_id);

//            Map<String, Object> customerMap = customerService.selectByUserId(user_id);
            BigDecimal min_open_card_limit = new BigDecimal(0.00);
            if(user!=null){
//                min_open_card_limit = (BigDecimal)customerMap.getOrDefault("min_open_card_limit", zero);
                min_open_card_limit = user.getMinOpenCardLimit();
            }



            Map amountMap = new HashMap();
            amountMap.put("user_id", user_id);
            List<Map<String, Object>> amountList = customerService.selectCustomerAmount(amountMap);
            System.out.println("amountList："+amountList);
            Map<String, Object> amountMap2 = amountList.get(0);

            BigDecimal actual_received_amount = (BigDecimal)amountMap2.getOrDefault("actual_received_amount", zero);
            BigDecimal external_amount = (BigDecimal)amountMap2.getOrDefault("external_amount", zero);


/*
            BigDecimal hundred = new BigDecimal(100.00);
            service_charge = service_charge.divide(hundred);
            transfer_amount = transfer_amount.subtract(transfer_amount.multiply(service_charge));
*/


            BigDecimal allot_recharge_amount = actual_received_amount.subtract(external_amount);
//            BigDecimal allot_recharge_amount = (BigDecimal)amountMap2.getOrDefault("allot_recharge_amount", zero);

            System.out.println("allot_recharge_amount："+allot_recharge_amount);

            BigDecimal total_amount = init_amount.multiply(open_card_count_big);
            if(total_amount.intValue() > allot_recharge_amount.intValue()){
                return JsonResult.error("可分配充值金额不足："+allot_recharge_amount+" 实际需要金额："+total_amount);
            }


            if(init_amount.intValue() < min_open_card_limit.intValue()){
                return JsonResult.error("初始额度不能小于："+min_open_card_limit);
            }


            Map map = new HashMap();
            map.put("user_id", user_id);
            map.put("open_card_count", open_card_count);
            map.put("init_amount", init_amount);
            map.put("status", 0);
            System.out.println("map："+map);

            List<Map<String, Object>> canAllotCardList = cardService.selectCanAllotCard(new HashMap());




            List<Map<String, Object>> resultList = new ArrayList<>();

            for(Map<String, Object> canAllotCardMap : canAllotCardList){
                BigDecimal actual_amount = (BigDecimal)canAllotCardMap.getOrDefault("actual_amount", zero);
                String end_timeStr = (String)canAllotCardMap.get("end_time");

                Date end_time = DateUtil.parseDateStr(end_timeStr, "yyyy-MM");

                System.out.println("init_amount.intValue() < actual_amount.intValue()："+(init_amount.intValue() < actual_amount.intValue()));
                System.out.println("end_time.getTime() > System.currentTimeMillis()："+(end_time.getTime() > System.currentTimeMillis()));
                // 初始额度小于实际额度、到期日大于当前日期
                if(init_amount.intValue() < actual_amount.intValue() && end_time.getTime() > System.currentTimeMillis()){
                    resultList.add(canAllotCardMap);
                }
            }

            System.out.println("resultList："+resultList);

            if(open_card_count > resultList.size()){
                return JsonResult.error("库存不足，最多只能申请"+resultList.size()+"张卡");
            }

            System.out.println("resultList："+JSON.toJSONString(resultList));

            int openCardCount = 0;
            for(Map<String, Object> resultMap : resultList){
                Integer card_id = (Integer)resultMap.get("id");
                Map userCardMap = new HashMap();
                userCardMap.put("card_id", card_id);
                userCardMap.put("user_id", user_id);
                cardService.insertUserCard(userCardMap);

                Map cardMap = new HashMap();
                cardMap.put("id", card_id);
                cardMap.put("init_amount", init_amount);
//                cardMap.put("actual_amount", "");
                cardMap.put("external_amount", init_amount);
                cardMap.put("external_create_time", DateUtil.timestampToTime(System.currentTimeMillis(), "yyyy-MM-dd"));
                cardService.updateOpenCard(cardMap);

                ++openCardCount;
                if(openCardCount >= open_card_count){
                    break;
                }
            }


            boolean result = openCardService.insert(map);
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
    @OperLog(value = "开卡申请", desc = "修改数据", result = true)
    @RequiresPermissions("sys:openCard:update")
    @ResponseBody
    @RequestMapping("/update")
    public JsonResult update(HttpServletRequest request, @RequestParam(name = "id", required = false)Long id
            , @RequestParam(name = "userId", required = false)Long user_id, @RequestParam(name = "open_card_count", required = false)Integer open_card_count
            , @RequestParam(name = "init_amount", required = false)String init_amount) {

        System.out.println("开卡申请 update:"+request);

        try {


            Map map = new HashMap();
            map.put("id", id);
            map.put("user_id", user_id);
            map.put("open_card_count", open_card_count);
            map.put("init_amount", init_amount);
            System.out.println("map："+map);


            List<Map<String, Object>> canAllotCardList = cardService.selectCanAllotCard(new HashMap());
            if(open_card_count > canAllotCardList.size()){
                return JsonResult.error("库存不足，最多只能申请"+canAllotCardList.size()+"张卡");
            }

            boolean result = openCardService.update(map);
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
    @OperLog(value = "开卡申请", desc = "删除数据", result = true)
    @RequiresPermissions("sys:openCard:delete")
    @ResponseBody
    @RequestMapping("/delete")
    public JsonResult remove(long id) {

        System.out.println("开卡申请 删除数据 card_id："+id);

        boolean result = openCardService.deleteById(id);

        if (true) {
            return JsonResult.ok("删除成功");
        }
        return JsonResult.error("删除失败");
    }



}
