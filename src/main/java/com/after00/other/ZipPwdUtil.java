package com.after00.other;


import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.*;
import java.nio.charset.Charset;

/**
 * 压缩文件密码破解工具类.
 * 
 * 7Z：jar（commons-compress-1.9.jar、xz-1.6.jar）
 * 
 */
public class ZipPwdUtil {
    public static void main(String[] args) throws IOException {
        // Scanner scanner = new Scanner(System.in);
        // String zipPath = scanner.nextLine();
        String zipPath = "e:\\123.7z";
        String pwd = "79";
        boolean bool = validatePwd(zipPath, pwd);
        System.out.println(bool);
    }

    /**
     * 校验xx.7Z压缩包的密码是否正确.
     * 
     * 若抛异常（java.security.InvalidKeyException: Illegal key size），
     * 
     * 在官方网站下载JCE无限制权限策略文件解决JDK不兼容问题（亦可从工程lib包下载）。
     * 
     * JDK7的下载地址: http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
     * 
     * JDK8的下载地址: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
     * 
     * 将解压后的local_policy.jar和US_export_policy.jar放到%JDK_HOME%\jre\lib\security目录和%JRE_HOME%\lib\security目录下覆盖原文件即可。
     * 
     * @param path7ZFile
     *            7Z压缩包文件路径
     * @param pwd
     *            密码
     * @return 密码是否正确
     */
    public static boolean validatePwd(String path7ZFile, String pwd) {
        boolean bool = true;
        File file = new File(path7ZFile);
        if (!file.exists()) {
            System.out.println("no file[" + path7ZFile + "]");
            return false;
        }
        if (null == pwd) {
            pwd = "";
        }
        try {
            @SuppressWarnings("resource")
            SevenZFile sevenZFile = new SevenZFile(file, pwd.getBytes(Charset.forName("UTF-16LE")));
            SevenZArchiveEntry entry = null;
            while (null != (entry = sevenZFile.getNextEntry())) {
                byte[] content = new byte[2]; // 流输出
                try {
                    // sevenZFile.read(content, 0, 2);
                    // sevenZFile.read(content, (int) (entry.getSize() - 2), (int) entry.getSize());
                    sevenZFile.read(content, 0, (int) entry.getSize());
                    // sevenZFile.read(content, 0, 2);
                } catch (Exception e) { // 读取数据异常，说明密码错误
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return bool;
    }

    /**
     * 解压7Z文件到当前位置.
     * 
     * @param path7ZFile
     *            7Z压缩包文件路径
     * @param pwd
     *            密码（无密码传null）
     * @return 解压成功
     */
    public static boolean un7z(String path7ZFile, String pwd) {
        SevenZFile sevenZFile = null;
        try {
            File file = new File(path7ZFile);
            if (!file.exists()) {
                System.out.println("no file[" + path7ZFile + "]");
                return false;
            }
            if (null == pwd) {
                pwd = "";
            }
            sevenZFile = new SevenZFile(file, pwd.getBytes(Charset.forName("UTF-16LE")));
            SevenZArchiveEntry archiveEntry = null;
            while ((archiveEntry = sevenZFile.getNextEntry()) != null) {
                String entryFileName = archiveEntry.getName(); // 文件名
                byte[] content = new byte[(int) archiveEntry.getSize()]; // 流输出
                sevenZFile.read(content, 0, content.length);
                String entryFilePath = file.getParent() + entryFileName; // 解压文件存放路径
                OutputStream os = null;
                try {
                    File entryFile = new File(entryFilePath);
                    os = new BufferedOutputStream(new FileOutputStream(entryFile)); // 输出解压文件
                    os.write(content);
                } finally {
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                }
            }
            sevenZFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (sevenZFile != null) {
                    sevenZFile.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

}
