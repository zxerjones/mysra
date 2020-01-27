import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/***
 *
 * @Data 2020年1月11日17:07:51
 * @author zxerjones
 *
 * des: 生成RSA秘钥对，分为以下几个步骤：
 * 1： 随机挑选两个个质数p, q(越大越好，这里只是模拟生成，所以质数选择在小范围以内挑选)，:
 *     对两个质数求积， 生成的数字为N  ,及 N = p * q
 * 2:  计算r在这里代表的意思是 ：区间从[1, N)区间内与N互质的数的个数，
 *      。求出 φ(n),根据欧拉函数性质，可以求得：
 *     r = φ(N) = φ(p) * φ(q) = (p - 1) * (q - 1),
 * 3： ->>欧几里得算法计算d, 1 <  e < r, （e和r是互质的关系） ；
 *     私钥是根据公钥计算出的公式：  ->> 根据扩展欧几里得算法，求乘法逆元，e * d ≡ 1 (mod r)
 *      这个公式等价为：(e * d - 1) % r = 0 （存在一个数d，使等成立）。
 *
 * 4： 这样秘钥对就已经出现了 私钥：(N ,e), 公钥：（N，r）
 *
 *
 *     m: 明文  c：密文
 *   加解密时有这样的规则: 1：明文为m的必须小于N，
 *                      2： 加密公式： c ≡ m^r mod(N)  等价于： m^r % N = c  ，注意%代表模运算，即求余数。
 *                      3. 解密公式： m = c^d mod(N)  等价于:   c^d % N = m
 *
 *  加密过程中会遇到结果过大的情况，如明文c为113，公钥为1134  此时 m^r会很大，解决方法有两种：
 *                      1：使用java BigInteger类内置的方法。
 *                      2：蒙哥马利幂模运算。本文中采用蒙哥马利幂模运算
 *
 *
 *   取模运算意味着：每次加密的长度不能超过N ，在实际应用中，秘钥长度经常取1024， 2048 ，
 *                  也意味着加密的数据长度不能超过秘钥长度， 也就是对应的128字节和256字节，
 *                  实际应用中，会使用分段加密的方式对明文加密，并且采用填充的方式
 *                 每次加密的实际数据也不可能刚好是128字节，256字节，，
 *                 具体的填充方式可以参阅： http://www.faqs.org/rfcs/rfc2313.html
 *
 *
 *
 *   此代码只是模拟RSA加密算法做一个小练习，不完善和不合理的地方有很多，欢迎大家提出宝贵的意见
 */
public class RSAUtils {

    /**
     * 模拟RSA运算过程中随机成成的数
     */
    private static Random random = new Random();


    public static void main(String[] args) {
        int failCount = 0;
        int totalCount = 0;
        // 模拟加密100次，以防偶然性
        for (int i = 0; i < 100; i++) {
            // 获取秘钥对
            Map<String, RSAKey> map = generateKeyMap();
            RSAPriKey priKey = (RSAPriKey) map.get("privateKey");
            RSAPubKey pubKey = (RSAPubKey) map.get("publicKey");

            // 模拟需要加密数据
            Student zxerjones = new Student();
            zxerjones.setAge(random.nextInt(100));
            zxerjones.setDes("3a!^D`~&*#@%$^有位非常漂亮的女同事，有天起晚了没有时间化妆便急忙冲到公司。结果那天她被记旷工了……吃惊");
            zxerjones.setName("zxerjones");
            String str = JSON.toJSONString(zxerjones);

            System.out.println("加密前字符串: " + str);
            String enStr = getEnString(str, pubKey);
            System.out.println("加密之后的二进制串:" + enStr);
            String deStr = getDeString(enStr, priKey);
            System.out.println("解密后字符串: " + deStr);
            if (!deStr.equals(str)) {
                failCount++;
            }
            System.out.println("----------------------------");
            totalCount++;

        }
        System.out.println("-------------------总共失败了： " +  failCount +  " 次-----------");
        System.out.println("-------------------总共加密了： " +  totalCount +  " 次-----------");
    }

    /**
     * 生成公私钥
     * @return 公私钥
     */
    public static Map<String, RSAKey> generateKeyMap() {
        PrimeUtils primeUtils = PrimeUtils.getInstance();
        // 随机生成质数对
        List<Integer> pairPrime = primeUtils.getRandomPairPrime();
        // 计算N
        int numberN = pairPrime.get(1) * pairPrime.get(0);
        // L根据欧拉函数特性： φ(N) = φ(p) * φ(q) = (p - 1) * (q - 1)
        int numberL = (pairPrime.get(0) - 1) * (pairPrime.get(1) - 1);
        // 随机选择d，d为区间[1, numberL)与numberL互质的数,即最大公约数为1的数
        int numberE;
        int numberD;
        do {
            // 运用欧几里得算法，即最大公约数为1的数
            numberE = getRandomGCD(numberL);
            //  运用扩展欧几里得算法求乘法逆元 (e * d - 1) % L = 0
            numberD = getNumberD(numberE, numberL);
        } while (numberD < 0 ); // 扩展欧几里得算法解存在负数,秘钥不选用负数，
        return getPairKeyInMap(numberN, numberE, numberD);
    }

    /**
     * 生成键值对
     * @param numberN
     * @param numberE
     * @param numberD
     * @return  公私钥map
     */
    private static Map<String, RSAKey> getPairKeyInMap(int numberN, int numberE, int numberD) {
        RSAPriKey rsaPriKey = new RSAPriKey();
        rsaPriKey.setNumberN(numberN);
        rsaPriKey.setNumberD(numberD);
        RSAPubKey rsaPubKey = new RSAPubKey();
        rsaPubKey.setNumberN(numberN);
        rsaPubKey.setNumberE(numberE);

        HashMap<String, RSAKey> keyMap = new HashMap<>();
        keyMap.put("privateKey", rsaPriKey);
        keyMap.put("publicKey", rsaPubKey);
        return keyMap;

    }

    /**
     * 扩展欧几里得算法算出的解集中会存在负数，在
     * RSA加密中对于私钥D 不允许出现负数的情况，
     * 下面的方法会避免出现负数
     *
     * @param numberE 随机选择的公钥
     * @param numberL L
     * @return 乘法逆元
     */
    private static int getNumberD(int numberE, int numberL) {
        return getNumberdDetail(numberE, numberL);
    }

    /**
     * 获取numberD 的乘法逆元
     * @param numberE
     * @param numberL
     * @return  满足乘法逆元的数
     */
    public static int getNumberdDetail(int numberE, int numberL) {
        List<Integer> xArr = new ArrayList<>();  // 保存x的递归解集
        List<Integer> yArr = new ArrayList<>();  // 保存y的递归解集
        extgcd(numberE, numberL, xArr, yArr);  // xArr最后加入的元素就是numberD
        int numberD = xArr.get(xArr.size() - 1);
        return xArr.get(xArr.size() - 1);
    }


    /**
     * 欧几里得算法，
     * @param numberL 区间
     * @return
     */
    private static int getRandomGCD(int numberL) {
        List<Integer> gcdList = IntStream.range(2, numberL)
                .filter(e -> GCD(numberL, e) == 1)  // 选取最大公约数为1的两个数
                .boxed()
                .collect(Collectors.toList());
        return gcdList.get(random.nextInt(gcdList.size()));
    }

    /**
     * 欧几里得算法，查找最大公约数
     * @param e   除数
     * @param num 被除数
     * @return 最大公约数
     */
    private static int GCD(int num, int e) {
        if (num % e == 0) {
            return e;
        } else {
            return GCD(e, num % e);
        }
    }


    /**
     * 扩展欧几里得算法， 求乘法逆元
     *
     * @param a 除数
     * @param b 被除数
     * @return 最大公约数
     */
    private static int extgcd(int a, int b, List<Integer> xArr, List<Integer> yArr) {
        if (b == 0) {
            xArr.add(1);
            yArr.add(0);
            return a;
        }
        int ans = extgcd(b, a % b, xArr, yArr);
        int t = xArr.get(xArr.size() - 1);
        xArr.add(yArr.get(yArr.size() - 1));
        yArr.add(t - (a / b) * yArr.get(yArr.size() - 1));
        return ans;
    }

    /**
     * 私钥解密
     * @param enStr 密文二进制串
     * @param priKey  私钥
     * @return  明文
     */
    public static String getDeString(String enStr, RSAPriKey priKey) {
        int enLen = Integer.toBinaryString(priKey.getNumberN()).length();
        // deArr,存放解密后的字节数组。numberN的二进制字符串长度
        byte[] deArr = new byte[enStr.length() / enLen];
        for (int i = 0; i < enStr.length(); i += enLen) {
            short deElem = Short.valueOf(enStr.substring(i, i + enLen), 2);
            // JDK内部幂模预算
//            BigInteger me = new BigInteger(Short.toString(deElem))
//                    .pow(priKey.getNumberD()).mod(new BigInteger(Integer.toString(priKey.getNumberN())));
//            byte elem = (byte) me.intValue();
            // 蒙哥马利幂模运算
            int meRes = modmul(deElem, priKey.getNumberD(), priKey.getNumberN());
            // 强转为byte类型，恢复原始字节最高位代表的符号位
            byte elem = (byte) meRes;
            deArr[i / enLen] = elem;
        }
        return new String(deArr, StandardCharsets.UTF_8);
    }

    /**
     * 加密明文
     * @param a 明文
     * @return 加密后二进制字符串
     */
    public static String getEnString(String a, RSAPubKey pubKey) {
        // 获取对应的字节流
        byte[] bytes = a.getBytes(StandardCharsets.UTF_8);
        // 已字节为单位的加密长度
        int Nlen = Integer.toBinaryString(pubKey.getNumberN()).length();
        StringBuilder stringBuilder = new StringBuilder();
        // 对字节进行逐个加密
        for (byte b : bytes) {
//            JDK内部幂模运算
//            BigInteger bi = new BigInteger(Integer.toString(Byte.toUnsignedInt(b)));
//            BigInteger encodeElem = bi.pow(pubKey.getNumberE()).mod(new BigInteger(Integer.toString(pubKey.getNumberN())));
//            String elemBinStr = getShortBinStr(encodeElem.intValue(), Nlen);

            // 蒙哥马利幂模运算，求余数。加密字节, 特殊符和汉字会得到对应的负数，
            // 这里采用转换为无符号整型存储，解密时强转为byte（只留低八位）
            int molRes = modmul(Byte.toUnsignedInt(b), pubKey.getNumberE(), pubKey.getNumberN());
            String elemBinStr = getShortBinStr(molRes, Nlen);
            stringBuilder.append(elemBinStr);
        }
        return stringBuilder.toString();
    }

    /**
     * 获取对应的二进制字符串，
     *
     * @param encodeElem 加密后的字节
     * @param Nlen       numberN二进制字符串长度
     * @return 长度为Nlen的密文二进制
     */
    public static String getShortBinStr(int encodeElem, int Nlen) {
//        int valElem = encodeElem.intValue();
        String valTemp = Integer.toBinaryString(encodeElem);
        int len = valTemp.length();
        // 不足Nlen的部分补0
        for (int i = 0; i < Nlen - len; i++) {
            valTemp = "0" + valTemp;
        }
        return valTemp;
    }

    /**
     * 蒙哥马利幂模运算
     * @param base    底数
     * @param pow     指数
     * @param numberN 除数
     * @return 模结果
     */
    public static int modmul(int base, int pow, int numberN) {
        int res = 1;
        base = base % numberN;
        while (pow > 0) {
            if (pow % 2 == 1) {
                res = (res * base) % numberN;
            }
            base = (base * base) % numberN;
            pow = pow >> 1;
        }
        return res;
    }


}
