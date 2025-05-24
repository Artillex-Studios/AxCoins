package com.artillexstudios.axcoins.placeholders;

import com.artillexstudios.axapi.placeholders.PlaceholderArgumentResolver;
import org.jetbrains.annotations.Nullable;

public class IntegerResolver implements PlaceholderArgumentResolver<Integer> {

    @Nullable
    @Override
    public Integer resolve(String string) {
        return Integer.parseInt(string);
    }
}
