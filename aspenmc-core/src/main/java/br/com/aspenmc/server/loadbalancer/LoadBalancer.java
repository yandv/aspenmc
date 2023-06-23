package br.com.aspenmc.server.loadbalancer;

import br.com.aspenmc.server.ProxiedServer;

public interface LoadBalancer<T extends ProxiedServer> {

	public T next();

}
