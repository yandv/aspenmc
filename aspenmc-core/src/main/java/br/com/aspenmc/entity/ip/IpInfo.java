package br.com.aspenmc.entity.ip;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IpInfo {

    private String ip;
    private String hostname;

    private String country;
    private String region;
    private String city;

    private String timezone;
}
