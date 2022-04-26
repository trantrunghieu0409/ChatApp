package ClientSide.FileTransfer;

/**
 * ClientSide.FileTransfer
 * Created by Hieu Tran Trung
 * Date 12/16/2021 - 1:31 AM
 * Description: ...
 */
public class FileBlock {
    private String fileName;
    private int length;
    private byte[] content;

    public FileBlock(String fileName, int length, byte[] content) {
        this.fileName = fileName;
        this.length = length;
        this.content = content;
    }

    public int getLength() {
        return length;
    }

    public String getFileName() {
        return fileName;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
