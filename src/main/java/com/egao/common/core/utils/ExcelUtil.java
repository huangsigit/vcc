package com.egao.common.core.utils;


import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import jxl.Sheet;
import jxl.SheetSettings;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.*;
import org.apache.poi.hssf.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * JSON解析工具类
 * Created by hs on 2017-06-10 10:10
 */
public class ExcelUtil {

    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";

    public static void main(String[] args) {
        try {

            ExcelUtil obj = new ExcelUtil();
            // 此处为我创建Excel路径：E:/zhanhj/studysrc/jxl下
            File file = new File("E:\\1126\\01.xlsx");

            List excelList = obj.readExcel(file);
            System.out.println("list中的数据打印出来："+ JSONArray.toJSONString(excelList));

            for (int i = 0; i < excelList.size(); i++) {
                List list = (List) excelList.get(i);
                for (int j = 0; j < list.size(); j++) {
                    System.out.print(list.get(j));
                }
                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 去读Excel的方法readExcel，该方法的入口参数为一个File对象
    public List readExcel(File file) {
        try {
            // 创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            // jxl提供的Workbook类
            Workbook wb = Workbook.getWorkbook(is);
            // Excel的页签数量
            int sheet_size = wb.getNumberOfSheets();
            for (int index = 0; index < sheet_size; index++) {
                List<List> outerList=new ArrayList<List>();
                // 每个页签创建一个Sheet对象
                Sheet sheet = wb.getSheet(index);
                // sheet.getRows()返回该页的总行数
                for (int i = 0; i < sheet.getRows(); i++) {
                    List innerList=new ArrayList();
                    // sheet.getColumns()返回该页的总列数
                    for (int j = 0; j < sheet.getColumns(); j++) {
                        String cellinfo = sheet.getCell(j, i).getContents();
                        /*if(cellinfo.isEmpty()){
                            continue;
                        }*/
                        innerList.add(cellinfo);
//                        System.out.print(cellinfo);
                    }
                    outerList.add(i, innerList);
//                    System.out.println();
                }
                return outerList;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 导出Excel
     * @param sheetName sheet名称
     * @param title 标题
     * @param values 内容
     * @param wb HSSFWorkbook对象
     * @return
     */
    public static HSSFWorkbook getHSSFWorkbook(String sheetName, String []title, String [][]values, HSSFWorkbook wb){

        // 第一步，创建一个HSSFWorkbook，对应一个Excel文件
        if(wb == null){
            wb = new HSSFWorkbook();
        }

        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet(sheetName);

        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
        HSSFRow row = sheet.createRow(0);

        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式

        //声明列对象
        HSSFCell cell = null;

        //创建标题
        for(int i=0;i<title.length;i++){
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }

        //创建内容
        for(int i=0;i<values.length;i++){
            row = sheet.createRow(i + 1);
            for(int j=0;j<values[i].length;j++){
                //将内容按顺序赋给对应的列对象
                row.createCell(j).setCellValue(values[i][j]);
            }
        }
        return wb;
    }


    public static int exportToExcel(HttpServletResponse response,
                                    List<Map<String, Object>> objData, String sheetName,
                                    List<String> columns) {
        int flag = 0;
        //声明工作簿jxl.write.WritableWorkbook
        WritableWorkbook wwb;
        try {
            //根据传进来的file对象创建可写入的Excel工作薄
            OutputStream os = response.getOutputStream();

            wwb = jxl.Workbook.createWorkbook(os);

            /*
             * 创建一个工作表、sheetName为工作表的名称、"0"为第一个工作表
             * 打开Excel的时候会看到左下角默认有3个sheet、"sheet1、sheet2、sheet3"这样
             * 代码中的"0"就是sheet1、其它的一一对应。
             * createSheet(sheetName, 0)一个是工作表的名称，另一个是工作表在工作薄中的位置
             */
            WritableSheet ws = wwb.createSheet(sheetName, 0);

            SheetSettings ss = ws.getSettings();
            ss.setVerticalFreeze(1);//冻结表头

            WritableFont font1 = new WritableFont(WritableFont.createFont("微软雅黑"), 10, WritableFont.BOLD);
            // WritableFont font2 =new WritableFont(WritableFont.createFont("微软雅黑"), 9 ,WritableFont.NO_BOLD);
            WritableCellFormat wcf = new WritableCellFormat(font1);
            // WritableCellFormat wcf2 = new WritableCellFormat(font2);
            // WritableCellFormat wcf3 = new WritableCellFormat(font2);//设置样式，字体

            //创建单元格样式
            //WritableCellFormat wcf = new WritableCellFormat();

            //背景颜色
            // wcf.setBackground(jxl.format.Colour.YELLOW);
//            wcf.setAlignment(Alignment.CENTRE);  //平行居中
//            wcf.setVerticalAlignment(VerticalAlignment.CENTRE);  //垂直居中
            //  wcf3.setAlignment(Alignment.CENTRE);  //平行居中
            //  wcf3.setVerticalAlignment(VerticalAlignment.CENTRE);  //垂直居中
            //  wcf3.setBackground(Colour.LIGHT_ORANGE);
            // wcf2.setAlignment(Alignment.CENTRE);  //平行居中
            // wcf2.setVerticalAlignment(VerticalAlignment.CENTRE);  //垂直居中

            /*
             * 这个是单元格内容居中显示
             * 还有很多很多样式
             */
            //   wcf.setAlignment(Alignment.CENTRE);
            //判断一下表头数组是否有数据
            if (columns != null && columns.size() > 0) {

                //循环写入表头
                for (int i = 0; i < columns.size(); i++) {
                    ws.setColumnView(i, 20);//设置列宽
//                    ws.setRowView(i+1, 600, false); //设置行高
                    /*
                     * 添加单元格(Cell)内容addCell()
                     * 添加Label对象Label()
                     * 数据的类型有很多种、在这里你需要什么类型就导入什么类型
                     * 如：jxl.write.DateTime 、jxl.write.Number、jxl.write.Label
                     * Label(i, 0, columns[i], wcf)
                     * 其中i为列、0为行、columns[i]为数据、wcf为样式
                     * 合起来就是说将columns[i]添加到第一行(行、列下标都是从0开始)第i列、样式为什么"色"内容居中
                     */
                    ws.addCell(new Label(i, 0, columns.get(i), wcf));
                }

                //判断表中是否有数据
                if (objData != null && objData.size() > 0) {
                    //循环写入表中数据
                    for (int i = 0; i < objData.size(); i++) {

                        //转换成map集合{activyName:测试功能,count:2}
                        Map<String, Object> map = objData.get(i);
                        //循环输出map中的子集：既列值
                        int j = 0;
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                        for (Object o : map.keySet()) {
                            //ps：因为要“”通用”“导出功能，所以这里循环的时候不是get("Name"),而是通过map.get(o)
                            String content = "";
//                            if (map.get(o).toString().contains(".") && CommonUtils.isNumber(map.get(o).toString())) {
                            if (map.get(o).toString().contains(".") && isNumeric(map.get(o).toString())) {
                                content = decimalFormat.format(Float.valueOf(map.get(o).toString()));
                                ws.addCell(new Label(j, i + 1, content));
                            } else if (map.get(o).toString().contains("-") && map.get(o).toString().contains(":")) {
                                content = String.valueOf(map.get(o)).split("\\.")[0];
                                ws.addCell(new Label(j, i + 1, content));
                            }
                            //图片处理
//                            else if (map.get(o).toString().contains("http") || map.get(o).toString().contains("https")){
//                                ws.setColumnView(j, 15);//设置列宽
//                                String path ="/resources/"+ String.valueOf(map.get(o)).split("upload/")[1];
//                                File imgFile = new File(path);
//                                WritableImage image = new WritableImage(j,i+1,1,1,imgFile);
//                                ws.addImage(image);
//                            }
                            else {
                                content = String.valueOf(map.get(o));
                                ws.addCell(new Label(j, i + 1, content));
                            }
                            j++;
                        }

                       /* for(int b=0;b<map.size();b++){
                        	 ws.addCell(new Label(b,i+1,String.valueOf(map.get(String.valueOf(b)))));
                        }*/
                    }
                } else {
                    flag = -1;
                }

                //写入Exel工作表
                wwb.write();

                //关闭Excel工作薄对象
                wwb.close();

                //关闭流
                os.flush();
                os.close();
            }
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
        } catch (Exception ex) {
            flag = 0;
            ex.printStackTrace();
        }

        return flag;
    }

    public static boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

}
