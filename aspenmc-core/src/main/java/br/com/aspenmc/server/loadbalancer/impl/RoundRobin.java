package br.com.aspenmc.server.loadbalancer.impl;

import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.loadbalancer.BaseBalancer;

public class RoundRobin<T extends ProxiedServer> extends BaseBalancer<T> {

	private int next = 0;

	@Override
	public T next() {
		T obj = null;
		if (nextObj != null && !nextObj.isEmpty()) {
			while (next < nextObj.size()) {
				obj = nextObj.get(next);
				++next;
				if (obj == null)
					continue;
				if (!obj.canBeSelected()) {
					obj = null;
					continue;
				}
				break;
			}
		}
		if (next + 1 >= nextObj.size())
			next = 0;
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
