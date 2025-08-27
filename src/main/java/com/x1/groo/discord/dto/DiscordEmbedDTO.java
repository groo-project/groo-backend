package com.x1.groo.discord.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DiscordEmbedDTO {

    private final String title;
    private final String description;
    private final int color;
}
