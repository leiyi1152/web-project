package com.icloud.thirdinterfaces.score.utils;

import com.icloud.basecommon.service.redis.RedisService;
import com.icloud.basecommon.service.redislock.DistributedLock;
import com.icloud.basecommon.service.redislock.DistributedLockUtil;
import com.icloud.common.ConfigUtil;
import com.icloud.common.DateUtil;
import com.icloud.common.util.StringUtil;
import com.icloud.exceptions.BeanException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;

@Component
public class LongCoinUtil {

    @Autowired
    private DistributedLockUtil distributedLockUtil;
    @Autowired
    private RedisService redisService;
    /**
     * 获取流水号中的第三部分
     * @return
     */
    public String getBinaryString() {

        DistributedLock lock = distributedLockUtil.getDistributedLock("longcoin_msgSerialNumber");
        try {
            if (lock.acquire()) {
                 Object reno =  redisService.get("msgSerialNumber");
                 String no = "";
                 if (!StringUtil.checkObj(reno)) {
                    no = "1";
                    redisService.set("msgSerialNumber", no);
                    return toFillingString(no);
                 } else {
                    no = reno.toString();
                    int count = Integer.parseInt(no);
                    if (count >= 99999999) {
                        no = "1";
                        redisService.set("msgSerialNumber", no);
                        return toFillingString(no);
                    } else {
                        no = (Integer.parseInt(no) + 1) + "";
                        redisService.set("msgSerialNumber", no);
                        return toFillingString(no);
                    }
                 }
            } else { // 获取锁失败
                //获取锁失败业务代码
                throw new BeanException("系统繁忙,请稍后再试");
            }
        } finally {
            if (lock != null) {
                lock.release();
            }
        }
    }

    /**
     * 补齐八位二进制
     *
     * @param str
     * @return
     */
    public static String toIntegerBinaryString(String str) {
        int num = Integer.parseInt(str);
        char[] bits = new char[8];
        for (int i = bits.length - 1; i >= 0; --i) {
            bits[i] = ((num & 1) == 0) ? '0' : '1';
            num >>>= 1;// 下一位
        }
        return String.copyValueOf(bits);
    }

    /**
     * 补齐八位
     *
     * @param str
     * @return
     */
    public static String toFillingString(String str) {

        int length = str.length();
        for(int i=0;i<8-length;i++){
            str = "0"+str;
        }

        return str;
    }

    /**
     * 获取请求参数中的时间戳
     *
     * @return
     */
    public String getTimeStamp() {
        return (System.currentTimeMillis() / 1000
                - DateUtil.parseTimeString("2000-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime() / 1000) + "";
    }

    /**
     * 得到请求中的流水号
     *
     * @return
     */
    public String getSerialNumber() {
        String machineNo = ConfigUtil.get("sid");
        String currentTime = DateUtil.getYearMonthDayWithMinus(new Date());
        String runNum = getBinaryString();
        return machineNo + currentTime + runNum;
    }

    /**
     * 得到请求中的流水号
     *
     * @return
     */
    public String getSerialNumber_signup() {
        String machineNo = ConfigUtil.get("sid_signup");
        String currentTime = DateUtil.getYearMonthDayWithMinus(new Date());
        String runNum = getBinaryString();
        return machineNo + currentTime + runNum;
    }


    /**
     * 获取设备号
     *
     * @return
     */
    public String getMachineNo() {
        return ConfigUtil.get("sid");
    }

    /**
     * 获取key
     *
     * @return
     */
    public String getKey() {
        return ConfigUtil.get("key");

    }

    /**
     * 获取充值类别
     *
     * @return
     */
    public String getRechargetype() {
        return ConfigUtil.get("rechargetype");
    }

    /**
     * 获取查询龙币URL
     *
     * @return
     */
    public String getQueryUrl() {
        return ConfigUtil.get("queryUrl");
    }

    /**
     * 获取充值接口URL
     *
     * @return
     */
    public String getRechargeUrl() {
        return ConfigUtil.get("rechargeUrl");
    }

    /**
     * 获取龙币消费URL
     *
     * @return
     */
    public String getConsumeUrl() {
        return ConfigUtil.get("consumeUrl");
    }

    /**
     * 获取消费类别
     *
     * @return
     */
    public String getConsumetype() {
        return ConfigUtil.get("consumetype");
    }

    /**
     * post请求 返回 不带证书
     *
     * @param params
     * @return
     */
    public String sendRequest(Map<String, String> params, String url) {

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod(url);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            post.addParameter(entry.getKey(), entry.getValue());
        }

        HttpMethodParams param = post.getParams();
        param.setContentCharset("UTF-8");

        try {
            httpClient.executeMethod(post);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 如果返回200，表明成功
        if (post.getStatusCode() == 200) {
            try {
//                String result = post.getResponseBodyAsString();
//                System.out.println("longbi："+result);
//                return result;

                InputStream inputStream = post.getResponseBodyAsStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer stringBuffer = new StringBuffer();
                String str= "";
                while((str = br.readLine()) != null){
                    stringBuffer .append(str );
                }
                return stringBuffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }

    }


    public String sslRequest(Map<String, String> params, String url) throws Exception {

        CloseableHttpClient httpclient = null;

        // 相信自己的CA和所有自签名的证书
        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .build();
        // 只允许使用TLSv1协议
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" },
                null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        url = url + "?" + sb.toString();
        HttpPost post = new HttpPost(url);


        CloseableHttpResponse response = httpclient.execute(post);
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
                String result = EntityUtils.toString(entity);
                return result;
            }
        } finally {
            response.close();
        }

        return null;


    }


}