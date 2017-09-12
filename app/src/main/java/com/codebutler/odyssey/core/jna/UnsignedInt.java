package com.codebutler.odyssey.core.jna;

import com.sun.jna.IntegerType;

public class UnsignedInt extends IntegerType {
    public UnsignedInt() {
        super(4, true);
    }

}
