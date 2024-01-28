package br.com.aspenmc.backend.data;

import br.com.aspenmc.entity.sender.member.MemberConnection;

import java.util.concurrent.CompletableFuture;

public interface ConnectionData {

    CompletableFuture<MemberConnection> retrieveConnection(String playerName);

    void cacheConnection(MemberConnection memberConnection);
}
