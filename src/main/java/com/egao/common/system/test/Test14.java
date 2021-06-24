package com.egao.common.system.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.egao.common.core.utils.AnalyticsUtil;
import com.egao.common.core.utils.Base64;
import com.egao.common.core.utils.DateUtil;
import com.egao.common.core.utils.ExcelUtil;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.tika.utils.DateUtils;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Test14 {

    public static void main(String[] args) throws Exception {


/*
        String billingAmount = "92.64 USD";
        String billingAmount2 = billingAmount.substring(0, billingAmount.indexOf(" "));
        System.out.println("billingAmount2："+billingAmount2);

        String billingCurrency = billingAmount.substring(billingAmount2.length()+1, billingAmount.length());
        System.out.println("billingCurrency："+billingCurrency);
*/

        String str = "02/12/2020 23:03:25";
        String str2 = DateUtil.changeDateFormat(str, "dd/MM/yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
        System.out.println("str2："+str2);
//        String str2 = "2020/12/01 23:03:25";
        String str3 = DateUtil.changeDateFormat(str2, "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss");
        System.out.println("str3："+str3);

        com.egao.common.core.utils.ExcelUtil obj = new ExcelUtil();
        // 此处为我创建Excel路径：E:/zhanhj/studysrc/jxl下
        File file = new File("E:\\vcc\\paymentControl.xls");
        // 创建输入流，读取Excel
        InputStream is = new FileInputStream(file.getAbsolutePath());
        // jxl提供的Workbook类


        Workbook wb = Workbook.getWorkbook(is);





        // Excel的页签数量
        int sheet_size = wb.getNumberOfSheets();



        Sheet sheet = wb.getSheet(0);

        Label xuexiao = new Label(0,0,"学校");
//        sheet.addCell(xuexiao);





    }


    public void test(OutputStream os){

        try {
            //创建工作薄
            WritableWorkbook workbook = Workbook.createWorkbook(os);
            //创建新的一页
            WritableSheet sheet = workbook.createSheet("First Sheet",0);
            //创建要显示的内容,创建一个单元格，第一个参数为列坐标，第二个参数为行坐标，第三个参数为内容
            Label xuexiao = new Label(0,0,"学校");
            sheet.addCell(xuexiao);
            Label zhuanye = new Label(1,0,"专业");
            sheet.addCell(zhuanye);
            Label jingzhengli = new Label(2,0,"专业竞争力");
            sheet.addCell(jingzhengli);

            Label qinghua = new Label(0,1,"清华大学");
            sheet.addCell(qinghua);
            Label jisuanji = new Label(1,1,"计算机专业");
            sheet.addCell(jisuanji);
            Label gao = new Label(2,1,"高");
            sheet.addCell(gao);

            Label beida = new Label(0,2,"北京大学");
            sheet.addCell(beida);
            Label falv = new Label(1,2,"法律专业");
            sheet.addCell(falv);
            Label zhong = new Label(2,2,"中");
            sheet.addCell(zhong);

            Label ligong = new Label(0,3,"北京理工大学");
            sheet.addCell(ligong);
            Label hangkong = new Label(1,3,"航空专业");
            sheet.addCell(hangkong);
            Label di = new Label(2,3,"低");
            sheet.addCell(di);

            //把创建的内容写入到输出流中，并关闭输出流
            workbook.write();
            workbook.close();
            os.close();
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
