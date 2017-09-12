package com.codebutler.odyssey.core.jna;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

// FIXME: Can't seem to convert this to Kotlin?

public class SizeT extends IntegerType {
    public SizeT() { this(0); }
    public SizeT(long value) { super(Native.SIZE_T_SIZE, value, true); }
}
