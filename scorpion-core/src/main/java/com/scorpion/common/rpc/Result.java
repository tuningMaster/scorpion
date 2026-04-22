package com.scorpion.common.rpc;

import lombok.Data;

@Data
public class Result {
    private int code;
    private boolean success;
    private String message;
}
