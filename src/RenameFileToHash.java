import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class RenameFileToHash {
    private final Scanner scanner;
    private File[] listFiles;
    private MessageDigest messageDigest;
    private String[] option;
    private int radix, numberMask, number, change;
    private boolean sort;

    private RenameFileToHash() {
        scanner = new Scanner(System.in);
    }

    public RenameFileToHash(String[] arguments) {
        this();
        option = arguments;
        setAlgorithm();
        setListFiles();
    }

    public static void main(String[] args) {
        final RenameFileToHash renameFileToHash = new RenameFileToHash(args);
        File newFile;
        for (File oldFile : renameFileToHash.getListFiles()) {
            newFile = renameFileToHash.getNewFile(oldFile, renameFileToHash.getHash(oldFile));
            if (!renameFileToHash.isDuplicate(oldFile, newFile) && oldFile.renameTo(newFile)) {
                System.out.printf("%s is renamed to %s.\n", oldFile.getName(), newFile.getName());
            }
        }
        System.out.printf("%s of %s files renamed.\n", renameFileToHash.getChange(), renameFileToHash.getListFiles().length);
    }

    public int getChange() {
        return change;
    }

    public String getHash(File file) {
        final byte[] bytes = new byte[1024];
        FileInputStream fileInputStream = null;
        String hash;
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
        hash = new BigInteger(1, messageDigest.digest()).toString(16);
        if (hash.length() != radix) {
            hash = String.format("%0" + (radix - hash.length()) + "d%s", 0, hash);
        }
        return hash;
    }

    public File[] getListFiles() {
        return listFiles;
    }

    public File getNewFile(File file, String hash) {
        final StringBuilder newName = new StringBuilder();
        newName.append(file.getPath().substring(0, file.getPath().lastIndexOf(file.getName())));
        if (sort) {
            newName.append(String.format("%0" + numberMask + "d-", ++number));
        }
        newName.append(hash);
        if (file.getName().lastIndexOf(".") != -1) {
            newName.append(file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase());
        }
        return new File(newName.toString());
    }

    public boolean isDuplicate(File oldName, File newName) {
        if (newName.exists()) {
            if (!newName.getName().equals(oldName.getName())) {
                System.out.printf("Found duplicate %s: %s.\n", newName.getName(), oldName.getName());
            }
            return true;
        }
        change++;
        return false;
    }

    private String searchOperand(String search) {
        try {
            for (int i = 0; i < option.length; i++) {
                if (option[i].equals(search)) {
                    if (option[i].equals("-n")) {
                        return option[i];
                    }
                    return option[i + 1];
                }
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            System.out.printf("%s: missing operand.\n", search);
        }
        return null;
    }

    private void setAlgorithm() {
        final String algorithm = searchOperand("-a");
        try {
            if (algorithm != null) {
                messageDigest = MessageDigest.getInstance(algorithm);
            }
        } catch (NoSuchAlgorithmException exception) {
            System.out.printf("%s unsupported. Selected MD5.\n", algorithm);
        } finally {
            if (messageDigest == null) {
                try {
                    messageDigest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException exception) {
                    exception.printStackTrace();
                }
            }
        }
        radix = messageDigest.digest().length * 2;
    }

    private void setListFiles() {
        File path = option.length != 0 ? new File(option[option.length - 1]) : new File(".");
        while (!path.isFile() && !path.isDirectory()) {
            System.out.printf("No such file or directory.\nPath: ");
            path = new File(scanner.nextLine());
        }
        if (path.isFile()) {
            listFiles = new File[]{path};
        } else {
            listFiles = path.listFiles(new FileFilter() {
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
            Arrays.sort(listFiles);
            numberMask = ("" + listFiles.length).length();
            sort = searchOperand("-n") != null;
        }
    }
}