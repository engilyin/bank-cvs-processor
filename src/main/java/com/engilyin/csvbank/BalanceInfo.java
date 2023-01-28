package com.engilyin.csvbank;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BalanceInfo implements Comparable<BalanceInfo> {

    LocalDate from;

    LocalDate to;

    double in;

    double out;

    @Override
    public int compareTo(BalanceInfo bi) {

        if (bi.getFrom().isEqual(from)) {
            return 0;
        } else if (bi.getFrom().isAfter(from)) {
            return -1;
        }

        return 1;
    }
}
