package my.common.util;

import java.util.Map;

public class BankCodeResolver {

    private static final Map<String, String> BANK_CODE_MAP = Map.ofEntries(
            Map.entry("신한은행", "88"),
            Map.entry("신한", "88"),
            Map.entry("국민은행", "06"),
            Map.entry("국민", "06"),
            Map.entry("우리은행", "20"),
            Map.entry("우리", "20"),
            Map.entry("하나은행", "81"),
            Map.entry("하나", "81"),
            Map.entry("농협은행", "11"),
            Map.entry("농협", "11"),
            Map.entry("기업은행", "03"),
            Map.entry("기업", "03"),
            Map.entry("SC제일은행", "23"),
            Map.entry("제일은행", "23"),
            Map.entry("카카오뱅크", "90"),
            Map.entry("카카오", "90"),
            Map.entry("케이뱅크", "89"),
            Map.entry("토스뱅크", "92"),
            Map.entry("토스", "92"),
            Map.entry("수협은행", "07"),
            Map.entry("수협", "07"),
            Map.entry("대구은행", "31"),
            Map.entry("대구", "31"),
            Map.entry("부산은행", "32"),
            Map.entry("부산", "32"),
            Map.entry("광주은행", "34"),
            Map.entry("광주", "34"),
            Map.entry("경남은행", "39"),
            Map.entry("경남", "39"),
            Map.entry("전북은행", "37"),
            Map.entry("전북", "37"),
            Map.entry("제주은행", "35"),
            Map.entry("제주", "35")
    );

    private BankCodeResolver() {
    }

    public static String resolve(String bankName) {
        return BANK_CODE_MAP.get(bankName);
    }
}
