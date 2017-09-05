/*
 * 文件名：FileUtil.java
 * 版权：Copyright 2007-2016 zxiaofan.com. Co. Ltd. All Rights Reserved. 
 * 描述： FileUtil.java
 * 修改人：zxiaofan
 * 修改时间：2016年11月17日
 * 修改内容：新增
 */
package com.after00.other;

import java.io.*;
import java.util.Map;
import java.util.concurrent.*;

/**
 * github.zxiaofan.com.
 * 
 * @author zxiaofan
 */
public class FileUtil {

    /**
     * 缓存指定数据的偏移位置.
     */
    static Map<String, Long> mapIndex = new ConcurrentHashMap<>(); // <行key,index>

    /**
     * 构造函数.
     * 
     */
    public FileUtil() {
        throw new RuntimeException("this is a util class,can not instance!");
    }

    /**
     * 超大文本获取指定内容行.
     * 
     * 文件行索引完成后，获取内容耗时在毫秒级别。
     * 
     * @param file
     *            file
     * @param specialContent
     *            查找指定内容
     * @return String
     * @throws Exception
     *             Exception
     */
    public static String getSpecificLine(File file, String specialContent) throws Exception {
        RandomAccessFile randomAccessFile = null;
        StringBuilder builder;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long fileLength = randomAccessFile.length(); // 获取当前文件的长度
            long indexStart = 0;
            if (!mapIndex.containsKey("max")) { // 当前最大指针
                mapIndex.put("max", 0L);
            }
            Future<String> future = null;
            if (mapIndex.containsKey(specialContent)) {
                indexStart = mapIndex.get(specialContent);
            } else {
                indexStart = mapIndex.get("max");
                // 大文件缓存全部key索引耗时较多，建议按行匹配并发,取优先完成的数据
                ExecutorService service = Executors.newFixedThreadPool(1);
                // future = service.submit(new QueryCallable(file, content));
            }
            if (indexStart > fileLength || indexStart < 0) {
                System.out.println("index illegal:" + indexStart);
                return null;
            }
            long index = indexStart;
            randomAccessFile.seek(index);
            builder = new StringBuilder();
            String line = null;
            boolean getData = false;
            while (index <= fileLength && null != (line = randomAccessFile.readLine())) {
                line = new String(line.getBytes("ISO-8859-1"), "utf-8"); // 处理乱码
                String key = line.substring(0, Math.min(10, line.length())); // 假设key为我们要匹配的content
                if (key.equals(specialContent)) {
                    builder.append(line);
                    getData = true;
                } else if (getData && !key.equals(specialContent)) {
                    break;
                }
                if (!mapIndex.containsKey(key)) {
                    mapIndex.put(key, index);
                }
                if (index > mapIndex.get("max")) {
                    mapIndex.put("max", index);
                }
                index = randomAccessFile.getFilePointer(); // 获取下次循环的开始指针
                if (null != future) {
                    try {
                        String res = future.get(1, TimeUnit.MILLISECONDS);
                        if (null != res) {
                            builder.setLength(0);
                            builder.append(res);
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println();
                    }
                }
            }
        } finally {
            if (null != randomAccessFile) {
                randomAccessFile.close();
            }
        }
        return builder.toString();
    }

    /**
     * 读取本地文件为字符串.
     * 
     * @param fileName
     *            文件名
     * @param encode
     *            字符编码
     * @return String
     * @throws IOException
     *             IOException
     */
    public static String readFileToString(String fileName, String encode) throws IOException {
        java.io.InputStream input = null;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                if (file.isDirectory()) {
                    throw new IOException("File '" + file + "' exists but is a directory");
                }
                if (!file.canRead()) {
                    throw new IOException("File '" + file + "' cannot be read");
                } else {
                    input = new FileInputStream(file);
                }
            } else {
                throw new FileNotFoundException("File '" + file + "' does not exist");
            }
            StringWriter output = new StringWriter();
            InputStreamReader in = null;
            if (encode == null) {
                in = new InputStreamReader(input);
            } else {
                in = new InputStreamReader(input, encode);
            }
            char[] buffer = new char[4096];
            for (int n = 0; -1 != (n = in.read(buffer));) {
                output.write(buffer, 0, n);
            }
            return output.toString();
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将字符串数据存于本地.
     * 
     * @param fileName
     *            文件名
     * @param data
     *            数据
     * @param encode
     *            编码（可为null）
     * @throws IOException
     *             IO异常
     */
    public static void writeStringToFile(String fileName, String data, String encode) throws IOException {
        FileOutputStream output = null;
        File file = new File(fileName);
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("File '" + file + "' could not be created");
            }
        }
        try {
            output = new FileOutputStream(file);
            if (data != null) {
                if (encode == null) {
                    output.write(data.getBytes());
                } else {
                    output.write(data.getBytes(encode));
                }
            }
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
