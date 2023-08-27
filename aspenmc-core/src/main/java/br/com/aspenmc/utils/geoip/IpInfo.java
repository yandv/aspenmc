package br.com.aspenmc.utils.geoip;

import br.com.aspenmc.CommonConst;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

@Getter
public class IpInfo {

    private String ip;
    private String country;
    private String region;
    private String city;
    private String timezone;

    private String status = "success";

    public static IpInfo retrieveIp(String ip) throws IOException {
        URLConnection con = new URL("https://ipinfo.io/" + ip + "?token=6d31b903236e8c").openConnection();

        IpInfo ipInfo = CommonConst.GSON.fromJson(
                new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)), IpInfo.class);

        if (!"success".equals(ipInfo.getStatus())) throw new IOException();

        return ipInfo;
    }

    public static void main(String[] args) throws IOException {
        IpInfo ipInfo = retrieveIp("186.221.6.225");

        System.out.println(CommonConst.GSON.toJson(ipInfo));
    }
}
