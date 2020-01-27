/**
 * @ClassName RSAPriKey
 * @Description TODO
 * @Author zhengxin
 * @Date 2020/1/19 15:14
 */
public class RSAPriKey extends RSAKey {
    private int numberD;

    public int getNumberD() {
        return numberD;
    }

    public void setNumberD(int numberD) {
        this.numberD = numberD;
    }

    @Override
    public String toString() {
        return "RSAPriKey{" +
                "numberD=" + numberD +
                '}';
    }
}
