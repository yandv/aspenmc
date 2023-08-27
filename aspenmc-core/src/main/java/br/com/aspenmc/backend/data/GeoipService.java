package br.com.aspenmc.backend.data;

import br.com.aspenmc.utils.geoip.IpInfo;

import java.util.concurrent.CompletableFuture;

public interface GeoipService {

    CompletableFuture<IpInfo> getIp(String ip);

}
