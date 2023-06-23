package br.com.aspenmc.server.loadbalancer.impl;

import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.loadbalancer.BaseBalancer;

public class MostConnection<T extends ProxiedServer> extends BaseBalancer<T> {

	@Override
	public T next() {
		T obj = null;
		if (nextObj != null && !nextObj.isEmpty()) {
			for (T item : nextObj) {
				if (!item.canBeSelected())
					continue;
				if (obj == null) {
					obj = item;
					continue;
				}
				if (obj.getActualNumber() < item.getActualNumber())
					obj = item;
			}
		}
		return obj;
	}

	@Override
	public int getTotalNumber() {
		int number = 0;
		for (T item : nextObj) {
			number += item.getActualNumber();
		}
		return number;
	}
}
