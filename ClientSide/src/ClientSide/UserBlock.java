package ClientSide;

import java.util.Objects;

/**
 * ClientSide
 * Created by Hieu Tran Trung
 * Date 12/15/2021 - 9:52 PM
 * Description: ...
 */
public class UserBlock {
    private final String username;
    private final boolean isNewMessage;

    public UserBlock(String username) {
        this.username = username;
        isNewMessage = false;
    }

    public UserBlock(String username, boolean isNewMessage) {
        this.username = username;
        this.isNewMessage = isNewMessage;
    }

    public String getUsername() {
        return username;
    }

    public boolean isNewMessage() {
        return isNewMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        UserBlock userBlock = (UserBlock) o;
        return getUsername().equals(userBlock.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername());
    }
}
