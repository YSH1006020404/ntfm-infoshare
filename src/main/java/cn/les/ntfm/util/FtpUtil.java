package cn.les.ntfm.util;

import cn.les.ntfm.infoshare.entity.FtpConfigDO;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.SocketException;

/****
 **
 * @author guoguoyang
 * @date 2019/4/9 13:42
 * @description
 **/
@Service
public class FtpUtil {

    /**
     * 创建连接
     *
     * @param ftpConfigDO
     * @return
     */
    public static FTPClient createFtpClient(FtpConfigDO ftpConfigDO) {
        FTPClient ftpClient = new FTPClient();
        try {
            // 连接FTP服务
            ftpClient.connect(ftpConfigDO.getIp(), ftpConfigDO.getPort());
            // 登陆FTP服务
            ftpClient.login(ftpConfigDO.getUsername(), ftpConfigDO.getPassword());
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                System.out.println("未连接到FTP，用户名或密码错误");
                ftpClient.disconnect();
            } else {
                System.out.println("FTP连接成功");
                ftpClient.setControlEncoding("UTF-8"); // 中文支持
//                ftpClient.doCommand("opts", "utf8 off");
//                FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
//                conf.setServerLanguageCode("zh");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
            }
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("FTP的IP地址可能错误，请正确配置");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("FTP的端口错误,请正确配置");
        }
        return ftpClient;
    }

    /**
     * 是否连接失败
     *
     * @param ftpClient
     * @return
     */
    public static Boolean isConnectClosed(FTPClient ftpClient) {
        Boolean flag;
        if (ftpClient == null || !ftpClient.isConnected()) {
            flag = true;
        } else {
            flag = false;

        }
        return flag;

    }


    /**
     * 上传FTP
     *
     * @param ftpClient
     * @param ftpConfigDO
     * @param is
     * @param fileName
     * @return
     */
    public static boolean uploadToFtp(FTPClient ftpClient, FtpConfigDO ftpConfigDO, InputStream is, String fileName) {
        boolean storeResult = false;
        try {
            ftpClient.changeWorkingDirectory(ftpConfigDO.getPath());
            storeResult = ftpClient.storeFile(fileName, is);
            is.close();
            //ftpClient.logout();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return storeResult;
    }

}