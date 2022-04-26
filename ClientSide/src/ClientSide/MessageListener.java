package ClientSide;

import ClientSide.FileTransfer.FileBlock;

/**
 * ClientSide
 * Created by Hieu Tran Trung
 * Date 12/14/2021 - 9:54 PM
 * Description: ...
 */
public interface MessageListener {
    void handleMessage(String username, String body);

    void handleFile(String username, FileBlock file);
}
