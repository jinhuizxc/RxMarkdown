/*
 * Copyright (C) 2016 yydcdut (yuyidong2015@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yydcdut.rxmarkdown.syntax.text;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.syntax.SyntaxKey;
import com.yydcdut.rxmarkdown.utils.CharacterProtector;

import java.util.regex.Pattern;

/**
 * The implementation of syntax for bold.
 * syntax:
 * "**content**"
 * "__content__"
 * <p>
 * Created by yuyidong on 16/5/3.
 */
class BoldSyntax extends TextSyntaxAdapter {

    public BoldSyntax(@NonNull RxMDConfiguration rxMDConfiguration) {
        super(rxMDConfiguration);
    }

    @Override
    boolean isMatch(@NonNull String text) {
        if (!text.contains(SyntaxKey.KEY_BOLD) && !text.contains(SyntaxKey.KEY_BOLD_1)) {
            return false;
        }
        boolean match = false;
        Pattern pattern = Pattern.compile(".*[\\*]{2}.*[\\*]{2}.*");
        Pattern pattern1 = Pattern.compile(".*[_]{2}.*[_]{2}.*");
        match |= pattern.matcher(text).matches();
        if (match) {
            return true;
        }
        match |= pattern1.matcher(text).matches();
        return match;
    }

    @NonNull
    @Override
    boolean encode(@NonNull SpannableStringBuilder ssb) {
        boolean isHandledBackSlash = false;
        isHandledBackSlash |= replace(ssb, SyntaxKey.KEY_BOLD_BACKSLASH_VALUE, CharacterProtector.getKeyEncode());
        isHandledBackSlash |= replace(ssb, SyntaxKey.KEY_BOLD_BACKSLASH_VALUE_1, CharacterProtector.getKeyEncode1());
        return isHandledBackSlash;
    }

    @NonNull
    @Override
    SpannableStringBuilder format(@NonNull SpannableStringBuilder ssb) {
        ssb = parse(SyntaxKey.KEY_BOLD, ssb.toString(), ssb);
        return parse(SyntaxKey.KEY_BOLD_1, ssb.toString(), ssb);
    }

    @NonNull
    @Override
    void decode(@NonNull SpannableStringBuilder ssb) {
        replace(ssb, CharacterProtector.getKeyEncode(), SyntaxKey.KEY_BOLD_BACKSLASH_VALUE);
        replace(ssb, CharacterProtector.getKeyEncode1(), SyntaxKey.KEY_BOLD_BACKSLASH_VALUE_1);
    }

    /**
     * parse
     *
     * @param key  {@link SyntaxKey#KEY_BOLD} or {@link SyntaxKey#KEY_BOLD_1}
     * @param text the original content,the class type is {@link String}
     * @param ssb  the original content,the class type is {@link SpannableStringBuilder}
     * @return the content after parsing
     */
    private SpannableStringBuilder parse(@NonNull String key, @NonNull String text, @NonNull SpannableStringBuilder ssb) {
        int keyLength = key.length();
        SpannableStringBuilder tmp = new SpannableStringBuilder();
        String tmpTotal = text;
        while (true) {
            int positionHeader = findPosition(key, tmpTotal, ssb, tmp);
            if (positionHeader == -1) {
                tmp.append(tmpTotal.substring(0, tmpTotal.length()));
                break;
            }
            tmp.append(tmpTotal.substring(0, positionHeader));
            int index = tmp.length();
            tmpTotal = tmpTotal.substring(positionHeader + keyLength, tmpTotal.length());
            int positionFooter = findPosition(key, tmpTotal, ssb, tmp);
            if (positionFooter != -1) {
                ssb.delete(tmp.length(), tmp.length() + keyLength);
                tmp.append(tmpTotal.substring(0, positionFooter));
                ssb.setSpan(new StyleSpan(Typeface.BOLD), index, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.delete(tmp.length(), tmp.length() + keyLength);
            } else {
                tmp.append(key);
                tmp.append(tmpTotal.substring(0, tmpTotal.length()));
                break;
            }
            tmpTotal = tmpTotal.substring(positionFooter + keyLength, tmpTotal.length());
        }
        return ssb;
    }

    /**
     * find the position of next "**" or "__"
     * ignore the "**" and "__" in inline code syntax,
     *
     * @param tmpTotal the original content, the class type is {@link String}
     * @param ssb      the original content, the class type is {@link SpannableStringBuilder}
     * @param tmp      the content that has parsed
     * @return the next position of "**" or "__"
     */
    private int findPosition(@NonNull String key, @NonNull String tmpTotal, @NonNull SpannableStringBuilder ssb, @NonNull SpannableStringBuilder tmp) {
        String tmpTmpTotal = tmpTotal;
        int position = tmpTmpTotal.indexOf(key);
        if (position == -1) {
            return -1;
        } else {
            if (checkInInlineCode(ssb, tmp.length() + position, key.length())) {//key是否在inlineCode中
                StringBuilder sb = new StringBuilder(tmpTmpTotal.substring(0, position))
                        .append("$$").append(tmpTmpTotal.substring(position + key.length(), tmpTmpTotal.length()));
                return findPosition(key, sb.toString(), ssb, tmp);
            } else {
                return position;
            }
        }
    }
}
