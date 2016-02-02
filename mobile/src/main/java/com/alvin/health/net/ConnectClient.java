package com.alvin.health.net;

import android.content.Context;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by alvin on 2016/1/8.
 */
public class ConnectClient {
    public static Context mContext;
    public static final String UTF_8 = "UTF-8";
    public static final String DESC = "descend";
    public static final String ASC = "ascend";
    public static final String HOST = "";
    public static final String VALUE = "value";
    public static final String ACCEPT = "application/json";


    public final static int TIMEOUT_CONNECTION = 10000;
    public final static int TIMEOUT_SOCKET = 10000;
    public final static int RETRY_TIME = 1;

    public static String appCookie;
    public static String appUserAgent;
    public final static int pagesize = 20;

    public static InputStream _post(Context context, String url,
                                    String xmldata, Map<String, File> files) throws ConnectException {
        String cookie = getCookie(context);
        String userAgent = getUserAgent(context);
        HttpClient httpClient = null;
        PostMethod httpPost = null;
        String responseBody = "";
        int time = 0;
        do {
            time++;
            httpClient = getHttpClient();
            httpPost = getHttpPost(url, cookie, userAgent);
            try {
                httpPost.setRequestEntity(new StringRequestEntity(xmldata, "application/json", "UTF-8"));
                int statusCode = httpClient.executeMethod(httpPost);
                if (statusCode != HttpStatus.SC_OK) {
                    throw ConnectException.http(statusCode);
                }
                responseBody = httpPost.getResponseBodyAsString();
                break;
            } catch (UnsupportedEncodingException e) {
                if (time < RETRY_TIME)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e1)
                    {

                    }
                    continue;
                }
                e.printStackTrace();
            } catch (IOException e) {
                if (time < RETRY_TIME)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e1)
                    {

                    }
                    continue;
                }
                e.printStackTrace();
            } finally {
                // 释放连接
                httpPost.releaseConnection();
                httpClient = null;
            }
        } while (time < RETRY_TIME);
        responseBody = responseBody.replace('', '?');
        return new ByteArrayInputStream(responseBody.getBytes());
    }

    public static HttpClient getHttpClient() {
        HttpClient httpClient = new HttpClient();
        // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        // 设置 默认的超时重试处理策略
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        // 设置 连接超时时间
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT_CONNECTION);
        // 设置 读数据超时时间
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(TIMEOUT_SOCKET);
        // 设置 字符集
        httpClient.getParams().setContentCharset(UTF_8);
        return httpClient;
    }

    public static PostMethod getHttpPost(String url, String cookie, String userAgent) {
        PostMethod httpPost = new PostMethod(url);
        // 设置 请求超时时间
        httpPost.getParams().setSoTimeout(TIMEOUT_SOCKET);

        httpPost.setRequestHeader("accept", ACCEPT);
        httpPost.setRequestHeader("Header", VALUE);
        httpPost.setRequestHeader("Host", HOST);
        httpPost.setRequestHeader("Connection", "Keep-Alive");
        httpPost.setRequestHeader("Content-type", "application/json");
        httpPost.setRequestHeader("Cookie", cookie);
        httpPost.setRequestHeader("User-Agent", userAgent);
        return httpPost;
    }

    public static byte[] readStream(InputStream inputStream) throws Exception
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1)
        {
            bout.write(buffer, 0, len);
        }
        bout.close();
        inputStream.close();
        return bout.toByteArray();
    }

    public static String getUserAgent(Context context) {
        if (appUserAgent == null || appUserAgent == "") {
            StringBuilder ua = new StringBuilder("");
            // ua.append('/'+context.getPackageInfo().versionName+'_'+context.getPackageInfo().versionCode);//App版本
            ua.append("/Android");// 手机系统平台
            ua.append("/" + android.os.Build.VERSION.RELEASE);// 手机系统版本
            ua.append("/" + android.os.Build.MODEL); // 手机型号
            // ua.append("/"+context.getAppId());//客户端唯一标识
            appUserAgent = ua.toString();
        }
        return appUserAgent;
    }

    public static void cleanCookie() {
        appCookie = "";
    }

    public static String getCookie(Context context) {
        if (appCookie == null || appCookie == "") {

        }
        return appCookie;
    }

}
