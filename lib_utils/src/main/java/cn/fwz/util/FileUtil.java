package cn.fwz.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class FileUtil {

    //拷贝文件
    public static void copyFileByChannel(File source, File des) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel targetChannel = new FileOutputStream(des).getChannel();){
            for (long count = sourceChannel.size() ;count>0 ;) {
                long transferred = sourceChannel.transferTo(sourceChannel.position(), count, targetChannel);
                sourceChannel.position(sourceChannel.position() + transferred);
                count -= transferred;
            }
        }
    }

    /**
     * 保存文件
     * @param isCover true时添加到文件结尾，当为false时为覆盖
     */
    public static boolean writeToFile(String fileName,boolean isCover,byte[] content){
        File file=new File(fileName);
        OutputStream out=null;
        try {
            out=new FileOutputStream(file,isCover);
            out.write(content);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            if (out != null) {
                try {
                    out.close();  //关闭输出文件流
                    return true;
                } catch (IOException el) {
                    el.fillInStackTrace();
                }
            }
            return false;
        }
    }

    /**
     * 判断当前目录是否存在
     */
    private static boolean createOrExistsDir(final File file) {
        // 如果存在，是目录则返回 true，是文件则返回 false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 创建空文件
     */
    public static boolean isFileExistence(String fileName){
        return isFileExistence(new File(fileName));
    }

    /**
     * 创建空文件
     */
    public static boolean isFileExistence(File file){
        if (file == null) {
            return false;
        }
        //当文件存在并且删除失败时返回false
        if (file.exists()&&!file.delete()){
            return false;
        }
        if (!createOrExistsDir(file.getParentFile())){
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
