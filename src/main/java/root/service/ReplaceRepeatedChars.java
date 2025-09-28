package root.service;

import org.springframework.stereotype.Service;

/**
 * @author ChatGPT
 */
@Service
public class ReplaceRepeatedChars {
    void appendChars(StringBuilder sb, char ch, int count, char[] targets) {
        if (contains(targets, ch) && count >= 3) {
            sb.append(String.valueOf(ch).repeat(2)); // 2번만 추가
        } else {
            sb.append(String.valueOf(ch).repeat(count)); // 원래 개수만큼 추가
        }
    }
    boolean contains(char[] arr, char target) {
        for (char c : arr) {
            if (c == target) return true;
        }
        return false;
    }
    public String replaceRepeatedChars(String input, char[] targets) {
        StringBuilder sb = new StringBuilder();

        if (input.isEmpty()) return input;

        char prev = input.charAt(0);
        int count = 1;

        for (int i = 1; i < input.length(); i++) {
            char current = input.charAt(i);
            if (current == prev && contains(targets, current)) {
                count++;
            } else {
                // 처리 로직
                appendChars(sb, prev, count, targets);
                prev = current;
                count = 1;
            }
        }

        // 마지막 문자 처리
        appendChars(sb, prev, count, targets);

        return sb.toString();
    }
}
