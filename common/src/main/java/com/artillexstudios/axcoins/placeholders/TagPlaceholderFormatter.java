package com.artillexstudios.axcoins.placeholders;

import com.artillexstudios.axapi.placeholders.DefaultPlaceholderFormatter;
import com.artillexstudios.axapi.placeholders.PlaceholderFormatter;

import java.util.ArrayList;
import java.util.List;

public enum TagPlaceholderFormatter implements PlaceholderFormatter {
    INSTANCE;

    @Override
    public List<String> format(String placeholder) {
        String raw = placeholder.replace("%", "");

        List<String> formatted = new ArrayList<>(DefaultPlaceholderFormatter.INSTANCE.format(placeholder));
        formatted.add("<" + raw + ">");
        return formatted;
    }
}