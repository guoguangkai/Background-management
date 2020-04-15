package com.yingxue.lesson.utils;

/**
 * @ClassName: CodeUtil
 * 编码工具类
 */
public class CodeUtil {

    private static final String DEPT_TPYE="YXD";
    private static final String PERMISSION_TPYE="YXP";
    /**
     * 右补位，左对齐
     */
    public  static String padRight(String oriStr,int len,String alexin){
        String str = "";
        int strlen = oriStr.length();
        if(strlen < len){
            for(int i=0;i<len-strlen;i++){
                str=str+alexin;
            }
        }
        str=str+oriStr;
        return str;
    }
    /**
     * 获取机构编码 YXD0001
     */
    public static String deptCode(String oriStr,int len,String alexin){
        return DEPT_TPYE+padRight(oriStr, len, alexin);
    }

}
