package ClientSide;

/**
 * ClientSide
 * Created by Hieu Tran Trung
 * Date 12/14/2021 - 9:12 PM
 * Description: ...
 */
public interface UserListener {
    public void handleOnline(String username);

    public void handleOffline(String username);
}
