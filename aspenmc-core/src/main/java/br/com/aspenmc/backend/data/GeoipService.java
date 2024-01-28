package br.com.aspenmc.backend.data;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.entity.ip.IpInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public interface GeoipService {

    static IpInfo retrieveIp(String ip) {
        try {
            URLConnection con = new URL("https://ipinfo.io/" + ip + "?token=6d31b903236e8c").openConnection();
            return CommonConst.GSON.fromJson(
                    new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)),
                    IpInfo.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    CompletableFuture<IpInfo> getIp(String ip);
}
