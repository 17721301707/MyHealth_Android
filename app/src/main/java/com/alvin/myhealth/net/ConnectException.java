package com.alvin.myhealth.net;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.alvin.myhealth.R;

import org.apache.commons.httpclient.HttpException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by alvin on 2016/1/8.
 */
public class ConnectException extends  Exception {
    private final static boolean Debug = false;// 是否保存错误日志

    /** 定义异常类型 */
    public final static byte TYPE_NETWORK = 0x01;
    public final static byte TYPE_SOCKET = 0x02;
    public final static byte TYPE_HTTP_CODE = 0x03;
    public final static byte TYPE_HTTP_ERROR = 0x04;
    public final static byte TYPE_XML = 0x05;
    public final static byte TYPE_IO = 0x06;
    public final static byte TYPE_RUN = 0x07;

    private byte type;
    private int code;

    private ConnectException(byte type, int code, Exception excp){
        super(excp);
        this.type = type;
        this.code = code;
        if (Debug)
        {
            this.saveErrorLog(excp);
        }
    }

    public int getCode()
    {
        return this.code;
    }

    public int getType()
    {
        return this.type;
    }

    /**
     * 提示友好的错误信息
     *
     * @param ctx
     */
    public void makeToast(Context ctx)
    {
        switch (this.getType())
        {
            case TYPE_HTTP_CODE:
                String err = ctx.getString(R.string.http_status_code_error,this.getCode());
                Toast.makeText(ctx, err, Toast.LENGTH_SHORT).show();
                break;
            case TYPE_HTTP_ERROR:
                Toast.makeText(ctx, R.string.http_exception_error,Toast.LENGTH_SHORT).show();
                break;
            case TYPE_SOCKET:
                Toast.makeText(ctx, R.string.socket_exception_error,Toast.LENGTH_SHORT).show();
                break;
            case TYPE_NETWORK:
                Toast.makeText(ctx, R.string.network_not_connected,Toast.LENGTH_SHORT).show();
                break;
            case TYPE_XML:
                Toast.makeText(ctx, R.string.xml_parser_failed, Toast.LENGTH_SHORT).show();
                break;
            case TYPE_IO:
                Toast.makeText(ctx, R.string.io_exception_error, Toast.LENGTH_SHORT).show();
                break;
            case TYPE_RUN:
                Toast.makeText(ctx, R.string.app_run_code_error, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 保存异常日志
     *
     * @param excp
     */
    public void saveErrorLog(Exception excp)
    {
        String errorlog = "errorlog.txt";
        String savePath = "";
        String logFilePath = "";
        FileWriter fw = null;
        PrintWriter pw = null;
        try
        {
            // 判断是否挂载了SD卡
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED))
            {
                savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OSChina/Log/";
                File file = new File(savePath);
                if (!file.exists())
                {
                    file.mkdirs();
                }
                logFilePath = savePath + errorlog;
            }
            // 没有挂载SD卡，无法写文件
            if (logFilePath == "")
            {
                return;
            }
            File logFile = new File(logFilePath);
            if (!logFile.exists())
            {
                logFile.createNewFile();
            }
            fw = new FileWriter(logFile, true);
            pw = new PrintWriter(fw);
            excp.printStackTrace(pw);
            pw.close();
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (pw != null)
            {
                pw.close();
            }
            if (fw != null)
            {
                try
                {
                    fw.close();
                }
                catch (IOException e)
                {

                }
            }
        }
    }

    public static ConnectException http(int code)
    {
        return new ConnectException(TYPE_HTTP_CODE, code, null);
    }

    public static ConnectException http(Exception e)
    {
        return new ConnectException(TYPE_HTTP_ERROR, 0, e);
    }

    public static ConnectException socket(Exception e)
    {
        return new ConnectException(TYPE_SOCKET, 0, e);
    }

    public static ConnectException io(Exception e)
    {
        if (e instanceof UnknownHostException || e instanceof ConnectException)
        {
            return new ConnectException(TYPE_NETWORK, 0, e);
        }
        else if (e instanceof IOException)
        {
            return new ConnectException(TYPE_IO, 0, e);
        }
        return run(e);
    }

    public static ConnectException xml(Exception e)
    {
        return new ConnectException(TYPE_XML, 0, e);
    }

    public static ConnectException network(Exception e)
    {
        if (e instanceof UnknownHostException || e instanceof ConnectException)
        {
            return new ConnectException(TYPE_NETWORK, 0, e);
        }
        else if (e instanceof HttpException)
        {
            return http(e);
        }
        else if (e instanceof SocketException)
        {
            return socket(e);
        }
        return http(e);
    }

    public static ConnectException run(Exception e)
    {
        return new ConnectException(TYPE_RUN, 0, e);
    }

}
