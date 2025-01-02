package ms.netty.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

    /**
     *  UIHandler class is supporting class.
     *  It imitates user interface, operating with database etc.
     *  In ur project u have to implement it by ur own way.
     * **/

public class UIHandler {

    private static String logdata = "lolipops:qwerty12223";


    public static String YesOrNotQuestion(String q) {
        Scanner in = new Scanner(System.in);
        System.out.print(q);
        return in.next();
    }

    public static String requestLogData() {
        Scanner in = new Scanner(System.in);
        System.out.print("Please enter ur login and password" + "\nlogin: ");
        String l = in.next();
        System.out.println("\npassword: ");
        String p = in.next();
        return l + ":" + p.replace(":", "_"); // replacing illegal char
    }

    public static void saveTokensInFile(String at, String rt) throws IOException {
        try (PrintWriter writer = new PrintWriter("src/main/java/ms/netty/client/tokens", StandardCharsets.UTF_8)) {
            writer.println("accessToken: " + at);
            writer.println("refreshToken: " + rt);
        }
    }

    public static void refreshAccessTokenInFile(String at) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"))) {
            String rt = reader.readLine().split(" ")[1];
            saveTokensInFile(at, rt);
        }
    }

    public static String getMacAddress() throws UnknownHostException, SocketException {
        byte[] hardwareAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
        String[] hexadecimal = new String[hardwareAddress.length];
        for (int i = 0; i < hardwareAddress.length; i++) {
            hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
        }
        return String.join("-", hexadecimal);
    }

    public static String[] getAccessAndRefreshTokens() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/main/java/ms/netty/client/tokens"));
        String at = reader.readLine();
        String rt = reader.readLine();
        reader.close();

        if (at != null && rt != null) {
            at = at.split(" ")[1];
            rt = rt.split(" ")[1];
        }

        return new String[]{at, rt};
    }

    public static String getLogdata() {
        return logdata;
    }

    public static void setLogdata(String logdata) {
        UIHandler.logdata = logdata;
    }
}
