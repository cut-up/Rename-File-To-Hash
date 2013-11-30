import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class RenameFileToHash {
    private final Scanner scanner;
    private int advance;
    private File[] listFiles;
    private MessageDigest messageDigest;

    private RenameFileToHash() {
        this.scanner = new Scanner(System.in);
    }

    public RenameFileToHash(String[] arguments) {
        this();
        this.setAlgorithm(arguments);
        this.setListFiles(arguments);
    }

    public static void main(String[] args) {
        final RenameFileToHash renameToHash = new RenameFileToHash(args);
        File newFile;
        for (File oldFile : renameToHash.getListFiles()) {
            newFile = renameToHash.getNewFile(oldFile, renameToHash.getHash(oldFile));
            if (!renameToHash.isDuplicate(oldFile, newFile) && oldFile.renameTo(newFile)) {
                System.out.printf("%s is renamed to %s.\n", oldFile.getName(), newFile.getName());
            }
        }
        System.out.printf("%s of %s files renamed.\n", renameToHash.getAdvance(), renameToHash.getListFiles().length);
    }

    public int getAdvance() {
        return this.advance;
    }

    public String getHash(File file) {
        final byte[] bytes = new byte[1024];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file.getPath());
            for (int read; (read = fileInputStream.read(bytes)) != -1; ) {
                messageDigest.update(bytes, 0, read);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
        return new BigInteger(1, messageDigest.digest()).toString(16);
    }

    public File[] getListFiles() {
        return this.listFiles;
    }

    public File getNewFile(File file, String hash) {
        String newName = file.getPath().substring(0, file.getPath().lastIndexOf(file.getName())) + hash;
        if (file.getName().lastIndexOf(".") != -1) {
            return new File(newName + file.getName().substring(file.getName().lastIndexOf(".")));
        }
        return new File(newName);
    }

    public boolean isDuplicate(File oldName, File newName) {
        if (newName.exists()) {
            if (!newName.getName().equals(oldName.getName())) {
                System.out.printf("Found duplicate %s: %s.\n", newName.getName(), oldName.getName());
            }
            return true;
        }
        this.advance++;
        return false;
    }

    private void setAlgorithm(String[] arguments) {
        try {
            if (arguments.length > 1) {
                this.messageDigest = MessageDigest.getInstance(arguments[1]);
            }
        } catch (NoSuchAlgorithmException exception) {
            System.out.printf("%s unsupported. Selected MD5.\n", arguments[1]);
        } finally {
            if (this.messageDigest == null) {
                try {
                    this.messageDigest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void setListFiles(String[] args) {
        File path = args.length != 0 ? new File(args[0]) : new File(".");
        while (!path.isFile() && !path.isDirectory()) {
            System.out.printf("No such file or directory.\nPath: ");
            path = new File(scanner.nextLine());
        }
        if (path.isFile()) {
            this.listFiles = new File[]{path};
        } else {
            this.listFiles = path.listFiles(new FileFilter() {
                private String answer = "";

                public boolean accept(File file) {
                    if (file.isFile()) {
                        if (!answer.matches("^(?i)[A](ll)?")) {
                            System.out.printf("Rename %s? [Yes/No/All] ", file.getName());
                            return (answer = scanner.nextLine()).matches("^(?i)[Y](es)?|[A](ll)?");
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}