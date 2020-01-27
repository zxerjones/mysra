/**
 * @ClassName RSAKey
 * @Description TODO
 * @Author zhengxin
 * @Date 2020/1/19 14:57
 */
public class RSAKey {
    private int numberN;

    public int getNumberN() {
        return numberN;
    }

    public void setNumberN(int numberN) {
        this.numberN = numberN;
    }

    @Override
    public String toString() {
        return "RSAKey{" +
                "numberN=" + numberN +
                '}';
    }
}
