package cn.edu.nju.p.dao.daoutils;

import cn.edu.nju.p.dao.StockDao;
import cn.edu.nju.p.po.StockPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 * Created by dell- on 2017/5/12.
 */
@Component
public class MysqlHelperImpl implements MysqlHelper {

    private static final String PATH="D://youkuang";
//    private static final MysqlHelperImpl helper=new MysqlHelperImpl(); //单例模式

//    public static MysqlHelperImpl getInstance() {
//        return helper;
//    }

    @Autowired
    private StockDao stockDao;

    @Override
    public void getDataFromCSV(String year) throws SQLException{
        String path = PATH + year + ".csv";
        String encoding="GBK";

        FileInputStream is = null;
        InputStreamReader reader = null;
        BufferedReader br = null;
//        int count = 0;
        try {
            File file = new File(path);
//            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            is = new FileInputStream(file);
            reader = new InputStreamReader(is, encoding);
            br = new BufferedReader(reader);
            String content = br.readLine();
            content = br.readLine();//读取文件标题
            while (content != null) {
                String[] data = content.split(",");
                String date = data[0];
                double open = setPrecision(data[1]);
                double high = setPrecision(data[2]);
                double low = setPrecision(data[3]);
                double close = setPrecision(data[4]);
                int volume = Integer.parseInt(data[5]);
                double adjClose = setPrecision(data[6]);
                String code = adjustCode(data[7]);
                String name = adjustName(data[8]);
                String market = data[9];

                StockPO stockPO = new StockPO();
                //date, open, high, low, close, volume, adjClose, code, name, market,"",0.0
                stockPO.setAdj_close(adjClose);
                stockPO.setClose(close);
                stockPO.setCode(code);
                stockPO.setCurrentPrice(0.0);
                stockPO.setDate(date);
                stockPO.setOpen(open);
                stockPO.setHigh(high);
                stockPO.setLow(low);
                stockPO.setVolume(volume);
                stockPO.setMarket(market);
                stockPO.setName(name);
                stockPO.setTime("");

                try {
                    insertIntoDataBase(year, stockPO);
                    System.out.println(stockPO.getName() + "已经写入数据库");
//                    count++;
//                    System.out.println(count);

                } catch (SQLException ex) {
                    System.out.println(stockPO.getName()+" "+stockPO.getCode()+" 没有写入数据库");
                }

                content=br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
               try{
                   br.close();
               }catch (IOException ex){
                   ex.printStackTrace();
               }
            }
        }
    }

    /**
     * 插入数据库
     * @param year
     * @param po
     * @throws SQLException
     */
    @Override
    public void insertIntoDataBase(String year, StockPO po) throws SQLException {
        try {
            System.out.println(stockDao == null);
            stockDao.insertIntoStockDatabase(year,po);
        } catch (SQLException e) {
            System.out.println(po.getName()+" "+po.getCode()+" 没有写入数据库");

        }

    }

    /**
     * 调整一下股票的名称，去除间隔，解决A的全角半角问题
     * @param name
     * @return
     */
    private String adjustName(String name){
        String newName=name.replaceAll(" ", "");
        if(newName.contains("Ａ")){
            String newStr=newName.replace('Ａ', 'A');
            return newStr;
        }
        return newName;
    }

    /**
     * 调整股票代码 变成六位标准的代码
     * @param code
     * @return
     */
    private String adjustCode(String code){
        String result=code;
        int len=code.length();
        int zero=6-len;
        for(int i=0;i<zero;i++){
            result="0"+result;
        }
        return result;
    }

    /**
     * 设置数据精度，保留小数点后两位小数
     * @param value
     * @return
     */
    public double setPrecision(String value){
        double data=Double.parseDouble(value);
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.parseDouble(df.format(data));
    }

//    public static void main(String[] args) {
//        String year = "2016";
//        try {
//            new MysqlHelperImpl().getDataFromCSV(year);
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }

}
