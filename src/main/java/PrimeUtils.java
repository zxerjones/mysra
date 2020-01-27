import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @ClassName PrimeUtils
 * @Description 质数生成器
 * @Author zhengxin,  这里只在10-100内随机选择质数
 * @Date 2020/1/19 14:34
 */
public class PrimeUtils {
    // 存放质数集合
    private static List<Integer> primeList;

    // 随机取质数的区间
    private static final int MIN_VALUE = 20;
    private static final int MAX_VALUE = 100;

    private Random random = new Random();

    private PrimeUtils() {
        primeList = generatePrime();
    }

    public static PrimeUtils getInstance() {
        return new PrimeUtils();
    }

    public static List<Integer> getPrimeList() {
        return primeList;
    }

    /**
     *  生成质数
     * @return 质数集合
     */
    private List<Integer> generatePrime () {
        return IntStream.rangeClosed(MIN_VALUE, MAX_VALUE)
                .filter(PrimeUtils::isPrime)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * 判断是否为质数
     * @param num 带判断的数
     * @return true or false
     */
    private static boolean isPrime(int num) {
        int candidateRoot = (int) Math.sqrt(num);
        return IntStream.rangeClosed(2, candidateRoot)
                .noneMatch(i -> num % i == 0);
    }

    /**
     * 随机获取质数对
     * @return 一对质数
     */
    public List<Integer> getRandomPairPrime() {
        int num = random.nextInt(primeList.size());
        List<Integer> result = new ArrayList<>();
        result.add(primeList.get(num));
        int temp;
        // 随机互不相等的两个质数
        do {
            temp = random.nextInt(primeList.size());
        } while (temp == num);

        result.add(primeList.get(temp));
        return result;
    }
}
