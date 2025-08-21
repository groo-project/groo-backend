package com.x1.groo.discord.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DiscordPayloadDTO {

    private final DiscordEmbedDTO[] embeds;
}
