package bot.utilities;

import net.dv8tion.jda.api.entities.Message;

//wrapper for file attachments which can be provided either as an attachment or another argument
public class AttachedFile {
    public String url;
    public String name; //full name with extension
    public String extension;

    private AttachedFile() {
    }

    public static AttachedFile parse(String url) {
        AttachedFile file = new AttachedFile();
        file.url = url;
        file.name = lastSegment(url);
        int dot = file.name.indexOf('.');
        file.extension = file.name.substring(dot + 1);
        return file;
    }

    public static AttachedFile fromAttachment(Message.Attachment attachment) {
        AttachedFile file = new AttachedFile();
        file.url = attachment.getUrl();
        file.name = attachment.getFileName();
        file.extension = attachment.getFileExtension();
        return file;
    }

    private static String lastSegment(String url) {
        char[] arr = url.toCharArray();
        for (int i = arr.length - 1; i > -1; i--) {
            if (arr[i] == '\\' || arr[i] == '/') {
                return url.substring(i + 1);
            }
        }
        throw new IllegalStateException("Slash separator not present, panicking");
    }

    @Override
    public String toString() {
        return "AttachedFile{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", extension='" + extension + '\'' +
                '}';
    }
}
