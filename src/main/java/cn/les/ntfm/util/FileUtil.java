package cn.les.ntfm.util;

import cn.les.ntfm.constant.LogConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 文件操作工具类
 *
 * @author 杨硕
 * @create 2020-03-18 19:44
 */
public class FileUtil {
    /**
     * 文件生成
     *
     * @param filePath 路径
     * @param fileName 文件名
     * @param content  内容
     */
    public static void createFile(String filePath, String fileName, String content) {
        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        try (FileWriter fw = new FileWriter(new File(file, fileName))) {
            fw.write(content);
            fw.flush();
        } catch (IOException e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("FileUtil类的createFile方法出现错误，错误原因：", e);
        }
    }

    public static void deleteFile(String filePath, String fileName) {
        File file = new File(filePath + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}
