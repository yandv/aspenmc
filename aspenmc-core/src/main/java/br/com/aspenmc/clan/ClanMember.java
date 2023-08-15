package br.com.aspenmc.clan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ClanMember {

    private UUID playerId;
    @Setter
    private String lastName;

    @Setter
    private ClanRole role;
}
