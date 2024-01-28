package br.com.aspenmc.backend.data;

import br.com.aspenmc.entity.ip.IpInfo;
import br.com.aspenmc.utils.geoip.GeoipResolver;

import java.util.concurrent.CompletableFuture;

public interface GeoipService {

    static IpInfo retrieveIp(String ip) {
        return GeoipResolver.resolveIp(ip);
    }

    CompletableFuture<IpInfo> getIp(String ip);
}
