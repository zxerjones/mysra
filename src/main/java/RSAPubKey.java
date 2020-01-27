/**
 * @ClassName RSAPubKey
 * @Description TODO
 * @Author zhengxin
 * @Date 2020/1/19 15:14
 */
public class RSAPubKey extends RSAKey {
    private int numberE;

    public int getNumberE() {
        return numberE;
    }

    public void setNumberE(int numberE) {
        this.numberE = numberE;
    }

    @Override
    public String toString() {
        return "RSAPubKey{" +
                "numberE=" + numberE +
                '}';
    }
}
