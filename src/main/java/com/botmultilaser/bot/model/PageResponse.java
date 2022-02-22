package com.botmultilaser.bot.model;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> data = null;

}
