package br.com.aspenmc.utils.geoip;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.entity.ip.IpInfo;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public enum GeoipResolver {

    IP_INFO_IO {
        @Override
        public IpInfo resolve(String ip) {
            try {
                URLConnection con = new URL("https://ipinfo.io/" + ip + "?token=6d31b903236e8c").openConnection();
                JsonObject jsonObject = CommonConst.GSON.fromJson(
                        new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)),
                        JsonObject.class);

                if (jsonObject.has("bogon") && jsonObject.get("bogon").getAsBoolean()) {
                    return new IpInfo(ip, "Localhost", "Localhost", "Localhost", "America/Sao_Paulo");
                }

                return new IpInfo(ip, jsonObject.get("country").getAsString(), jsonObject.get("region").getAsString(),
                                  jsonObject.get("city").getAsString(), jsonObject.get("timezone").getAsString());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }, IP_API {
        @Override
        public IpInfo resolve(String ip) {
            try {
                URLConnection con = new URL("http://ip-api.com/json/" + ip).openConnection();
                JsonObject jsonObject = CommonConst.GSON.fromJson(
                        new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)),
                        JsonObject.class);

                if (jsonObject.has("status") && jsonObject.get("status").getAsString().equals("fail")) {
                    return new IpInfo(ip, "Localhost", "Localhost", "Localhost", "America/Sao_Paulo");
                }

                return new IpInfo(ip, jsonObject.get("countryCode").getAsString(),
                                  jsonObject.get("regionName").getAsString(), jsonObject.get("city").getAsString(),
                                  jsonObject.get("timezone").getAsString());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    private static int index = 0;

    public static IpInfo resolveIp(String ip) {
        GeoipResolver resolver = values()[index];

        index++;

        if (index >= values().length) {
            index = 0;
        }

        return resolver.resolve(ip);
    }

    public static void main(String[] args) {
        for (GeoipResolver value : values()) {
            System.out.println(value.resolve("186.221.6.225"));
            System.out.println(value.resolve("127.0.0.1"));
        }
    }

    public abstract IpInfo resolve(String ip);
}
