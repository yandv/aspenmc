package br.com.aspenmc.entity.ip;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IpInfo {

    private String ip;

    private String country;
    private String region;
    private String city;

    private String timezone;

    @Override
    public String toString() {
        return "IpInfo{" + "ip='" + ip + '\'' + ", country='" + country + '\'' + ", region='" + region + '\'' +
                ", city='" + city + '\'' + ", timezone='" + timezone + '\'' + '}';
    }
}
