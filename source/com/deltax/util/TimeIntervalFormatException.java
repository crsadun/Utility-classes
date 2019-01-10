// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 28.01.2002 23:42:59
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3)
// Source File Name:   TimeIntervalFormatException.java

package com.deltax.util;


public class TimeIntervalFormatException extends Exception
{

    public TimeIntervalFormatException()
    {
        super("Format is like <x>MONTHS+<y>WEEKS+<z>DAYS+<i>MINUTES+<j>SECONDS+<k>MILLISECONDS");
    }

    public TimeIntervalFormatException(String s)
    {
        super("Format is like <x>WEEKS+<y>DAYS: <" + s + "> is wrong");
    }
}